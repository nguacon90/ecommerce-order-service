package com.vctek.orderservice.promotionengine.ruleengine.listener.impl;

import com.google.common.util.concurrent.AtomicLongMap;
import com.vctek.orderservice.promotionengine.ruleengine.exception.DroolsRuleLoopException;
import com.vctek.orderservice.promotionengine.ruleengine.listener.RuleExecutionCountListener;
import org.drools.core.event.DefaultAgendaEventListener;
import org.kie.api.definition.rule.Rule;
import org.kie.api.event.rule.AfterMatchFiredEvent;

public class RuleMatchCountListener extends DefaultAgendaEventListener implements RuleExecutionCountListener {
    private final AtomicLongMap<Rule> map = AtomicLongMap.create();
    private long executionLimit = 0L;

    public void afterMatchFired(AfterMatchFiredEvent event) {
        long currentCount = this.map.addAndGet(event.getMatch().getRule(), 1L);
        if (currentCount > this.executionLimit) {
            throw new DroolsRuleLoopException(this.executionLimit, this.map.asMap());
        }
    }

    @Override
    public void setExecutionLimit(long max) {
        this.executionLimit = max;
    }

    protected long getExecutionLimit() {
        return this.executionLimit;
    }
}
