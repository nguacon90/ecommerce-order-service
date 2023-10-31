package com.vctek.orderservice.converter.storefront;

import com.vctek.converter.Populator;
import com.vctek.dto.promotion.ConditionDTO;
import com.vctek.orderservice.converter.sourcerule.AbstractPromotionSourceRulePopulator;
import com.vctek.orderservice.dto.CommercePromotionData;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.enums.RuleType;
import com.vctek.orderservice.promotionengine.ruleengineservice.converter.RuleConditionsConverter;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleConditionsRegistry;
import com.vctek.orderservice.util.ConditionDefinitionParameter;
import com.vctek.orderservice.util.PromotionDefinitionCode;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
public class CommercePromotionDataPopulator extends AbstractPromotionSourceRulePopulator implements Populator<PromotionSourceRuleModel, CommercePromotionData> {
    private RuleConditionsConverter ruleConditionsConverter;
    private RuleConditionsRegistry ruleConditionsRegistry;

    @Override
    public void populate(PromotionSourceRuleModel source, CommercePromotionData target) {
        target.setId(source.getId());
        target.setCode(source.getCode());
        target.setCompanyId(source.getCompanyId());
        target.setMessageFired(source.getMessageFired());
        target.setStartDate(source.getStartDate());
        target.setEndDate(source.getEndDate());
        target.setActive(source.isActive());
        target.setPublishedStatus(source.getStatus());
        target.setPriority(source.getPriority());
        target.setAllowReward(source.isAllowReward());
        target.setAppliedOnlyOne(source.isAppliedOnlyOne());
        target.setDescription(source.getDescription());
        populateWarehouses(source, target);
        populateOrderTypes(source, target);
        populatePriceTypes(source, target);
        populateExcludeOrderSources(source, target);
        populateConditionProducts(source, target);
    }

    private void populateConditionProducts(PromotionSourceRuleModel source, CommercePromotionData target) {
        Map<String, RuleConditionDefinitionData> conditionDefinitionData = ruleConditionsRegistry.getConditionDefinitionsForRuleTypeAsMap(RuleType.PROMOTION);
        List<RuleConditionData> ruleConditionData = ruleConditionsConverter.fromString(source.getConditions(), conditionDefinitionData);

        for(RuleConditionData conditionData : ruleConditionData) {
            String definitionId = conditionData.getDefinitionId();
            if(PromotionDefinitionCode.ORDER_TYPES.code().equals(definitionId) ||
                    PromotionDefinitionCode.WAREHOUSE.code().equals(definitionId) ||
                    PromotionDefinitionCode.EXCLUDE_ORDER_SOURCES.code().equals(definitionId) ||
                    PromotionDefinitionCode.PRICE_TYPES.code().equals(definitionId)) {
                continue;
            }

            if(PromotionDefinitionCode.QUALIFIER_PRODUCTS.code().equalsIgnoreCase(definitionId)) {
                target.setConditionProducts(getCollectionOf(conditionData, ConditionDefinitionParameter.QUALIFYING_PRODUCTS.code()));
            } else if (PromotionDefinitionCode.QUALIFIER_CATEGORIES.code().equalsIgnoreCase(definitionId)) {
                target.setConditionCategories(getCollectionOf(conditionData, ConditionDefinitionParameter.QUALIFYING_CATEGORIES.code()));
            }

        }
    }

    private List<Long> getCollectionOf(RuleConditionData ruleConditionData, String key) {
        Object products = ruleConditionData.getParameters().get(key).getValue();
        List<Long> result = new ArrayList<>();
        if(products instanceof Collection) {
            ((Collection) products).stream().forEach(p -> result.add(Long.valueOf(p.toString())));
            return result;
        }

        return result;
    }


    @Autowired
    public void setRuleConditionsConverter(RuleConditionsConverter ruleConditionsConverter) {
        this.ruleConditionsConverter = ruleConditionsConverter;
    }

    @Autowired
    public void setRuleConditionsRegistry(RuleConditionsRegistry ruleConditionsRegistry) {
        this.ruleConditionsRegistry = ruleConditionsRegistry;
    }
}
