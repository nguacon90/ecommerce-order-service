package com.vctek.orderservice.promotionengine.ruleengineservice.maintenance;

public interface RuleCompilationContext {
    Long getNextRuleEngineRuleVersion(String moduleName);
}
