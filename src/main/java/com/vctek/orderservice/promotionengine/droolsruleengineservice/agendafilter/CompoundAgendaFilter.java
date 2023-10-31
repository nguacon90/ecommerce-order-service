package com.vctek.orderservice.promotionengine.droolsruleengineservice.agendafilter;

import org.kie.api.runtime.rule.AgendaFilter;

import java.util.List;

public interface CompoundAgendaFilter extends AgendaFilter {
    void setForceAllEvaluations(boolean forceAllEvaluations);

    void setAgendaFilters(List<AgendaFilter> agendaFilters);
}
