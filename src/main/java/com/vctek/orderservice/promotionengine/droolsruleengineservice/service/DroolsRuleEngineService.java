package com.vctek.orderservice.promotionengine.droolsruleengineservice.service;

import com.vctek.orderservice.promotionengine.droolsruleengineservice.agendafilter.AgendaFilterFactory;
import com.vctek.orderservice.promotionengine.ruleengine.ExecutionContext;
import com.vctek.orderservice.promotionengine.ruleengine.RuleEvaluationContext;
import com.vctek.orderservice.promotionengine.ruleengine.RuleEvaluationResult;
import com.vctek.orderservice.promotionengine.ruleengine.exception.DroolsRuleLoopException;
import com.vctek.orderservice.promotionengine.ruleengine.init.InitializationFuture;
import com.vctek.orderservice.promotionengine.ruleengine.listener.impl.RuleMatchCountListener;
import com.vctek.orderservice.promotionengine.ruleengine.model.AbstractRuleEngineContextModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.AbstractRuleModuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleEngineContextModel;
import com.vctek.orderservice.promotionengine.ruleengine.service.RuleEngineService;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.RuleEngineResultRAO;
import org.kie.api.runtime.rule.AgendaFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("droolsRuleEngineService")
public class DroolsRuleEngineService implements RuleEngineService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DroolsRuleEngineService.class);
    public static final String NOT_SUPPORT_RE_CONTEXT_ERR = "ruleEngineContext %s is not a DroolsRuleEngineContext, not supported.";
    private RuleEngineService platformRuleEngineService;
    private AgendaFilterFactory agendaFilterFactory;

    public DroolsRuleEngineService(@Qualifier("platformRuleEngineService") RuleEngineService platformRuleEngineService,
                                   AgendaFilterFactory agendaFilterFactory) {
        this.platformRuleEngineService = platformRuleEngineService;
        this.agendaFilterFactory = agendaFilterFactory;
    }

    private void validate(RuleEvaluationContext context) {
        if(context == null) {
            throw new IllegalArgumentException("Parameter context can not be null");
        }
        AbstractRuleEngineContextModel abstractREContext = context.getRuleEngineContext();
        if(!(abstractREContext instanceof DroolsRuleEngineContextModel)) {
            String errorMessage = String.format(NOT_SUPPORT_RE_CONTEXT_ERR,
                    abstractREContext.getName(), abstractREContext.getName());
            throw new IllegalArgumentException(errorMessage);
        }
    }

    @Override
    public RuleEvaluationResult evaluate(RuleEvaluationContext context) {
        this.validate(context);
        DroolsRuleEngineContextModel ruleEngineContext = (DroolsRuleEngineContextModel) context.getRuleEngineContext();
        try {
            RuleEngineResultRAO rao = this.addRuleEngineResultRAO(context);
            AgendaFilter agendaFilter = this.agendaFilterFactory.createAgendaFilter(ruleEngineContext);
            context.setFilter(agendaFilter);
            Set<Object> eventListeners = this.getEventListeners(ruleEngineContext);
            context.setEventListeners(eventListeners);
            RuleEvaluationResult result = this.platformRuleEngineService.evaluate(context);
            result.setResult(rao);
            return result;
        } catch (DroolsRuleLoopException var8) {
            LOGGER.error(var8.getMessage());
            throw var8;
        } catch (RuntimeException var9) {
            String errorMessage = String.format("Rule evaluation failed with message '%s' for facts: %s.",
                    var9.getMessage(),Arrays.toString(context.getFacts() != null ? context.getFacts().toArray() : null));
            LOGGER.error(errorMessage, var9);
            RuleEvaluationResult result = new RuleEvaluationResult();
            result.setEvaluationFailed(true);
            result.setErrorMessage(errorMessage);
            result.setFacts(context.getFacts());
            return result;
        }
    }

    @Override
    public InitializationFuture initialize(List<AbstractRuleModuleModel> modules, boolean enableIncrementalUpdate, ExecutionContext executionContext) {
        return this.platformRuleEngineService.initialize(modules, enableIncrementalUpdate, executionContext);
    }

    private Set<Object> getEventListeners(DroolsRuleEngineContextModel ruleEngineContext) {
        Set<Object> listeners = new LinkedHashSet();
        Long firingLimit = ruleEngineContext.getRuleFiringLimit();
        if (firingLimit != null) {
            RuleMatchCountListener listener = new RuleMatchCountListener();
            listener.setExecutionLimit(firingLimit);
            listeners.add(listener);
        }
        return listeners;
    }

    protected RuleEngineResultRAO addRuleEngineResultRAO(RuleEvaluationContext context) {
        RuleEngineResultRAO rao = null;
        LinkedHashSet facts;
        if (context.getFacts() == null) {
            facts = new LinkedHashSet();
            context.setFacts(facts);
        }

        Iterator var4 = context.getFacts().iterator();

        while(var4.hasNext()) {
            Object fact = var4.next();
            if (fact instanceof RuleEngineResultRAO) {
                rao = (RuleEngineResultRAO)fact;
            }
        }

        if (rao == null) {
            rao = new RuleEngineResultRAO();
            rao.setActions(new LinkedHashSet());
            facts = new LinkedHashSet(context.getFacts());
            facts.add(rao);
            context.setFacts(facts);
        }

        return rao;
    }

}
