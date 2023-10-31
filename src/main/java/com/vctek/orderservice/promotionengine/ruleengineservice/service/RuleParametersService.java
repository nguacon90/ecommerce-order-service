package com.vctek.orderservice.promotionengine.ruleengineservice.service;


import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;

import java.util.List;

public interface RuleParametersService {
    RuleParameterData createParameterFromDefinition(RuleParameterDefinitionData var1);

    String convertParametersToString(List<RuleParameterData> var1);

    List<RuleParameterData> convertParametersFromString(String var1);
}
