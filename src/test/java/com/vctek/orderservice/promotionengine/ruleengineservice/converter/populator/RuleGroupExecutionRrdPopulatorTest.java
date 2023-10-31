package com.vctek.orderservice.promotionengine.ruleengineservice.converter.populator;

import com.vctek.orderservice.promotionengine.ruleengine.model.AbstractRuleEngineRuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.rrd.RuleGroupExecutionRRD;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

public class RuleGroupExecutionRrdPopulatorTest {
    private RuleGroupExecutionRRDPopulator populator;

    @Mock
    private AbstractRuleEngineRuleModel source;

    @Mock
    private RuleGroupExecutionRRD target;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        populator = new RuleGroupExecutionRRDPopulator();
    }

    @Test
    public void populate() {
        populator.populate(source, target);
        verify(target).setCode(anyString());
    }
}
