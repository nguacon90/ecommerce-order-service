package com.vctek.orderservice.promotionengine.ruleengineservice.rrd;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class RuleGroupExecutionRRDTest {
    private RuleGroupExecutionRRD ruleGroupExecutionRRD;
    @Mock
    private RuleConfigurationRRD ruleConfig;
    private Map<String, Integer> executedRules = new HashMap<>();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ruleGroupExecutionRRD = new RuleGroupExecutionRRD();
        ruleGroupExecutionRRD.setExecutedRules(executedRules);
        when(ruleConfig.getRuleCode()).thenReturn("code");
    }

    @Test
    public void compare2Object() {
        RuleGroupExecutionRRD ruleGroupExecutionRRD1 = new RuleGroupExecutionRRD();
        RuleGroupExecutionRRD ruleGroupExecutionRRD2 = new RuleGroupExecutionRRD();
        ruleGroupExecutionRRD1.setCode("code");
        ruleGroupExecutionRRD1.setExclusive(false);

        ruleGroupExecutionRRD2.setCode("code");
        ruleGroupExecutionRRD2.setExclusive(false);

        assertEquals(ruleGroupExecutionRRD1, ruleGroupExecutionRRD2);
    }

    @Test
    public void allowedToExecute_NotTrackingAction() {
        ruleGroupExecutionRRD.setExecutedRules(null);
        assertTrue(ruleGroupExecutionRRD.allowedToExecute(ruleConfig));
    }

    @Test
    public void allowedToExecute_NotTrackingAction_EmptyMapTracking() {
        assertTrue(ruleGroupExecutionRRD.allowedToExecute(ruleConfig));
    }

    @Test
    public void allowedToExecute_TrackingCurrentExecuteNull_AndNotExclusive_ReturnTrue() {
        executedRules.put("code2", 1);
        assertTrue(ruleGroupExecutionRRD.allowedToExecute(ruleConfig));
    }

    @Test
    public void allowedToExecute_TrackingCurrentExecuteNull_AndExclusive_ReturnFalse() {
        executedRules.put("code2", 1);
        ruleGroupExecutionRRD.setExclusive(true);
        assertFalse(ruleGroupExecutionRRD.allowedToExecute(ruleConfig));
    }

    @Test
    public void allowedToExecute_TrackingCurrentExecute_MaximumConfigIsNull_TrackingTimesLargerThan1() {
        executedRules.put("code", 3);
        when(ruleConfig.getMaxAllowedRuns()).thenReturn(null);
        assertFalse(ruleGroupExecutionRRD.allowedToExecute(ruleConfig));
    }

    @Test
    public void allowedToExecute_TrackingCurrentExecute_RunTimesLargerThanMaximumConfig() {
        executedRules.put("code", 3);
        when(ruleConfig.getMaxAllowedRuns()).thenReturn(1);
        assertFalse(ruleGroupExecutionRRD.allowedToExecute(ruleConfig));
    }

}
