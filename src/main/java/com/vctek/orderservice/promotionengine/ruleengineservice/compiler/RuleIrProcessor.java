package com.vctek.orderservice.promotionengine.ruleengineservice.compiler;

public interface RuleIrProcessor {
    void process(DefaultRuleCompilerContext context, RuleIr ruleIr);
}
