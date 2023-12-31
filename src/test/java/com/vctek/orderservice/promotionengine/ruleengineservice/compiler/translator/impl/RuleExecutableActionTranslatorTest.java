package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.impl;

import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.actions.RuleIrAction;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.actions.RuleIrExecutableAction;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.actions.RuleIrNoOpAction;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;


public class RuleExecutableActionTranslatorTest
{
	public static final String ACTION_ID = "action";
	public static final String TEST_PARAM_ID = "test";
	public static final String TEST_PARAM_VALUE = "testvalue";

	@Rule
	public ExpectedException expectedException = ExpectedException.none(); //NOPMD

	@Mock
	private RuleCompilerContext context;

	private RuleActionData action;
	private RuleActionDefinitionData actionDefinition;

	private RuleExecutableActionTranslator ruleExecutableActionTranslator;

	@Before
	public void setUp()
	{
		MockitoAnnotations.initMocks(this);

		action = new RuleActionData();
		action.setParameters(Collections.emptyMap());

		actionDefinition = new RuleActionDefinitionData();
		actionDefinition.setTranslatorParameters(Collections.emptyMap());

		ruleExecutableActionTranslator = new RuleExecutableActionTranslator();
	}

	@Test
	public void translate()
	{
		// given
		actionDefinition.setTranslatorParameters(Collections
				.singletonMap(RuleExecutableActionTranslator.ACTION_ID_PARAM, ACTION_ID));

		// when
		final RuleIrAction irAction = ruleExecutableActionTranslator.translate(context, action, actionDefinition);

		// then
		assertNotNull(irAction);
		assertTrue(irAction instanceof RuleIrExecutableAction);
		assertEquals(ACTION_ID, ((RuleIrExecutableAction) irAction).getActionId());
	}

	@Test
	public void translateWithParameter()
	{
		// given
		final RuleParameterData testParam = new RuleParameterData();
		testParam.setValue(TEST_PARAM_VALUE);

		action.setParameters(Collections.singletonMap(TEST_PARAM_ID, testParam));
		actionDefinition.setTranslatorParameters(Collections
				.singletonMap(RuleExecutableActionTranslator.ACTION_ID_PARAM, ACTION_ID));

		// when
		final RuleIrAction irAction = ruleExecutableActionTranslator.translate(context, action, actionDefinition);

		// then
		assertNotNull(irAction);
		assertTrue(irAction instanceof RuleIrExecutableAction);
		assertEquals(ACTION_ID, ((RuleIrExecutableAction) irAction).getActionId());
		assertEquals(TEST_PARAM_VALUE, ((RuleIrExecutableAction) irAction).getActionParameters().get(TEST_PARAM_ID));
	}

	@Test
	public void failToTranslateNoActionId()
	{
		// when
		final RuleIrAction irAction = ruleExecutableActionTranslator.translate(context, action, actionDefinition);

		// then
		assertTrue(irAction instanceof RuleIrNoOpAction);
	}

	@Test
	public void translateActionHasNoParameters()
	{
		// given
		action.setParameters(null);
		actionDefinition.setTranslatorParameters(Collections
				.singletonMap(RuleExecutableActionTranslator.ACTION_ID_PARAM, ACTION_ID));

		// when
		final RuleIrAction irAction = ruleExecutableActionTranslator.translate(context, action, actionDefinition);

		// then
		assertThat(irAction).isNotNull();
		assertThat(irAction).isInstanceOf(RuleIrExecutableAction.class);
		assertThat(((RuleIrExecutableAction)irAction).getActionParameters()).isNotNull();
	}

}
