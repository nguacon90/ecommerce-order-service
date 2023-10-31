package com.vctek.orderservice.promotionengine.droolsruleengineservice.agendafilter.impl;

import com.vctek.orderservice.promotionengine.ruleengineservice.rrd.RuleConfigurationRRD;
import org.apache.commons.lang3.StringUtils;
import org.drools.core.common.InternalFactHandle;
import org.kie.api.definition.KieDefinition;
import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.rule.AgendaFilter;
import org.kie.api.runtime.rule.Match;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractRuleConfigurationAgendaFilter implements AgendaFilter {
    protected Optional<RuleConfigurationRRD> getRuleConfig(Match match) {
        Rule rule = match.getRule();
        if (rule.getKnowledgeType() != KieDefinition.KnowledgeType.RULE) {
            return Optional.empty();
        }
        List<RuleConfigurationRRD> ruleConfigs = match.getFactHandles().stream()
                .filter((fact) -> fact instanceof InternalFactHandle)
                .map(fact -> ((InternalFactHandle) fact).getObject())
                .filter(fact -> fact instanceof RuleConfigurationRRD)
                .map(fact -> (RuleConfigurationRRD) fact)
                .collect(Collectors.toList());
        if (ruleConfigs.isEmpty()) {
            return Optional.empty();
        }

        String ruleCode = (String) rule.getMetaData().get("ruleCode");
        if (StringUtils.isEmpty(ruleCode)) {
            throw new IllegalStateException("Misconfigured rule: @ruleCode is not set or empty for drools rule:"
                    + rule.getName() + " in package:" + rule.getPackageName());
        }

        return ruleConfigs.stream().filter((config) -> ruleCode.equals(config.getRuleCode())).findFirst();

    }

    public boolean accept(Match match) {
        Optional<RuleConfigurationRRD> option = this.getRuleConfig(match);
        return option.isPresent() ? this.accept(match, option.get()) : true;
    }

    protected abstract boolean accept(Match match, RuleConfigurationRRD ruleConfigurationRRD);
}
