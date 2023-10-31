package com.vctek.orderservice.promotionengine.ruleengine;


import com.vctek.orderservice.promotionengine.ruleengine.model.AbstractRuleEngineContextModel;

import java.util.Map;
import java.util.Set;

public class RuleEvaluationContext {
    private AbstractRuleEngineContextModel ruleEngineContext;
    private Set<Object> facts;
    private Map<String,Object> globals;
    private Object filter;
    private Set<Object> eventListeners;

    public AbstractRuleEngineContextModel getRuleEngineContext() {
        return ruleEngineContext;
    }

    public void setRuleEngineContext(AbstractRuleEngineContextModel ruleEngineContext) {
        this.ruleEngineContext = ruleEngineContext;
    }

    public Set<Object> getFacts() {
        return facts;
    }

    public void setFacts(Set<Object> facts) {
        this.facts = facts;
    }

    public Map<String, Object> getGlobals() {
        return globals;
    }

    public void setGlobals(Map<String, Object> globals) {
        this.globals = globals;
    }

    public Object getFilter() {
        return filter;
    }

    public void setFilter(Object filter) {
        this.filter = filter;
    }

    public Set<Object> getEventListeners() {
        return eventListeners;
    }

    public void setEventListeners(Set<Object> eventListeners) {
        this.eventListeners = eventListeners;
    }
}
