package com.vctek.orderservice.promotionengine.ruleengine.service.impl;

import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEBaseModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.repository.DroolsKIEModuleRepository;
import com.vctek.orderservice.promotionengine.ruleengine.repository.DroolsRuleRepository;
import com.vctek.orderservice.promotionengine.ruleengine.versioning.ModuleVersionResolver;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class DroolsRuleServiceTest {
    private DroolsRuleServiceImpl service;

    @Mock
    private DroolsRuleRepository droolsRuleRepository;
    @Mock
    private DroolsKIEModuleRepository droolsKIEModuleRepository;
    @Mock
    private ModuleVersionResolver<DroolsKIEModuleModel> moduleVersionResolver;
    @Mock
    private DroolsRuleModel droolsRule;
    @Mock
    private DroolsKIEBaseModel kieBase;
    @Mock
    private DroolsKIEModuleModel kieModule;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new DroolsRuleServiceImpl(droolsRuleRepository);
        service.setDroolsKIEModuleRepository(droolsKIEModuleRepository);
        service.setModuleVersionResolver(moduleVersionResolver);

        when(droolsRule.getKieBase()).thenReturn(kieBase);
        when(kieBase.getDroolsKIEModule()).thenReturn(kieModule);
        when(droolsRule.getVersion()).thenReturn(0l);
    }

    @Test
    public void getRuleForCodeAndModule() {
        service.getRuleForCodeAndModule("code", "kie-module");
        verify(droolsRuleRepository).findByCodeAndModuleName(anyString(), anyString());
    }

    @Test
    public void save_RuleEngineVersionNotSmallerThanDroolVersion() {
        when(kieModule.getVersion()).thenReturn(1l);
        service.save(droolsRule);
        verify(kieModule, times(0)).setVersion(anyLong());
        verify(droolsRuleRepository).save(droolsRule);
    }

    @Test
    public void save_RuleEngineVersionSmallerThanDroolVersion_ShouldUpdateToRuleEngineVersion() {
        when(droolsRule.getVersion()).thenReturn(2l);
        when(kieModule.getVersion()).thenReturn(1l);
        service.save(droolsRule);
        verify(kieModule).setVersion(2l);
        verify(droolsRuleRepository).save(droolsRule);
    }

    @Test
    public void findByCodeAndModuleNameAndActive() {
        service.findByCodeAndModuleNameAndActive("code", "kie-module", true);
        verify(droolsRuleRepository).findByCodeAndModuleNameAndActive(anyString(), anyString(), anyBoolean());
    }

    @Test
    public void getDeployedEngineRulesForModule() {
        when(droolsKIEModuleRepository.findByName(anyString())).thenReturn(kieModule);
        when(moduleVersionResolver.getDeployedModuleVersion(kieModule)).thenReturn(Optional.of(0l));

        service.getDeployedEngineRulesForModule("kie-module");
        verify(droolsRuleRepository).getActiveRulesForVersion(anyString(), anyLong());
    }

}
