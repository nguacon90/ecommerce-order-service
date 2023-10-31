package com.vctek.orderservice.promotionengine.ruleengineservice.service;


import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionDefinitionData;

import java.util.List;
import java.util.Map;

public interface RuleActionsService {
    RuleActionData createActionFromDefinition(RuleActionDefinitionData definition);

    List<RuleActionData> convertActionsFromString(String actions, Map<String, RuleActionDefinitionData> actionDefinitions);
}
