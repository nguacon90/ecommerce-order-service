package com.vctek.orderservice.promotionengine.ruleengineservice.service;

import com.vctek.orderservice.promotionengine.ruleengine.enums.RuleType;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionDefinitionData;

import java.util.List;
import java.util.Map;

public interface RuleActionsRegistry {
    List<RuleActionDefinitionData> getAllActionDefinitions();

    Map<String, RuleActionDefinitionData> getAllActionDefinitionsAsMap();

    List<RuleActionDefinitionData> getActionDefinitionsForRuleType(RuleType ruleType);

    Map<String, RuleActionDefinitionData> getActionDefinitionsForRuleTypeAsMap(RuleType ruleType);
}
