package com.vctek.orderservice.promotionengine.droolsruleengineservice.service;

import com.vctek.orderservice.promotionengine.droolsruleengineservice.agendafilter.AgendaFilterFactory;
import com.vctek.orderservice.promotionengine.ruleengine.RuleEvaluationContext;
import com.vctek.orderservice.promotionengine.ruleengine.RuleEvaluationResult;
import com.vctek.orderservice.promotionengine.ruleengine.model.AbstractRuleEngineContextModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleEngineContextModel;
import com.vctek.orderservice.promotionengine.ruleengine.service.RuleEngineService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DroolsRuleEngineServiceTest {
    @Mock
    private RuleEngineService platformRuleEngineService;

    @Mock
    private AgendaFilterFactory agendaFilterFactory;

    private DroolsRuleEngineService service;
    private RuleEvaluationContext context;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        context = new RuleEvaluationContext();
        service = new DroolsRuleEngineService(platformRuleEngineService, agendaFilterFactory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void evaluate_nullContext() {
        service.evaluate(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void evaluate_NotDroolsRuleEngineContextModel() {
        context.setRuleEngineContext(new AbstractRuleEngineContextModel());
        service.evaluate(context);
    }

    @Test
    public void evaluate_ThrowRuntimeException() {
        context.setRuleEngineContext(new DroolsRuleEngineContextModel());
        when(platformRuleEngineService.evaluate(context)).thenThrow(new RuntimeException());
        RuleEvaluationResult result = service.evaluate(context);
        assertTrue(result.isEvaluationFailed());
    }

    @Test
    public void evaluate() {
        context.setRuleEngineContext(new DroolsRuleEngineContextModel());
        when(platformRuleEngineService.evaluate(context)).thenReturn(new RuleEvaluationResult());
        service.evaluate(context);
        verify(platformRuleEngineService).evaluate(context);
    }
}
