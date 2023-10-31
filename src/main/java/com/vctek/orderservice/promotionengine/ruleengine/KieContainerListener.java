package com.vctek.orderservice.promotionengine.ruleengine;

import com.vctek.orderservice.promotionengine.ruleengine.cache.KIEModuleCacheBuilder;
import org.kie.api.runtime.KieContainer;

public interface KieContainerListener {
    void onSuccess(KieContainer container, KIEModuleCacheBuilder cacheBuilder);

    void onFailure(RuleEngineActionResult ruleEngineActionResult);
}
