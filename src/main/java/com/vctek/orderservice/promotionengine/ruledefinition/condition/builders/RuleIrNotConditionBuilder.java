package com.vctek.orderservice.promotionengine.ruledefinition.condition.builders;

import com.google.common.collect.Lists;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrLocalVariablesContainer;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrNotCondition;

import java.util.List;

public class RuleIrNotConditionBuilder {
    private static RuleIrNotConditionBuilder self = new RuleIrNotConditionBuilder();
    private RuleIrNotCondition condition;

    public RuleIrNotConditionBuilder() {
        //NOSONAR
    }

    public static RuleIrNotConditionBuilder newNotCondition() {
        self.condition = new RuleIrNotCondition();
        self.condition.setChildren(Lists.newArrayList());
        return self;
    }

    public RuleIrNotConditionBuilder withVariablesContainer(RuleIrLocalVariablesContainer container) {
        self.condition.setVariablesContainer(container);
        return self;
    }

    public RuleIrNotConditionBuilder withChildren(List<RuleIrCondition> leafConditions) {
        self.condition.setChildren(leafConditions);
        return self;
    }

    public RuleIrNotCondition build() {
        return this.condition;
    }
}
