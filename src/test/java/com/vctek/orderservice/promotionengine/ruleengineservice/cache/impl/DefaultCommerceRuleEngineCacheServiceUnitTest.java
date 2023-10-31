package com.vctek.orderservice.promotionengine.ruleengineservice.cache.impl;

import com.vctek.orderservice.promotionengine.ruleengine.RuleEvaluationContext;
import com.vctek.orderservice.promotionengine.ruleengine.cache.KIEModuleCacheBuilder;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEBaseModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIESessionModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleEngineContextModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.cache.CommerceRuleEngineCache;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class DefaultCommerceRuleEngineCacheServiceUnitTest {
    @Mock
    private CommerceRuleEngineCache commerceRuleEngineCache;

    private DefaultCommerceRuleEngineCacheService service;

    @Mock
    private KIEModuleCacheBuilder cacheBuilder;

    @Mock
    private RuleEvaluationContext context;

    @Mock
    private DroolsRuleEngineContextModel ruleContextModel;

    @Mock
    private DroolsKIESessionModel kieSession;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new DefaultCommerceRuleEngineCacheService(commerceRuleEngineCache);
    }

    @Test
    public void addToCache() {
        service.addToCache(cacheBuilder);
        verify(commerceRuleEngineCache).addKIEModuleCache(cacheBuilder);
    }

    @Test
    public void provideCachedEntities() {
        when(context.getRuleEngineContext()).thenReturn(ruleContextModel);
        when(ruleContextModel.getKieSession()).thenReturn(kieSession);
        when(kieSession.getDroolsKIEBase()).thenReturn(new DroolsKIEBaseModel());

        service.provideCachedEntities(context);
        verify(commerceRuleEngineCache).getCachedFacts(any(DroolsKIEBaseModel.class));
    }
}
