package com.vctek.orderservice.promotionengine.ruleengine.cache;

@FunctionalInterface
public interface RuleGlobalsBeanProvider {
    Object getRuleGlobals(String value);
}
