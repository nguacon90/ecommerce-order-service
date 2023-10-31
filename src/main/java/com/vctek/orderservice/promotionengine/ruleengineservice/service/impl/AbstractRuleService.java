package com.vctek.orderservice.promotionengine.ruleengineservice.service.impl;

import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleParametersService;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class AbstractRuleService {

    protected RuleParametersService ruleParametersService;

    protected AbstractRuleService(RuleParametersService ruleParametersService) {
        this.ruleParametersService = ruleParametersService;
    }

    protected Map<String, RuleParameterData> populateRuleParams(Map<String, RuleParameterDefinitionData> ruleParameterDefinition) {
        Iterator iterator = ruleParameterDefinition.entrySet().iterator();
        Map<String, RuleParameterData> parameters = new HashMap();
        while(iterator.hasNext()) {
            Map.Entry<String, RuleParameterDefinitionData> entry = (Map.Entry)iterator.next();
            String parameterId = entry.getKey();
            RuleParameterDefinitionData parameterDefinition = entry.getValue();
            RuleParameterData parameter = this.ruleParametersService.createParameterFromDefinition(parameterDefinition);
            parameters.put(parameterId, parameter);
        }

        return parameters;
    }
}
