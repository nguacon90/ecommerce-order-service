package com.vctek.orderservice.promotionengine.ruleengine.cache.impl;

import com.vctek.orderservice.promotionengine.ruleengine.RuleEvaluationContext;
import com.vctek.orderservice.promotionengine.ruleengine.cache.KIEModuleCacheBuilder;
import com.vctek.orderservice.promotionengine.ruleengine.cache.RuleEngineCache;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEBaseModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIESessionModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleEngineContextModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

public class DefaultRuleEngineCacheServiceTest {

    @Mock
    protected RuleEngineCache ruleEngineCache;

    private DefaultRuleEngineCacheService service;
    private RuleEvaluationContext context;

    @Before
    public void setUp() {
        context = new RuleEvaluationContext();
        DroolsRuleEngineContextModel ruleEngineContext = new DroolsRuleEngineContextModel();
        context.setRuleEngineContext(ruleEngineContext);
        DroolsKIESessionModel kieSession = new DroolsKIESessionModel();
        kieSession.setDroolsKIEBase(new DroolsKIEBaseModel());
        ruleEngineContext.setKieSession(kieSession);

        MockitoAnnotations.initMocks(this);
        service = new DefaultRuleEngineCacheService(ruleEngineCache);
    }

    @Test
    public void provideCachedEntities() {
        service.provideCachedEntities(context);
        verify(ruleEngineCache).getGlobalsForKIEBase(any(DroolsKIEBaseModel.class));
    }

    @Test
    public void createKIEModuleCacheBuilder() {
        DroolsKIEModuleModel module = new DroolsKIEModuleModel();
        service.createKIEModuleCacheBuilder(module);
        verify(ruleEngineCache).createKIEModuleCacheBuilder(module);
    }

    @Test
    public void addToCache() {
        DroolsKIEModuleModel module = new DroolsKIEModuleModel();
        KIEModuleCacheBuilder cache = new DefaultKIEModuleCacheBuilder(null, module, false);
        service.addToCache(cache);
        verify(ruleEngineCache).addKIEModuleCache(cache);
    }
}
