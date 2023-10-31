package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.validation;

import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.AbstractRuleDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;

public interface RuleParameterValidator {
    void validate(RuleCompilerContext context, AbstractRuleDefinitionData ruleDefinition, RuleParameterData parameter,
                  RuleParameterDefinitionData parameterDefinition);
}
