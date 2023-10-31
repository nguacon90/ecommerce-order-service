package com.vctek.orderservice.promotionengine.ruleengineservice.facade;


import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;

public interface RuleConditionDefinitionFacade {
    RuleConditionDefinitionData findByDefinitionId(String definitionId);
}
