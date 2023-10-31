package com.vctek.orderservice.promotionengine.droolsruleengineservice.agendafilter.impl;

import com.vctek.orderservice.promotionengine.droolsruleengineservice.agendafilter.AgendaFilterCreationStrategy;
import com.vctek.orderservice.promotionengine.droolsruleengineservice.agendafilter.AgendaFilterFactory;
import com.vctek.orderservice.promotionengine.droolsruleengineservice.agendafilter.CompoundAgendaFilter;
import com.vctek.orderservice.promotionengine.ruleengine.model.AbstractRuleEngineContextModel;
import org.apache.commons.collections4.CollectionUtils;
import org.kie.api.runtime.rule.AgendaFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class DefaultAgendaFilterFactory implements AgendaFilterFactory {
    private List<AgendaFilterCreationStrategy> strategies;
    private boolean forceAllEvaluations = false;

    @Override
    public AgendaFilter createAgendaFilter(AbstractRuleEngineContextModel context) {
        if (CollectionUtils.isEmpty(this.strategies)) {
            return null;
        }
        List<AgendaFilter> agendaFilters = new ArrayList();
        Iterator var4 = this.strategies.iterator();

        while (var4.hasNext()) {
            AgendaFilterCreationStrategy strategy = (AgendaFilterCreationStrategy) var4.next();
            agendaFilters.add(strategy.createAgendaFilter(context));
        }

        CompoundAgendaFilter result = new DefaultCompoundAgendaFilter();
        result.setAgendaFilters(agendaFilters);
        result.setForceAllEvaluations(this.forceAllEvaluations);
        return result;
    }

    @Autowired
    @Qualifier("agendaFilterStrategies")
    public void setStrategies(List<AgendaFilterCreationStrategy> strategies) {
        this.strategies = strategies;
    }

    public void setForceAllEvaluations(boolean forceAllEvaluations) {
        this.forceAllEvaluations = forceAllEvaluations;
    }
}
