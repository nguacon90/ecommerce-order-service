package com.vctek.orderservice.promotionengine.ruleengineservice.compiler;

import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;

public interface RuleCompilerProblemFactory {
    RuleCompilerProblem createProblem(RuleCompilerProblem.Severity severity, String messageKey, Object... parameters);

    RuleCompilerParameterProblem createParameterProblem(RuleCompilerProblem.Severity severity, String messageKey, RuleParameterData parameterData,
                                                        RuleParameterDefinitionData parameterDefinitionData, Object... parameters);
}
