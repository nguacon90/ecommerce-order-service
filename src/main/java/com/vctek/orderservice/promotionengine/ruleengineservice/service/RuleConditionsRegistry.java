package com.vctek.orderservice.promotionengine.ruleengineservice.service;


import com.vctek.orderservice.promotionengine.ruleengine.enums.RuleType;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;

import java.util.List;
import java.util.Map;

public interface RuleConditionsRegistry {
    List<RuleConditionDefinitionData> getAllConditionDefinitions();

    Map<String, RuleConditionDefinitionData> getAllConditionDefinitionsAsMap();

    List<RuleConditionDefinitionData> getConditionDefinitionsForRuleType(RuleType ruleType);

    Map<String, RuleConditionDefinitionData> getConditionDefinitionsForRuleTypeAsMap(RuleType ruleType);
}
