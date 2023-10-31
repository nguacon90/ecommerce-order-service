package com.vctek.orderservice.promotionengine.ruleengineservice.service;


import com.vctek.orderservice.promotionengine.ruleengine.enums.RuleType;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleActionDefinitionModel;

import java.util.List;

public interface RuleActionDefinitionService {
    List<RuleActionDefinitionModel> getAllRuleActionDefinitions();

    List<RuleActionDefinitionModel> getRuleActionDefinitionsForRuleType(RuleType type);

    RuleActionDefinitionModel findByCode(String definitionId);
}
