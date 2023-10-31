package com.vctek.orderservice.promotionengine.ruleengineservice.cache;

import com.vctek.orderservice.promotionengine.ruleengine.cache.RuleEngineCache;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEBaseModel;

import java.util.Collection;

public interface CommerceRuleEngineCache extends RuleEngineCache {
    Collection<Object> getCachedFacts(DroolsKIEBaseModel kieBase);
}
