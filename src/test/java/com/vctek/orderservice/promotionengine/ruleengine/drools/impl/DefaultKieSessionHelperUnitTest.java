package com.vctek.orderservice.promotionengine.ruleengine.drools.impl;

import com.vctek.orderservice.promotionengine.ruleengine.RuleEvaluationContext;
import com.vctek.orderservice.promotionengine.ruleengine.enums.KIESessionType;
import com.vctek.orderservice.promotionengine.ruleengine.init.impl.DefaultRuleEngineBootstrap;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEBaseModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIESessionModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleEngineContextModel;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.builder.ReleaseId;
import org.kie.api.event.rule.DefaultAgendaEventListener;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class DefaultKieSessionHelperUnitTest {
    private static final String TEST_SESSION_NAME = "TEST_SESSION_NAME";
    private DefaultKieSessionHelper kieSessionHelper;

    @Mock
    private DefaultRuleEngineBootstrap bootstrap;
    @Mock
    private RuleEvaluationContext context;
    @Mock
    private DroolsRuleEngineContextModel ruleEngineContext;
    @Mock
    private KieContainer kieContainer;
    @Mock
    private DroolsKIESessionModel droolsKieSession;
    @Mock
    private KieSession kieSession;
    @Mock
    private StatelessKieSession statelessKieSession;
    private Set<Object> eventListeners;

    @Mock
    private DroolsKIEBaseModel kieBase;
    @Mock
    private DroolsKIEModuleModel droolsModule;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        eventListeners = new HashSet<>();
        eventListeners.add(new DefaultAgendaEventListener());
        this.kieSessionHelper = new DefaultKieSessionHelper(bootstrap);
        when(this.context.getRuleEngineContext()).thenReturn(this.ruleEngineContext);
        when(context.getEventListeners()).thenReturn(eventListeners);
        when(this.ruleEngineContext.getKieSession()).thenReturn(this.droolsKieSession);
        when(droolsKieSession.getDroolsKIEBase()).thenReturn(kieBase);
        when(kieBase.getDroolsKIEModule()).thenReturn(droolsModule);
        when(this.kieContainer.newKieSession(anyString())).thenReturn(this.kieSession);
        when(this.kieContainer.newStatelessKieSession(anyString())).thenReturn(this.statelessKieSession);
        when(this.droolsKieSession.getName()).thenReturn(TEST_SESSION_NAME);
    }

    @Test
    public void testInitializeKieSessionInternalWrongType() {
        when(this.droolsKieSession.getSessionType()).thenReturn(KIESessionType.STATELESS.toString());
        Assertions.assertThatThrownBy(() -> {
            this.kieSessionHelper.initializeKieSessionInternal(this.context, this.ruleEngineContext, this.kieContainer);
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testInitializeStatelessKieSessionInternalWrongType() {
        when(this.droolsKieSession.getSessionType()).thenReturn(KIESessionType.STATEFUL.toString());
        Assertions.assertThatThrownBy(() -> {
            this.kieSessionHelper.initializeStatelessKieSessionInternal(this.context, this.ruleEngineContext, this.kieContainer);
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testInitializeKieSessionInternalOk() {
        when(this.droolsKieSession.getSessionType()).thenReturn(KIESessionType.STATEFUL.toString());
        Object session = this.kieSessionHelper.initializeKieSessionInternal(this.context, this.ruleEngineContext, this.kieContainer);
        Assertions.assertThat(session).isInstanceOf(KieSession.class);
        Mockito.verify(this.kieContainer).newKieSession(TEST_SESSION_NAME);
    }

    @Test
    public void testInitializeStatelessKieSessionInternalOk() {
        when(this.droolsKieSession.getSessionType()).thenReturn(KIESessionType.STATELESS.toString());
        Object session = this.kieSessionHelper.initializeStatelessKieSessionInternal(this.context, this.ruleEngineContext, this.kieContainer);
        Assertions.assertThat(session).isInstanceOf(StatelessKieSession.class);
        Mockito.verify(this.kieContainer).newStatelessKieSession(TEST_SESSION_NAME);
    }

    @Test
    public void testInitializeKieSessionStatefulOk() {
        when(this.droolsKieSession.getSessionType()).thenReturn(KIESessionType.STATEFUL.toString());
        Object session = this.kieSessionHelper.initializeSession(KieSession.class, this.context, this.kieContainer);
        Assertions.assertThat(session).isInstanceOf(KieSession.class);
        Mockito.verify(this.kieContainer).newKieSession(TEST_SESSION_NAME);
    }

    @Test
    public void testInitializeKieSessionStatelessOk() {
        when(this.droolsKieSession.getSessionType()).thenReturn(KIESessionType.STATELESS.toString());
        Object session = this.kieSessionHelper.initializeSession(StatelessKieSession.class, this.context, this.kieContainer);
        Assertions.assertThat(session).isInstanceOf(StatelessKieSession.class);
        Mockito.verify(this.kieContainer).newStatelessKieSession(TEST_SESSION_NAME);
    }

    @Test
    public void getDummyReleaseId() {
        ReleaseId releaseId = kieSessionHelper.getDeployedKieModuleReleaseId(context);
        assertEquals(DefaultModuleReleaseIdAware.DUMMY_GROUP, releaseId.getGroupId());
    }
}
