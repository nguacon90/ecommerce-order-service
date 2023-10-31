package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions;


import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleIrGroupOperator;

public class RuleIrGroupCondition extends RuleIrConditionWithChildren {
    private RuleIrGroupOperator operator;

    public RuleIrGroupOperator getOperator() {
        return operator;
    }

    public void setOperator(RuleIrGroupOperator operator) {
        this.operator = operator;
    }
}
