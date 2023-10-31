package com.vctek.orderservice.promotionengine.droolsruleengineservice.agendafilter;

import com.vctek.orderservice.promotionengine.ruleengine.model.AbstractRuleEngineContextModel;
import org.kie.api.runtime.rule.AgendaFilter;

public interface AgendaFilterCreationStrategy {
    AgendaFilter createAgendaFilter(AbstractRuleEngineContextModel ruleEngineContextModel);
}
