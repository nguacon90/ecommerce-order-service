package com.vctek.orderservice.promotionengine.ruleengine.cache;


import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEBaseModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;

import java.util.Map;

public interface RuleEngineCache {
    KIEModuleCacheBuilder createKIEModuleCacheBuilder(DroolsKIEModuleModel module);

    void addKIEModuleCache(KIEModuleCacheBuilder builder);

    Map<String, Object> getGlobalsForKIEBase(DroolsKIEBaseModel kieBase);
}
