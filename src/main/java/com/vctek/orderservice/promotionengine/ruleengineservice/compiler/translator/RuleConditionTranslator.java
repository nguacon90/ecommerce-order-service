package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator;


import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;

public interface RuleConditionTranslator {
    RuleIrCondition translate(RuleCompilerContext context, RuleConditionData ruleConditionData,
                              RuleConditionDefinitionData ruleConditionDefinitionData);
}
