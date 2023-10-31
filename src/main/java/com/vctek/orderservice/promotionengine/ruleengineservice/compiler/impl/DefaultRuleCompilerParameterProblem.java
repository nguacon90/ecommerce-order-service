package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.impl;

import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerParameterProblem;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;

public class DefaultRuleCompilerParameterProblem extends DefaultRuleCompilerProblem implements RuleCompilerParameterProblem {
    private final RuleParameterData parameter;
    private final RuleParameterDefinitionData parameterDefinition;

    public DefaultRuleCompilerParameterProblem(Severity severity, String message, RuleParameterData parameter, RuleParameterDefinitionData parameterDefinition) {
        super(severity, message);
        this.parameter = parameter;
        this.parameterDefinition = parameterDefinition;
    }

    @Override
    public RuleParameterData getParameter() {
        return this.parameter;
    }

    @Override
    public RuleParameterDefinitionData getParameterDefinition() {
        return this.parameterDefinition;
    }
}
