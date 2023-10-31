package com.vctek.orderservice.promotionengine.ruleengineservice.data;

import java.util.List;

public class RuleConditionData extends AbstractRuleData {
    private List<RuleConditionData> children;

    public List<RuleConditionData> getChildren() {
        return children;
    }

    public void setChildren(List<RuleConditionData> children) {
        this.children = children;
    }
}
