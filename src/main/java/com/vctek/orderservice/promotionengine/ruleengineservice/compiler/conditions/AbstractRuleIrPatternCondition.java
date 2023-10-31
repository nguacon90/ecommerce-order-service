package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions;

public abstract class AbstractRuleIrPatternCondition extends RuleIrCondition {

    private String variable;

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }
}
