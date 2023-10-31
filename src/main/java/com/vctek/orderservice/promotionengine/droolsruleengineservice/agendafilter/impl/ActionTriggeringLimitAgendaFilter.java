package com.vctek.orderservice.promotionengine.droolsruleengineservice.agendafilter.impl;

import com.vctek.orderservice.promotionengine.ruleengineservice.rrd.RuleConfigurationRRD;
import org.kie.api.runtime.rule.Match;

public class ActionTriggeringLimitAgendaFilter extends AbstractRuleConfigurationAgendaFilter {

    @Override
    public boolean accept(Match match, RuleConfigurationRRD config) {
        Integer maxAllowedRuns = config.getMaxAllowedRuns();
        Integer currentRuns = config.getCurrentRuns();
        return currentRuns == null || maxAllowedRuns == null || currentRuns.compareTo(maxAllowedRuns) < 0;
    }
}
