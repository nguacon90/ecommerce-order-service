package com.vctek.orderservice.promotionengine.ruleengineservice.cache.impl;


import com.vctek.orderservice.promotionengine.ruleengine.cache.RuleGlobalsBeanProvider;
import com.vctek.orderservice.promotionengine.ruleengine.cache.impl.DefaultKIEModuleCacheBuilder;
import com.vctek.orderservice.promotionengine.ruleengine.model.AbstractRuleEngineRuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEBaseModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.RAOProvider;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultCommerceKIEModuleCacheBuilder extends DefaultKIEModuleCacheBuilder {
    private final Map<Object, Collection<Object>> factTemplateCache = new ConcurrentHashMap();
    private List<RAOProvider> raoCacheCreators;

    public DefaultCommerceKIEModuleCacheBuilder(RuleGlobalsBeanProvider ruleGlobalsBeanProvider,
                                                DroolsKIEModuleModel kieModule, List<RAOProvider> raoCacheCreators,
                                                boolean failOnBeanMismatches) {
        super(ruleGlobalsBeanProvider, kieModule, failOnBeanMismatches);
        this.raoCacheCreators = raoCacheCreators;
    }

    public <T extends AbstractRuleEngineRuleModel> void processRule(T rule) {
        super.processRule(rule);
        DroolsRuleModel droolsRule = (DroolsRuleModel)rule;
        Collection<Object> cacheSegment = this.getFactTemplateCacheSegmentForKieBase(droolsRule.getKieBase());
        this.raoCacheCreators.forEach((creator) -> {
            cacheSegment.addAll(creator.expandFactModel(rule));
        });
    }

    protected Collection<Object> getFactTemplateCacheSegmentForKieBase(DroolsKIEBaseModel kieBase) {
        return this.factTemplateCache.computeIfAbsent(kieBase.getId(), (k) -> ConcurrentHashMap.newKeySet());
    }


    public Map<Object, Collection<Object>> getFactTemplateCache() {
        return this.factTemplateCache;
    }
}
