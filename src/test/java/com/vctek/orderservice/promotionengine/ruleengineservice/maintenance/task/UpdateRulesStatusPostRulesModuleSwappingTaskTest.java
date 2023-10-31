package com.vctek.orderservice.promotionengine.ruleengineservice.maintenance.task;

import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionSourceRuleService;
import com.vctek.orderservice.promotionengine.ruleengine.RuleEngineActionResult;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.service.DroolsRuleService;
import com.vctek.orderservice.promotionengine.ruleengine.versioning.ModuleVersionResolver;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleStatus;
import com.vctek.orderservice.service.ModelService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.*;

public class UpdateRulesStatusPostRulesModuleSwappingTaskTest {
    private UpdateRulesStatusPostRulesModuleSwappingTask postRulesModuleSwappingTask;
    private List<DroolsRuleModel> drools = new ArrayList<>();
    @Mock
    private ModuleVersionResolver<DroolsKIEModuleModel> moduleVersionResolver;
    @Mock
    private DroolsRuleService droolsRuleService;
    @Mock
    private PromotionSourceRuleService promotionSourceRuleService;
    @Mock
    private ModelService modelService;
    @Mock
    private RuleEngineActionResult result;
    @Mock
    private DroolsRuleModel drool;
    @Mock
    private PromotionSourceRuleModel sourceRule;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        postRulesModuleSwappingTask = new UpdateRulesStatusPostRulesModuleSwappingTask();
        postRulesModuleSwappingTask.setPromotionSourceRuleService(promotionSourceRuleService);
        when(drool.getCode()).thenReturn("promotionsourcerule_code");
        drools.add(drool);
    }

    @Test
    public void execute_resultFaile() {
        when(result.isActionFailed()).thenReturn(true);
        assertFalse(postRulesModuleSwappingTask.execute(result));
        verify(modelService, times(0)).saveAll(anyList());
    }

    @Test
    public void execute() {
        when(result.isActionFailed()).thenReturn(false);
        when(result.getModuleName()).thenReturn("promotion-module");
        when(result.getDeployedVersion()).thenReturn("1.0.0");
        when(moduleVersionResolver.extractModuleVersion(anyString(), anyString())).thenReturn(22l);
        when(droolsRuleService.getRulesForVersion(anyString(), anyLong())).thenReturn(drools);
        when(promotionSourceRuleService.findByCode(anyString())).thenReturn(sourceRule);

        assertTrue(postRulesModuleSwappingTask.execute(result));
        verify(promotionSourceRuleService).updateAllActiveRuleStatus(anyString(), eq(RuleStatus.PUBLISHED));
        verify(promotionSourceRuleService).updateAllExpiredRuleToInActive(anyString());
    }
}
