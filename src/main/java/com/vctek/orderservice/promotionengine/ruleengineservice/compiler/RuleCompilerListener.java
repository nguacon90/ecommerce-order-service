package com.vctek.orderservice.promotionengine.ruleengineservice.compiler;

public interface RuleCompilerListener {
    void beforeCompile(RuleCompilerContext context);

    void afterCompile(RuleCompilerContext context);

    void afterCompileError(RuleCompilerContext context);
}
