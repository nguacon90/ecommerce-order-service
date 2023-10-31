package com.vctek.orderservice.promotionengine.ruleengineservice.strategy;


import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;

@FunctionalInterface
public interface RuleMessageParameterDecorator {
    String decorate(String var1, RuleParameterData var2);
}
