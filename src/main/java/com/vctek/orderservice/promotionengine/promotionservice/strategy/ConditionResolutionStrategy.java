package com.vctek.orderservice.promotionengine.promotionservice.strategy;


import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;

public interface ConditionResolutionStrategy {
    void cleanStoredParameterValues(RuleCompilerContext context);
}
