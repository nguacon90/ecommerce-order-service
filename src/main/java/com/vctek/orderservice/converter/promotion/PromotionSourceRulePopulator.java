package com.vctek.orderservice.converter.promotion;

import com.vctek.converter.Populator;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.enums.RuleType;
import com.vctek.orderservice.promotionengine.ruleengineservice.converter.RuleActionsConverter;
import com.vctek.orderservice.promotionengine.ruleengineservice.converter.RuleConditionsConverter;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.PromotionSourceRuleData;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleActionsRegistry;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleConditionsRegistry;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("promotionSourceRulePopulator")
public class PromotionSourceRulePopulator implements Populator<PromotionSourceRuleData, PromotionSourceRuleModel> {

    public static final int MAX_ALLOWED_RUNS = 1;
    public static final int DEFAULT_PRIORITY_HIGHEST = 0;
    private RuleConditionsConverter ruleConditionsConverter;
    private RuleActionsConverter ruleActionsConverter;
    private RuleConditionsRegistry ruleConditionsRegistry;
    private RuleActionsRegistry ruleActionsRegistry;

    @Override
    public void populate(PromotionSourceRuleData source, PromotionSourceRuleModel target) {
        target.setCompanyId(source.getCompanyId());
        target.setStartDate(source.getStartDate());
        target.setEndDate(source.getEndDate());
        if(StringUtils.isBlank(target.getCode())) {
            target.setCode(source.getCode());
        }

        target.setAppliedWarehouseIds(source.getAppliedWarehouseIds());
        target.setAppliedOrderTypes(source.getAppliedOrderTypes());
        target.setAppliedPriceTypes(source.getAppliedPriceTypes());
        target.setExcludeOrderSources(source.getExcludeOrderSources());
        target.setMaxAllowedRuns(MAX_ALLOWED_RUNS);
        if(StringUtils.isBlank(target.getUuid())) {
            target.setUuid(UUID.randomUUID().toString());
        }
        target.setDescription(source.getDescription());
        target.setStatus(source.getStatus());
        target.setActive(source.isActive());
        target.setMessageFired(source.getMessageFired());
        target.setPriority(source.getPriority() == null ? DEFAULT_PRIORITY_HIGHEST : source.getPriority());
        target.setName(source.getName());
        target.setAppliedOnlyOne(source.isAppliedOnlyOne());
        target.setConditions(ruleConditionsConverter.toString(source.getConditions(),
                ruleConditionsRegistry.getConditionDefinitionsForRuleTypeAsMap(RuleType.PROMOTION)));

        target.setActions(ruleActionsConverter.toString(source.getActions(),
                ruleActionsRegistry.getActionDefinitionsForRuleTypeAsMap(RuleType.PROMOTION)));
        target.setCampaigns(source.getCampaigns());
        target.setAllowReward(source.isAllowReward());
    }

    @Autowired
    public void setRuleConditionsConverter(RuleConditionsConverter ruleConditionsConverter) {
        this.ruleConditionsConverter = ruleConditionsConverter;
    }

    @Autowired
    public void setRuleConditionsRegistry(RuleConditionsRegistry ruleConditionsRegistry) {
        this.ruleConditionsRegistry = ruleConditionsRegistry;
    }

    @Autowired
    public void setRuleActionsConverter(RuleActionsConverter ruleActionsConverter) {
        this.ruleActionsConverter = ruleActionsConverter;
    }

    @Autowired
    public void setRuleActionsRegistry(RuleActionsRegistry ruleActionsRegistry) {
        this.ruleActionsRegistry = ruleActionsRegistry;
    }

}
