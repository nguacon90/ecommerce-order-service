package com.vctek.orderservice.promotionengine.ruleengine.drools.impl;

import com.google.common.base.Preconditions;
import com.vctek.orderservice.promotionengine.ruleengine.RuleEvaluationContext;
import com.vctek.orderservice.promotionengine.ruleengine.drools.KieSessionHelper;
import com.vctek.orderservice.promotionengine.ruleengine.enums.KIESessionType;
import com.vctek.orderservice.promotionengine.ruleengine.exception.RuleEngineRuntimeException;
import com.vctek.orderservice.promotionengine.ruleengine.init.RuleEngineBootstrap;
import com.vctek.orderservice.promotionengine.ruleengine.listener.RuleExecutionCountListener;
import com.vctek.orderservice.promotionengine.ruleengine.listener.impl.RuleMatchCountListener;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIESessionModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleEngineContextModel;
import org.apache.commons.collections4.CollectionUtils;
import org.drools.core.event.DebugAgendaEventListener;
import org.drools.core.event.DebugRuleRuntimeEventListener;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Objects;

@Component
public class DefaultKieSessionHelper<T> extends DefaultModuleReleaseIdAware implements KieSessionHelper<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultKieSessionHelper.class);
    private Class<? extends RuleExecutionCountListener> ruleExecutionCounterClass;

    public DefaultKieSessionHelper(RuleEngineBootstrap ruleEngineBootstrap) {
        super(ruleEngineBootstrap);
        this.ruleExecutionCounterClass = RuleMatchCountListener.class;
    }

    @Override
    public T initializeSession(Class<T> kieSessionClass, RuleEvaluationContext context, KieContainer kieContainer) {
        this.assertKieSessionClass(kieSessionClass);
        DroolsRuleEngineContextModel ruleEngineContext = this.validateRuleEvaluationContext(context);
        return KieSession.class.isAssignableFrom(kieSessionClass) ?
                (T) this.initializeKieSessionInternal(context, ruleEngineContext, kieContainer) :
                (T) this.initializeStatelessKieSessionInternal(context, ruleEngineContext, kieContainer);
    }

    protected KieSession initializeKieSessionInternal(RuleEvaluationContext context, DroolsRuleEngineContextModel ruleEngineContext, KieContainer kieContainer) {
        DroolsKIESessionModel kieSession = ruleEngineContext.getKieSession();
        this.assertSessionIsStateful(kieSession);
        KieSession session = kieContainer.newKieSession(kieSession.getName());
        if (Objects.nonNull(context.getGlobals())) {
            context.getGlobals().forEach(session::setGlobal);
        }

        this.registerKieSessionListeners(context, session, ruleEngineContext.getRuleFiringLimit());
        return session;
    }

    protected StatelessKieSession initializeStatelessKieSessionInternal(RuleEvaluationContext context, DroolsRuleEngineContextModel ruleEngineContext, KieContainer kieContainer) {
        DroolsKIESessionModel kieSession = ruleEngineContext.getKieSession();
        this.assertSessionIsStateless(kieSession);
        StatelessKieSession session = kieContainer.newStatelessKieSession(kieSession.getName());
        if (Objects.nonNull(context.getGlobals())) {
            context.getGlobals().forEach(session::setGlobal);
        }

        this.registerStatelessKieSessionListeners(context, session, ruleEngineContext.getRuleFiringLimit());
        return session;
    }

    protected void assertKieSessionClass(Class<T> kieSessionClass) {
        Preconditions.checkArgument(KieSession.class.isAssignableFrom(kieSessionClass) || StatelessKieSession.class.isAssignableFrom(kieSessionClass), "No other session types other than KieSession and StatelessKieSession are supported");
    }

    protected void assertSessionIsStateless(DroolsKIESessionModel kieSession) {
        Preconditions.checkArgument(kieSession.getSessionType().equals(KIESessionType.STATELESS.toString()),
                "Expected STATELESS session type here. Check the invocation parameters");
    }

    protected void assertSessionIsStateful(DroolsKIESessionModel kieSession) {
        Preconditions.checkArgument(kieSession.getSessionType().equals(KIESessionType.STATEFUL.toString()),
                "Expected STATEFUL session type here. Check the invocation parameters");
    }

    protected void registerKieSessionListeners(RuleEvaluationContext context, KieSession session, Long maximumExecutions) {

        if (CollectionUtils.isNotEmpty(context.getEventListeners())) {
            Iterator var5 = context.getEventListeners().iterator();

            while(var5.hasNext()) {
                Object listener = var5.next();
                if (listener instanceof AgendaEventListener) {
                    session.addEventListener((AgendaEventListener)listener);
                } else if (listener instanceof RuleRuntimeEventListener) {
                    session.addEventListener((RuleRuntimeEventListener)listener);
                } else {
                    if (!(listener instanceof ProcessEventListener)) {
                        throw new IllegalArgumentException("context.eventListeners attribute must only contain " +
                                "instances of the types org.kie.api.event.rule.AgendaEventListener, " +
                                "org.kie.api.event.process.ProcessEventListener or " +
                                "org.kie.api.event.rule.RuleRuntimeEventListener");
                    }

                    session.addEventListener((ProcessEventListener)listener);
                }
            }
        }

        if (Objects.nonNull(this.getRuleExecutionCounterClass()) && Objects.nonNull(maximumExecutions)) {
            RuleExecutionCountListener listener = this.createRuleExecutionCounterListener();
            listener.setExecutionLimit(maximumExecutions);
            session.addEventListener(listener);
        }

        if (LOGGER.isDebugEnabled()) {
            session.addEventListener(new DebugRuleRuntimeEventListener());
            session.addEventListener(new DebugAgendaEventListener());
        }

    }

    protected void registerStatelessKieSessionListeners(RuleEvaluationContext context, StatelessKieSession session,
                                                        Long maximumExecutions) {
        if (CollectionUtils.isNotEmpty(context.getEventListeners())) {
            Iterator var5 = context.getEventListeners().iterator();

            while(var5.hasNext()) {
                Object listener = var5.next();
                if (listener instanceof AgendaEventListener) {
                    session.addEventListener((AgendaEventListener)listener);
                } else if (listener instanceof RuleRuntimeEventListener) {
                    session.addEventListener((RuleRuntimeEventListener)listener);
                } else {
                    if (!(listener instanceof ProcessEventListener)) {
                        throw new IllegalArgumentException("context.eventListeners attribute must only contain " +
                                "instances of the types org.kie.api.event.rule.AgendaEventListener, " +
                                "org.kie.api.event.process.ProcessEventListener or " +
                                "org.kie.api.event.rule.RuleRuntimeEventListener");
                    }

                    session.addEventListener((ProcessEventListener)listener);
                }
            }
        }

        if (Objects.nonNull(this.getRuleExecutionCounterClass()) && Objects.nonNull(maximumExecutions)) {
            RuleExecutionCountListener listener = this.createRuleExecutionCounterListener();
            listener.setExecutionLimit(maximumExecutions);
            session.addEventListener(listener);
        }

        if (LOGGER.isDebugEnabled()) {
            session.addEventListener(new DebugRuleRuntimeEventListener());
            session.addEventListener(new DebugAgendaEventListener());
        }

    }

    protected RuleExecutionCountListener createRuleExecutionCounterListener() {
        try {
            return this.getRuleExecutionCounterClass().newInstance();
        } catch (IllegalAccessException | InstantiationException var2) {
            throw new RuleEngineRuntimeException(var2);
        }
    }

    protected Class<? extends RuleExecutionCountListener> getRuleExecutionCounterClass() {
        return this.ruleExecutionCounterClass;
    }

}
