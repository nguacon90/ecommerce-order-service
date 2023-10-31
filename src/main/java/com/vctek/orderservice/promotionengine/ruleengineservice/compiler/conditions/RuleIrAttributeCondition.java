package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions;

public class RuleIrAttributeCondition extends AbstractRuleIrAttributeCondition {
    private Object value;

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
