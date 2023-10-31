package com.vctek.orderservice.promotionengine.ruledefinition.condition.builders;

import com.google.common.collect.Lists;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrGroupCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleIrGroupOperator;

import java.util.List;

public class RuleIrGroupConditionBuilder {
    private static RuleIrGroupConditionBuilder self = new RuleIrGroupConditionBuilder();
    private RuleIrGroupCondition condition;

    public static RuleIrGroupConditionBuilder newGroupConditionOf(RuleIrGroupOperator operator) {
        self.condition = new RuleIrGroupCondition();
        self.condition.setOperator(operator);
        self.condition.setChildren(Lists.newArrayList());
        return self;
    }

    public RuleIrGroupConditionBuilder withChildren(List<RuleIrCondition> leafConditions) {
        self.condition.setChildren(leafConditions);
        return self;
    }

    public RuleIrGroupCondition build() {
        return this.condition;
    }
}
