package com.vctek.orderservice.promotionengine.droolsruleengineservice.agendafilter.impl;

import com.vctek.orderservice.promotionengine.droolsruleengineservice.agendafilter.AgendaFilterCreationStrategy;
import com.vctek.orderservice.promotionengine.ruleengine.model.AbstractRuleEngineContextModel;
import org.kie.api.runtime.rule.AgendaFilter;
import org.springframework.stereotype.Component;

@Component
public class DefaultActionTriggeringLimitAgendaFilterCreationStrategy implements AgendaFilterCreationStrategy {
    @Override
    public AgendaFilter createAgendaFilter(AbstractRuleEngineContextModel ruleEngineContextModel) {
        return new ActionTriggeringLimitAgendaFilter();
    }
}
