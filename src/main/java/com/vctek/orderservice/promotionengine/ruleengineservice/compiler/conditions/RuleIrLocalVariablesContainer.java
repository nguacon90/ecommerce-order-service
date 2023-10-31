package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions;


import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleIrVariable;

import java.util.Map;

public class RuleIrLocalVariablesContainer {
    private Map<String, RuleIrVariable> variables;

    public Map<String, RuleIrVariable> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, RuleIrVariable> variables) {
        this.variables = variables;
    }
}
