package com.vctek.orderservice.promotionengine.ruleengineservice.facade;


import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionDefinitionData;

public interface RuleActionDefinitionFacade {
    RuleActionDefinitionData findByDefinitionId(String definitionId);
}
