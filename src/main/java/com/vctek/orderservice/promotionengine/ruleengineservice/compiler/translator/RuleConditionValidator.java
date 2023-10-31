package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator;

import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;

public interface RuleConditionValidator {
    void validate(RuleCompilerContext context, RuleConditionData condition, RuleConditionDefinitionData conditionDefinition);
}