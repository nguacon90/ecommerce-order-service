package com.vctek.orderservice.promotionengine.ruledefinition.condition.builders;


import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrFalseCondition;

public class IrConditions {
    public static RuleIrFalseCondition newIrRuleFalseCondition() {
        return new RuleIrFalseCondition();
    }
}
