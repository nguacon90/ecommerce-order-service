package com.vctek.orderservice.promotionengine.ruleengineservice.converter;


import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionDefinitionData;

import java.util.List;
import java.util.Map;

public interface RuleActionsConverter {
    String toString(List<RuleActionData> actions, Map<String, RuleActionDefinitionData> actionDefinitions);

    List<RuleActionData> fromString(String actions, Map<String, RuleActionDefinitionData> actionDefinitions);
}
