package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions;

import java.util.List;

public class RuleIrConditionWithChildren extends RuleIrCondition {
    private List<RuleIrCondition> children;

    public List<RuleIrCondition> getChildren() {
        return children;
    }

    public void setChildren(List<RuleIrCondition> children) {
        this.children = children;
    }
}
