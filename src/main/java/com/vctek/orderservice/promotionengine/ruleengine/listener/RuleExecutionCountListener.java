package com.vctek.orderservice.promotionengine.ruleengine.listener;

import org.kie.api.event.rule.AgendaEventListener;

public interface RuleExecutionCountListener extends AgendaEventListener {
    void setExecutionLimit(long fireLimit);
}
