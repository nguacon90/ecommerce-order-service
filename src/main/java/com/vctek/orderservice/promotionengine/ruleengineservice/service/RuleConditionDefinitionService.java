package com.vctek.orderservice.promotionengine.ruleengineservice.service;


import com.vctek.orderservice.promotionengine.ruleengine.enums.RuleType;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleConditionDefinitionModel;

import java.util.List;

public interface RuleConditionDefinitionService {
    List<RuleConditionDefinitionModel> getAllRuleConditionDefinitions();

    List<RuleConditionDefinitionModel> getRuleConditionDefinitionsForRuleType(RuleType type);

    RuleConditionDefinitionModel findByCode(String definitionId);
}
