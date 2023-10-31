package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions;

public class RuleIrNotCondition extends RuleIrConditionWithChildren {
    private RuleIrLocalVariablesContainer variablesContainer;

    public RuleIrLocalVariablesContainer getVariablesContainer() {
        return variablesContainer;
    }

    public void setVariablesContainer(RuleIrLocalVariablesContainer variablesContainer) {
        this.variablesContainer = variablesContainer;
    }
}
