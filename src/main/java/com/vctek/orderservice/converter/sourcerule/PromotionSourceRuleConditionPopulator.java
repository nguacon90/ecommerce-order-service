package com.vctek.orderservice.converter.sourcerule;

import com.vctek.converter.Populator;
import com.vctek.dto.promotion.ConditionDTO;
import com.vctek.dto.promotion.PromotionSourceRuleDTO;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.enums.RuleType;
import com.vctek.orderservice.promotionengine.ruleengineservice.converter.RuleConditionsConverter;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleConditionsRegistry;
import com.vctek.orderservice.util.PromotionDefinitionCode;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component("promotionSourceRuleConditionPopulator")
public class PromotionSourceRuleConditionPopulator extends AbstractPromotionSourceRulePopulator
        implements Populator<PromotionSourceRuleModel, PromotionSourceRuleDTO> {

    private RuleConditionsConverter ruleConditionsConverter;
    private RuleConditionsRegistry ruleConditionsRegistry;

    @Override
    public void populate(PromotionSourceRuleModel source, PromotionSourceRuleDTO target) {
        List<ConditionDTO> conditions = new ArrayList<>();
        Map<String, RuleConditionDefinitionData> conditionDefinitionData = ruleConditionsRegistry.getConditionDefinitionsForRuleTypeAsMap(RuleType.PROMOTION);
        List<RuleConditionData> ruleConditionData = ruleConditionsConverter.fromString(source.getConditions(), conditionDefinitionData);

        for(RuleConditionData conditionData : ruleConditionData) {
            ConditionDTO dto = new ConditionDTO();
            String definitionId = conditionData.getDefinitionId();
            if(PromotionDefinitionCode.ORDER_TYPES.code().equals(definitionId) ||
                    PromotionDefinitionCode.WAREHOUSE.code().equals(definitionId) ||
                    PromotionDefinitionCode.EXCLUDE_ORDER_SOURCES.code().equals(definitionId) ||
                    PromotionDefinitionCode.PRICE_TYPES.code().equals(definitionId)) {
                continue;
            }

            if(CollectionUtils.isNotEmpty(conditionData.getChildren())) {
                List<ConditionDTO> children = populateChildren(conditionData);
                dto.setChildren(children);
            }
            dto.setDefinitionId(definitionId);
            dto.setParameters(populateParameters(conditionData.getParameters()));
            conditions.add(dto);
        }

        target.setConditions(conditions);
    }

    private List<ConditionDTO> populateChildren(RuleConditionData conditionData) {
        List<ConditionDTO> children = new ArrayList<>();
        ConditionDTO child;
        for(RuleConditionData childData : conditionData.getChildren()) {
            child = new ConditionDTO();
            child.setDefinitionId(childData.getDefinitionId());
            child.setParameters(populateParameters(childData.getParameters()));
            if(CollectionUtils.isNotEmpty(childData.getChildren())) {
                child.setChildren(populateChildren(childData));
            }
            children.add(child);
        }
        return children;
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
