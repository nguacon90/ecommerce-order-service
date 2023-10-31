package com.vctek.orderservice.promotionengine.ruledefinition.condition.builders;


import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrAttributeCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleIrAttributeOperator;

public class RuleIrAttributeConditionBuilder {
    private static RuleIrAttributeConditionBuilder self = new RuleIrAttributeConditionBuilder();
    private RuleIrAttributeCondition condition;

    public static RuleIrAttributeConditionBuilder newAttributeConditionFor(String variableName) {
        self.condition = new RuleIrAttributeCondition();
        self.condition.setVariable(variableName);
        return self;
    }

    public RuleIrAttributeConditionBuilder withAttribute(String attribute) {
        self.condition.setAttribute(attribute);
        return self;
    }

    public RuleIrAttributeConditionBuilder withOperator(RuleIrAttributeOperator operator) {
        self.condition.setOperator(operator);
        return self;
    }

    public RuleIrAttributeConditionBuilder withValue(Object value) {
        self.condition.setValue(value);
        return self;
    }

    public RuleIrAttributeCondition build() {
        return this.condition;
    }
}
