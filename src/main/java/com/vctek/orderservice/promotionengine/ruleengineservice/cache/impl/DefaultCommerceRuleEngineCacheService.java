package com.vctek.orderservice.promotionengine.ruleengineservice.cache.impl;

import com.vctek.orderservice.promotionengine.ruleengine.RuleEvaluationContext;
import com.vctek.orderservice.promotionengine.ruleengine.cache.KIEModuleCacheBuilder;
import com.vctek.orderservice.promotionengine.ruleengine.cache.impl.DefaultRuleEngineCacheService;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleEngineContextModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.cache.CommerceRuleEngineCache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component("commerceRuleEngineCacheService")
public class DefaultCommerceRuleEngineCacheService extends DefaultRuleEngineCacheService {
    private CommerceRuleEngineCache commerceRuleEngineCache;

    public DefaultCommerceRuleEngineCacheService(@Qualifier("defaultCommerceRuleEngineCache")
                                                         CommerceRuleEngineCache commerceRuleEngineCache) {
        super(commerceRuleEngineCache);
        this.commerceRuleEngineCache = commerceRuleEngineCache;
    }

    public void addToCache(KIEModuleCacheBuilder cacheBuilder) {
        this.commerceRuleEngineCache.addKIEModuleCache(cacheBuilder);
    }

    public void provideCachedEntities(RuleEvaluationContext context) {
        super.provideCachedEntities(context);
        DroolsRuleEngineContextModel engineContext = (DroolsRuleEngineContextModel)context.getRuleEngineContext();
        Set<Object> facts = this.getOrCreateFacts(context);
        Collection<Object> cachedFacts = this.commerceRuleEngineCache.getCachedFacts(engineContext.getKieSession()
                .getDroolsKIEBase());
        facts.addAll(cachedFacts);
    }

    protected Set<Object> getOrCreateFacts(RuleEvaluationContext context) {
        if (context.getFacts() == null) {
            Set<Object> facts = new HashSet();
            context.setFacts(facts);
        }

        return context.getFacts();
    }

}
