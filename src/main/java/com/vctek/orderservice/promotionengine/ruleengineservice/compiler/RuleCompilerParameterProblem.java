package com.vctek.orderservice.promotionengine.ruleengineservice.compiler;

import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;

public interface RuleCompilerParameterProblem extends RuleCompilerProblem {
    RuleParameterData getParameter();

    RuleParameterDefinitionData getParameterDefinition();
}
