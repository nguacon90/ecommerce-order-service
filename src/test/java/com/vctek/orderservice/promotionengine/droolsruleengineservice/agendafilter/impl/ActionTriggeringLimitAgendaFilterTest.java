package com.vctek.orderservice.promotionengine.droolsruleengineservice.agendafilter.impl;

import com.vctek.orderservice.promotionengine.ruleengineservice.rrd.RuleConfigurationRRD;
import org.junit.Test;
import org.kie.api.runtime.rule.Match;

import static java.lang.Integer.valueOf;
import static org.assertj.core.api.Assertions.assertThat;


public class ActionTriggeringLimitAgendaFilterTest
{
	private ActionTriggeringLimitAgendaFilter actionTriggeringLimitAgendaFilter = new ActionTriggeringLimitAgendaFilter();
	private final Match match = null;

	@Test
	public void shouldAcceptWhenMaxExecutionThresholdIsNotReached() throws Exception
	{
		//given
		final RuleConfigurationRRD config = createRuleConfigurationRRD(1, 2);
		//when
		final boolean result = actionTriggeringLimitAgendaFilter.accept(match, config);
		//then
		assertThat(result).isTrue();
	}

	protected RuleConfigurationRRD createRuleConfigurationRRD(final int currentRuns, final int maxAllowedRuns)
	{
		final RuleConfigurationRRD config = new RuleConfigurationRRD();
		config.setMaxAllowedRuns(valueOf(maxAllowedRuns));
		config.setCurrentRuns(valueOf(currentRuns));
		return config;
	}

	@Test
	public void shouldRejectWhenMaxExecutionThresholdIsEqualCurrentRuns() throws Exception
	{
		//given
		final RuleConfigurationRRD config = createRuleConfigurationRRD(2, 2);
		//when
		final boolean result = actionTriggeringLimitAgendaFilter.accept(match, config);
		//then
		assertThat(result).isFalse();
	}

	@Test
	public void shouldRejectWhenMaxExecutionThresholdIsReached() throws Exception
	{
		//given
		final RuleConfigurationRRD config = createRuleConfigurationRRD(3, 2);
		//when
		final boolean result = actionTriggeringLimitAgendaFilter.accept(match, config);
		//then
		assertThat(result).isFalse();
	}
}
