package com.vctek.orderservice.promotionengine.ruleengineservice.cache.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.vctek.orderservice.promotionengine.ruleengine.cache.KIEModuleCacheBuilder;
import com.vctek.orderservice.promotionengine.ruleengine.cache.RuleGlobalsBeanProvider;
import com.vctek.orderservice.promotionengine.ruleengine.cache.impl.DefaultRuleEngineCache;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEBaseModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.cache.CommerceRuleEngineCache;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.RAOProvider;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component("defaultCommerceRuleEngineCache")
public class DefaultCommerceRuleEngineCache extends DefaultRuleEngineCache implements CommerceRuleEngineCache {
    private final ConcurrentHashMap<Object, Map<Object, Collection<Object>>> factTemplateCache = new ConcurrentHashMap();
    protected static final RAOProvider<Object> identityRAOProvider = Collections::singleton;
    private Map<Class, RAOProvider> raoProviders;
    private List<RAOProvider> raoCacheCreators;

    @Value("${defaultCommerceRuleEngineCache.checkRAOProvidersForCache:true}")
    private boolean checkRAOProvidersForCache = true;

    public DefaultCommerceRuleEngineCache(RuleGlobalsBeanProvider ruleGlobalsBeanProvider) {
        super(ruleGlobalsBeanProvider);
    }

    @Override
    public KIEModuleCacheBuilder createKIEModuleCacheBuilder(DroolsKIEModuleModel kieModule) {
        return new DefaultCommerceKIEModuleCacheBuilder(this.ruleGlobalsBeanProvider, kieModule,
                ImmutableList.copyOf(this.raoCacheCreators), false);
    }

    @Override
    public void addKIEModuleCache(KIEModuleCacheBuilder cacheBuilder) {
        Preconditions.checkArgument(cacheBuilder instanceof DefaultCommerceKIEModuleCacheBuilder,
                "cache must be of type DefaultCommerceRuleEngineKIEModuleCache");
        super.addKIEModuleCache(cacheBuilder);
        DefaultCommerceKIEModuleCacheBuilder cacheBuilderImpl = (DefaultCommerceKIEModuleCacheBuilder) cacheBuilder;
        Map<Object, Collection<Object>> factTemplates = cacheBuilderImpl.getFactTemplateCache();
        this.checkFactTemplates(factTemplates);
        this.factTemplateCache.put(cacheBuilderImpl.getKieModule().getId(), factTemplates);
    }

    @Override
    public Collection<Object> getCachedFacts(DroolsKIEBaseModel kieBase) {
        Collection<Object> factTemplates = this.getFactTemplateCacheForKieBase(kieBase);
        Collection<Object> facts = new HashSet();
        factTemplates.forEach((ft) -> {
            facts.addAll(this.getRaoProvider(ft).orElse(identityRAOProvider).expandFactModel(ft));
        });
        return facts;
    }

    protected void checkFactTemplates(Map<Object, Collection<Object>> factTemplates) {
        if (checkRAOProvidersForCache && MapUtils.isNotEmpty(factTemplates)) {
            factTemplates.entrySet().stream().map(Map.Entry::getValue).flatMap(Collection::stream).forEach((ft) -> {
                this.getRaoProvider(ft).orElseThrow(() ->
                        new IllegalArgumentException("Cannot create cache. No RAOProvider registered in " +
                                "DefaultCommerceRuleEngineCacheService for facts of class: " + ft.getClass().getName()
                                + ". Please register an RAOProvider for this class. " +
                                "Otherwise this fact template cannot be cloned into a fact at rule evaluation time. "
                                + "You can disable this check by setting the system variable " +
                                "'defaultCommerceRuleEngineCache.checkRAOProvidersForCache' to false. " +
                                "If you do that, the fact templates will be inserted directly without " +
                                "creating a copy of them (make sure these facts are not modified by the rule evaluation though!)"));
            });
        }

    }

    protected Collection<Object> getFactTemplateCacheForKieBase(DroolsKIEBaseModel kieBase) {
        Object key = kieBase.getId();
        return this.getFactTemplateCacheForKIEModule(kieBase.getDroolsKIEModule())
                .orElse(Collections.emptyMap())
                .getOrDefault(key, Collections.emptyList());
    }

    protected Optional<Map<Object, Collection<Object>>> getFactTemplateCacheForKIEModule(DroolsKIEModuleModel kieModule) {
        Object key = kieModule.getId();
        return Optional.ofNullable(this.factTemplateCache.get(key));
    }

    protected Optional<RAOProvider> getRaoProvider(Object factTemplate) {
        return Optional.ofNullable(this.raoProviders.get(factTemplate.getClass()));
    }

    @Autowired
    @Qualifier("commerceRuleEngineRaoCacheProviders")
    public void setRaoProviders(Map<Class, RAOProvider> raoProviders) {
        this.raoProviders = raoProviders;
    }

    @Autowired
    @Qualifier("commerceRuleEngineRaoCacheCreators")
    public void setRaoCacheCreators(List<RAOProvider> raoCacheCreators) {
        this.raoCacheCreators = raoCacheCreators;
    }
}
