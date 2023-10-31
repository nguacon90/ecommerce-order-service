package com.vctek.orderservice.promotionengine.droolsruleengineservice.agendafilter.impl;

import com.vctek.orderservice.promotionengine.droolsruleengineservice.agendafilter.AgendaFilterCreationStrategy;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleEngineContextModel;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.rule.AgendaFilter;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;


public class DefaultAgendaFilterFactoryTest
{

	private DefaultAgendaFilterFactory factory;


	@Mock
	private DroolsRuleEngineContextModel context;

	@Mock
	private AgendaFilterCreationStrategy strategy;

	@Mock
	private AgendaFilter filter;


	@Before
	public void setUp()
	{
		MockitoAnnotations.initMocks(this);
		factory = new DefaultAgendaFilterFactory();
		factory.setForceAllEvaluations(false);
		factory.setStrategies(Collections.singletonList(strategy));
		when(strategy.createAgendaFilter(context)).thenReturn(filter);
	}

	@Test
	public void testNoStrategiesSet()
	{
		factory.setStrategies(Collections.EMPTY_LIST);
		assertNull(factory.createAgendaFilter(context));
		factory.setStrategies(null);
		assertNull(factory.createAgendaFilter(context));
	}

	@Test
	public void testCreateAgendaFilter()
	{
		final AgendaFilter resultFilter = factory.createAgendaFilter(context);
		assertNotNull(resultFilter);
		assertEquals(resultFilter.getClass(), DefaultCompoundAgendaFilter.class);
	}
}
