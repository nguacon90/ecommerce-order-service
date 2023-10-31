package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions;


import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleIrAttributeOperator;

public abstract class AbstractRuleIrAttributeCondition extends AbstractRuleIrPatternCondition {
    private String attribute;
    private RuleIrAttributeOperator operator;

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public RuleIrAttributeOperator getOperator() {
        return operator;
    }

    public void setOperator(RuleIrAttributeOperator operator) {
        this.operator = operator;
    }
}
