package com.vctek.orderservice.promotionengine.ruleengine.cache.impl;

import com.vctek.orderservice.promotionengine.ruleengine.cache.RuleGlobalsBeanProvider;
import com.vctek.orderservice.promotionengine.ruleengine.infrastructure.GetRuleEngineGlobalByName;
import org.springframework.stereotype.Component;

@Component
public class DefaultRuleGlobalsBeanProvider implements RuleGlobalsBeanProvider {

    @Override
    @GetRuleEngineGlobalByName
    public Object getRuleGlobals(String value) {
        return null;
    }
}
