package com.vctek.orderservice.promotionengine.ruleengineservice.converter;


import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;

import java.util.List;
import java.util.Map;

public interface RuleConditionsConverter {
    String toString(List<RuleConditionData> condition, Map<String, RuleConditionDefinitionData> conditionDefinitions);

    List<RuleConditionData> fromString(String conditions, Map<String, RuleConditionDefinitionData> conditionDefinitions);
}
