package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.impl;

import com.vctek.converter.Converter;
import com.vctek.orderservice.promotionengine.ruleengine.exception.RuleCompilerException;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.actions.RuleIrAction;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.actions.RuleIrNoOpAction;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.RuleActionTranslator;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleActionDefinitionModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleActionDefinitionService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


public class DefaultRuleActionsTranslatorTest
{
	private static final String ACTION_DEFINITION_ID = "actionDefinition";
	private static final String ACTION_TRANSLATOR_ID = "actionTranslator";

	@Rule
	public ExpectedException expectedException = ExpectedException.none(); //NOPMD

	@Mock
	private RuleCompilerContext ruleCompilerContext;

	@Mock
	private ApplicationContext applicationContext;

	@Mock
	private RuleActionTranslator ruleActionTranslator;

	@Mock
    private RuleActionDefinitionService ruleActionDefinitionService;

	@Mock
    private Converter<RuleActionDefinitionModel, RuleActionDefinitionData> actionDefinitionConverter;

	private DefaultRuleActionsTranslator ruleActionsTranslator;

	@Before
	public void setUp()
	{
		MockitoAnnotations.initMocks(this);
		ruleActionsTranslator = new DefaultRuleActionsTranslator(applicationContext, ruleActionDefinitionService,
                actionDefinitionConverter);
	}

	@Test
	public void translateActions() throws Exception
	{
		// given
		final RuleActionData ruleAction = new RuleActionData();
		ruleAction.setDefinitionId(ACTION_DEFINITION_ID);

		final RuleActionDefinitionData ruleActionDefinition = new RuleActionDefinitionData();
		ruleActionDefinition.setTranslatorId(ACTION_TRANSLATOR_ID);

		final RuleIrAction ruleIrAction = new RuleIrNoOpAction();

        when(ruleActionDefinitionService.findByCode(anyString())).thenReturn(new RuleActionDefinitionModel());
        when(actionDefinitionConverter.convert(any(RuleActionDefinitionModel.class))).thenReturn(ruleActionDefinition);
		when(applicationContext.getBean(ACTION_TRANSLATOR_ID, RuleActionTranslator.class)).thenReturn(ruleActionTranslator);
		when(ruleActionTranslator.translate(ruleCompilerContext, ruleAction, ruleActionDefinition)).thenReturn(ruleIrAction);

		// when
		final List<RuleIrAction> ruleIrActions = ruleActionsTranslator.translate(ruleCompilerContext, Arrays.asList(ruleAction));

		// then
		assertEquals(1, ruleIrActions.size());
		assertThat(ruleIrActions, hasItem(ruleIrAction));
	}

	@Test
	public void actionDefinitionNotFound() throws Exception
	{
		// given
		final RuleActionData ruleAction = new RuleActionData();

		// when
		final List<RuleIrAction> ruleIrActions = ruleActionsTranslator.translate(ruleCompilerContext, Arrays.asList(ruleAction));

		// then
		assertThat(ruleIrActions, is(Collections.<RuleIrAction> emptyList()));
	}

	@Test
	public void actionTranslatorNotFound() throws Exception
	{
		// given
		final RuleActionData ruleAction = new RuleActionData();
		ruleAction.setDefinitionId(ACTION_DEFINITION_ID);

		final RuleActionDefinitionData ruleActionDefinition = new RuleActionDefinitionData();
		ruleActionDefinition.setTranslatorId(ACTION_TRANSLATOR_ID);

        when(ruleActionDefinitionService.findByCode(anyString())).thenReturn(new RuleActionDefinitionModel());
        when(actionDefinitionConverter.convert(any(RuleActionDefinitionModel.class))).thenReturn(ruleActionDefinition);
		when(applicationContext.getBean(ACTION_TRANSLATOR_ID, RuleActionTranslator.class)).thenThrow(
				new NoSuchBeanDefinitionException(ACTION_TRANSLATOR_ID));

		// expect
		expectedException.expect(RuleCompilerException.class);

		// when
		ruleActionsTranslator.translate(ruleCompilerContext, Arrays.asList(ruleAction));
	}
}
