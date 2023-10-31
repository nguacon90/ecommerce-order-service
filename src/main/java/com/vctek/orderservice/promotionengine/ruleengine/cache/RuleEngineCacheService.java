package com.vctek.orderservice.promotionengine.ruleengine.cache;


import com.vctek.orderservice.promotionengine.ruleengine.RuleEvaluationContext;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;

public interface RuleEngineCacheService {
    KIEModuleCacheBuilder createKIEModuleCacheBuilder(DroolsKIEModuleModel module);

    void addToCache(KIEModuleCacheBuilder builder);

    void provideCachedEntities(RuleEvaluationContext context);
}
