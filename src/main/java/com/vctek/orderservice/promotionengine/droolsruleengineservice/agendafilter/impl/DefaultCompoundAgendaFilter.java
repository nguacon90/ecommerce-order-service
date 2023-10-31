package com.vctek.orderservice.promotionengine.droolsruleengineservice.agendafilter.impl;

import com.vctek.orderservice.promotionengine.droolsruleengineservice.agendafilter.CompoundAgendaFilter;
import org.kie.api.runtime.rule.AgendaFilter;
import org.kie.api.runtime.rule.Match;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DefaultCompoundAgendaFilter implements CompoundAgendaFilter {
    private List<AgendaFilter> agendaFilters = new ArrayList();
    private boolean forceAllEvaluations = false;

    public boolean accept(Match match) {
        boolean result = true;
        Iterator var4 = this.getAgendaFilters().iterator();

        while(var4.hasNext()) {
            AgendaFilter agendaFilter = (AgendaFilter)var4.next();
            result &= agendaFilter.accept(match);
            if (!result && !this.isForceAllEvaluations()) {
                break;
            }
        }

        return result;
    }

    protected List<AgendaFilter> getAgendaFilters() {
        return this.agendaFilters;
    }

    protected boolean isForceAllEvaluations() {
        return this.forceAllEvaluations;
    }

    @Override
    public void setForceAllEvaluations(boolean forceAllEvaluations) {
        this.forceAllEvaluations = forceAllEvaluations;
    }

    @Override
    public void setAgendaFilters(List<AgendaFilter> agendaFilters) {
        this.agendaFilters = agendaFilters;
    }
}
