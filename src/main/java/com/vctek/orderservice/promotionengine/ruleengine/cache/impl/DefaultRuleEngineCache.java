package com.vctek.orderservice.promotionengine.ruleengine.cache.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.vctek.orderservice.promotionengine.ruleengine.cache.KIEModuleCacheBuilder;
import com.vctek.orderservice.promotionengine.ruleengine.cache.RuleEngineCache;
import com.vctek.orderservice.promotionengine.ruleengine.cache.RuleGlobalsBeanProvider;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEBaseModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component("defaultRuleEngineCache")
public class DefaultRuleEngineCache implements RuleEngineCache {
    protected final ConcurrentHashMap<Object, Map<Object, Map<String, Object>>> globalsCache = new ConcurrentHashMap();

    @Value("${defaultRuleEngineCacheService.globals.fail.on.bean.mismatch:false}")
    protected boolean failOnBeanMismatch;

    protected RuleGlobalsBeanProvider ruleGlobalsBeanProvider;

    public DefaultRuleEngineCache(RuleGlobalsBeanProvider ruleGlobalsBeanProvider) {
        this.ruleGlobalsBeanProvider = ruleGlobalsBeanProvider;
    }

    @Override
    public KIEModuleCacheBuilder createKIEModuleCacheBuilder(DroolsKIEModuleModel kieModule) {
        return new DefaultKIEModuleCacheBuilder(this.ruleGlobalsBeanProvider, kieModule, failOnBeanMismatch);
    }

    @Override
    public void addKIEModuleCache(KIEModuleCacheBuilder cacheBuilder) {
        Preconditions.checkArgument(cacheBuilder instanceof DefaultKIEModuleCacheBuilder, "cache must be of type DefaultRuleEngineKIEModuleCacheBuilder");
        DefaultKIEModuleCacheBuilder moduleCache = (DefaultKIEModuleCacheBuilder)cacheBuilder;
        Map<Object, Map<String, Object>> globals = moduleCache.getGlobalsCache();
        this.globalsCache.put(moduleCache.getKieModule().getId(), ImmutableMap.copyOf(globals));
    }

    @Override
    public Map<String, Object> getGlobalsForKIEBase(DroolsKIEBaseModel kieBase) {
        Object key = kieBase.getId();
        return (Map)((Map)this.getGlobalsCacheForKIEModule(kieBase.getDroolsKIEModule())
                .orElse(Collections.emptyMap())).getOrDefault(key, Collections.emptyMap());
    }

    protected Optional<Map<Object, Map<String, Object>>> getGlobalsCacheForKIEModule(DroolsKIEModuleModel kieModule) {
        Object key = kieModule.getId();
        return Optional.ofNullable(this.globalsCache.get(key));
    }
}
