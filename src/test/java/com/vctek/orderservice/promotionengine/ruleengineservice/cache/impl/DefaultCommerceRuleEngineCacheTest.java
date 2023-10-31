package com.vctek.orderservice.promotionengine.ruleengineservice.cache.impl;

import com.vctek.orderservice.promotionengine.ruleengine.cache.KIEModuleCacheBuilder;
import com.vctek.orderservice.promotionengine.ruleengine.cache.RuleGlobalsBeanProvider;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEBaseModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.RAOProvider;
import org.assertj.core.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultCommerceRuleEngineCacheTest {
    @Mock
    private RuleGlobalsBeanProvider provider;
    @Mock
    private DroolsKIEModuleModel kieModule;

    private DefaultCommerceRuleEngineCache cache;
    private List<RAOProvider> raoCreators;
    private KIEModuleCacheBuilder kieModuleCacheBuilder;
    @Mock
    private DroolsKIEBaseModel kieBase;
    private Map<Class, RAOProvider> raoProviders;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        raoCreators = new ArrayList<>();
        raoProviders = new HashMap<>();
        when(kieModule.getId()).thenReturn(1l);
        kieModuleCacheBuilder = new DefaultCommerceKIEModuleCacheBuilder(provider, kieModule, raoCreators, false);
        cache = new DefaultCommerceRuleEngineCache(provider);
        cache.setRaoCacheCreators(raoCreators);
        cache.setRaoProviders(raoProviders);
        when(kieBase.getId()).thenReturn(33l);
    }

    @Test
    public void createKIEModuleCacheBuilder() {
        assertNotNull(cache.createKIEModuleCacheBuilder(kieModule));
    }

    @Test
    public void addKIEModuleCache() {
        cache.addKIEModuleCache(kieModuleCacheBuilder);
        assertNotNull(cache.getFactTemplateCacheForKIEModule(kieModule));
    }

    @Test
    public void getCachedFacts() {
        DefaultCommerceKIEModuleCacheBuilder cacheBuilder = new DefaultCommerceKIEModuleCacheBuilder(provider,
                                                                kieModule, raoCreators, false);
        cacheBuilder.getFactTemplateCache().put(kieBase.getId(), Arrays.asList(new Object[]{new Object()}));
        RAOProvider raoProvider = mock(RAOProvider.class);
        raoProviders.put(Object.class, raoProvider);
        Set value = new HashSet();
        value.add(new Object());
        when(raoProvider.expandFactModel(any())).thenReturn(value);
        cache.addKIEModuleCache(cacheBuilder);
        when(kieBase.getDroolsKIEModule()).thenReturn(kieModule);
        Collection<Object> cachedFacts = cache.getCachedFacts(kieBase);
        assertEquals(1, cachedFacts.size());
    }
}
