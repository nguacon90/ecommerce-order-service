package com.vctek.orderservice.promotionengine.ruleengine.listener.impl;

import com.vctek.orderservice.promotionengine.ruleengine.exception.DroolsRuleLoopException;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.definition.rule.Rule;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.runtime.rule.Match;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

public class RuleMatchCountListenerTest {
    private RuleMatchCountListener listener;

    @Mock
    private AfterMatchFiredEvent event;
    @Mock
    private Match matchValue;
    @Mock
    private Rule rule;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        listener = new RuleMatchCountListener();
    }

    @Test
    public void afterMatchFired() {
        try {
            when(event.getMatch()).thenReturn(matchValue);
            when(matchValue.getRule()).thenReturn(rule);
            listener.afterMatchFired(event);
            fail("must throw exception");
        } catch (DroolsRuleLoopException e) {
            assertNotNull(e.getMessage());
        }
    }
}
