package com.vctek.orderservice.facade.impl;

import com.vctek.orderservice.promotionengine.ruleengine.init.RuleEngineBootstrap;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.service.DroolsKIEModuleService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PromotionKieModuleFacadeTest {
    private PromotionKieModuleFacadeImpl facade;
    @Mock
    private DroolsKIEModuleService droolsKIEModuleService;
    @Mock
    private RuleEngineBootstrap ruleEngineBootstrap;
    private DroolsKIEModuleModel droolsKieModule = new DroolsKIEModuleModel();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        facade = new PromotionKieModuleFacadeImpl(droolsKIEModuleService);
        facade.setRuleEngineBootstrap(ruleEngineBootstrap);
    }

    @Test
    public void init() {
        when(droolsKIEModuleService.findByCompanyId(anyLong())).thenReturn(droolsKieModule);

        facade.init(2l);
        verify(droolsKIEModuleService).save(droolsKieModule);
        assertNotNull(droolsKieModule.getDefaultKIEBase());
        assertNotNull(droolsKieModule.getDefaultKIEBase().getDefaultKieSession());
        verify(ruleEngineBootstrap).startup(any(DroolsKIEModuleModel.class));
    }
}
