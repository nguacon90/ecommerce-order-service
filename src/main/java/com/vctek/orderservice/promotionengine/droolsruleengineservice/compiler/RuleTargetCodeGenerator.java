package com.vctek.orderservice.promotionengine.droolsruleengineservice.compiler;


import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleIr;

public interface RuleTargetCodeGenerator {
    DroolsRuleModel generate(RuleCompilerContext ruleCompilerContext, RuleIr ruleIr);
}
