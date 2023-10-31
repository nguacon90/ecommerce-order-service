package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.impl;

import com.vctek.converter.Converter;
import com.vctek.orderservice.promotionengine.ruleengine.exception.RuleCompilerException;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrFalseCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.RuleConditionTranslator;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleConditionDefinitionModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleConditionDefinitionService;
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


public class DefaultRuleConditionsTranslatorTest {
    private static final String CONDITION_DEFINITION_ID = "conditionDefinition";
    private static final String CONDITION_TRANSLATOR_ID = "conditionTranslator";

    @Rule
    public ExpectedException expectedException = ExpectedException.none(); //NOPMD

    @Mock
    private RuleCompilerContext ruleCompilerContext;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private RuleConditionTranslator ruleConditionTranslator;

    private DefaultRuleConditionsTranslator ruleConditionsTranslator;

    @Mock
    private RuleConditionDefinitionService conditionDefinitionService;
    @Mock
    private Converter<RuleConditionDefinitionModel, RuleConditionDefinitionData> conditionDefinitionConverter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        ruleConditionsTranslator = new DefaultRuleConditionsTranslator(applicationContext, conditionDefinitionService,
                conditionDefinitionConverter);
    }

    @Test
    public void translateConditions() {
        // given
        final RuleConditionData ruleCondition = new RuleConditionData();
        ruleCondition.setDefinitionId(CONDITION_DEFINITION_ID);

        final RuleConditionDefinitionData ruleConditionDefinition = new RuleConditionDefinitionData();
        ruleConditionDefinition.setTranslatorId(CONDITION_TRANSLATOR_ID);

        final RuleIrCondition ruleIrCondition = new RuleIrFalseCondition();

        when(conditionDefinitionService.findByCode(anyString())).thenReturn(new RuleConditionDefinitionModel());
        when(conditionDefinitionConverter.convert(any(RuleConditionDefinitionModel.class))).thenReturn(ruleConditionDefinition);
        when(applicationContext.getBean(CONDITION_TRANSLATOR_ID, RuleConditionTranslator.class))
                .thenReturn(ruleConditionTranslator);

        when(ruleConditionTranslator.translate(ruleCompilerContext, ruleCondition, ruleConditionDefinition)).thenReturn(
                ruleIrCondition);

        // when
        final List<RuleIrCondition> ruleIrConditions = ruleConditionsTranslator.translate(ruleCompilerContext,
                Arrays.asList(ruleCondition));

        // then
        assertEquals(1, ruleIrConditions.size());
        assertThat(ruleIrConditions, hasItem(ruleIrCondition));
    }

    @Test
    public void conditionDefinitionNotFound() {
        // given
        final RuleConditionData ruleCondition = new RuleConditionData();

        // when
        final List<RuleIrCondition> ruleIrConditions = ruleConditionsTranslator.translate(ruleCompilerContext,
                Arrays.asList(ruleCondition));

        // then
        assertThat(ruleIrConditions, is(Collections.<RuleIrCondition>emptyList()));
    }

    @Test
    public void conditionTranslatorNotFound() {
        // given
        final RuleConditionData ruleCondition = new RuleConditionData();
        ruleCondition.setDefinitionId(CONDITION_DEFINITION_ID);

        final RuleConditionDefinitionData ruleConditionDefinition = new RuleConditionDefinitionData();
        ruleConditionDefinition.setTranslatorId(CONDITION_TRANSLATOR_ID);

        when(conditionDefinitionService.findByCode(anyString())).thenReturn(new RuleConditionDefinitionModel());
        when(conditionDefinitionConverter.convert(any(RuleConditionDefinitionModel.class))).thenReturn(ruleConditionDefinition);
        when(applicationContext.getBean(CONDITION_TRANSLATOR_ID, RuleConditionTranslator.class)).thenThrow(
                new NoSuchBeanDefinitionException(CONDITION_TRANSLATOR_ID));

        // expect
        expectedException.expect(RuleCompilerException.class);

        // when
        ruleConditionsTranslator.translate(ruleCompilerContext, Arrays.asList(ruleCondition));
    }
}
