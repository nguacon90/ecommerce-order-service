package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.impl;

import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.exception.RuleCompilerException;
import com.vctek.orderservice.promotionengine.ruleengine.exception.RuleConverterException;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleIr;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleIrVariablesGenerator;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.actions.RuleIrAction;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.actions.RuleIrNoOpAction;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrFalseCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.RuleActionsTranslator;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.RuleConditionsTranslator;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleActionsService;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleConditionsService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;


public class DefaultSourceRuleSourceCodeTranslatorTest {
    private static final String RULE_CODE = "testrule";
    private static final String RULE_CONDITIONS = "[{\"definitionId\":\"y_qualifying_products\"}]";
    private static final String RULE_ACTIONS = "[{\"definitionId\":\"y_order_percentage_discount\"}]";

    @Rule
    public ExpectedException expectedException = ExpectedException.none(); //NOPMD

    @Mock
    private RuleIrVariablesGenerator variablesGenerator;

    @Mock
    private RuleCompilerContext compilerContext;

    @Mock
    private PromotionSourceRuleModel sourceRule;

    @Mock
    private RuleConditionsService ruleConditionsService;

    @Mock
    private RuleActionsService ruleActionsService;

    @Mock
    private RuleConditionsTranslator ruleConditionsTranslator;

    @Mock
    private RuleActionsTranslator ruleActionsTranslator;

    private DefaultRuleSourceCodeTranslator sourceRuleSourceCodeTranslator;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(compilerContext.getVariablesGenerator()).thenReturn(variablesGenerator);

        sourceRuleSourceCodeTranslator = new DefaultRuleSourceCodeTranslator(ruleConditionsService, ruleActionsService,
                ruleConditionsTranslator, ruleActionsTranslator);
    }

    @Test
    public void translateRule() {
        // given
        final RuleConditionData ruleConditionData = new RuleConditionData();
        ruleConditionData.setChildren(Collections.emptyList());
        ruleConditionData.setParameters(Collections.emptyMap());
        final List<RuleConditionData> ruleConditions = Arrays.asList(ruleConditionData);

        final RuleActionData ruleActionData = new RuleActionData();
        ruleActionData.setParameters(Collections.emptyMap());
        final List<RuleActionData> ruleActions = Arrays.asList(ruleActionData);

        final List<RuleIrCondition> ruleIrConditions = Arrays.asList(new RuleIrFalseCondition());
        final List<RuleIrAction> ruleIrActions = Arrays.asList(new RuleIrNoOpAction());

        when(sourceRule.getCode()).thenReturn(RULE_CODE);
        when(sourceRule.getConditions()).thenReturn(RULE_CONDITIONS);
        when(sourceRule.getActions()).thenReturn(RULE_ACTIONS);

        when(ruleConditionsService.convertConditionsFromString(RULE_CONDITIONS, compilerContext.getConditionDefinitions()))
                .thenReturn(ruleConditions);
        when(ruleActionsService.convertActionsFromString(RULE_ACTIONS, compilerContext.getActionDefinitions()))
                .thenReturn(ruleActions);

        when(compilerContext.getRule()).thenReturn(sourceRule);

        when(ruleConditionsTranslator.translate(compilerContext, ruleConditions)).thenReturn(ruleIrConditions);
        when(ruleActionsTranslator.translate(compilerContext, ruleActions)).thenReturn(ruleIrActions);

        // when
        final RuleIr ruleIr = sourceRuleSourceCodeTranslator.translate(compilerContext);

        // then
        assertNotNull(ruleIr);
        assertSame(ruleIrConditions, ruleIr.getConditions());
        assertSame(ruleIrActions, ruleIr.getActions());
    }

    @Test
    public void failToConvertConditions() {
        // given
        final List<RuleActionData> ruleActions = Arrays.asList(new RuleActionData());

        when(sourceRule.getCode()).thenReturn(RULE_CODE);
        when(sourceRule.getConditions()).thenReturn(RULE_CONDITIONS);
        when(sourceRule.getActions()).thenReturn(RULE_ACTIONS);

        when(compilerContext.getRule()).thenReturn(sourceRule);

        when(ruleConditionsService.convertConditionsFromString(RULE_CONDITIONS, compilerContext.getConditionDefinitions()))
                .thenThrow(new RuleConverterException());
        when(ruleActionsService.convertActionsFromString(RULE_ACTIONS, compilerContext.getActionDefinitions()))
                .thenReturn(ruleActions);

        // expect
        expectedException.expect(RuleCompilerException.class);

        // when
        sourceRuleSourceCodeTranslator.translate(compilerContext);
    }

    @Test
    public void failToConvertActions() {
        // given
        final List<RuleConditionData> ruleConditions = Arrays.asList(new RuleConditionData());

        when(sourceRule.getCode()).thenReturn(RULE_CODE);
        when(sourceRule.getConditions()).thenReturn(RULE_CONDITIONS);
        when(sourceRule.getActions()).thenReturn(RULE_ACTIONS);

        when(compilerContext.getRule()).thenReturn(sourceRule);

        when(ruleConditionsService.convertConditionsFromString(RULE_CONDITIONS, compilerContext.getConditionDefinitions()))
                .thenReturn(ruleConditions);
        when(ruleActionsService.convertActionsFromString(RULE_ACTIONS, compilerContext.getActionDefinitions()))
                .thenThrow(new RuleConverterException());

        // expect
        expectedException.expect(RuleCompilerException.class);

        // when
        sourceRuleSourceCodeTranslator.translate(compilerContext);
    }

    @Test
    public void failToTranslateConditions() {
        // given
        final RuleConditionData ruleConditionData = new RuleConditionData();
        ruleConditionData.setChildren(Collections.emptyList());
        ruleConditionData.setParameters(Collections.emptyMap());
        final List<RuleConditionData> ruleConditions = Arrays.asList(ruleConditionData);

        final RuleActionData ruleActionData = new RuleActionData();
        ruleActionData.setParameters(Collections.emptyMap());
        final List<RuleActionData> ruleActions = Arrays.asList(ruleActionData);

        final List<RuleIrAction> ruleIrActions = Arrays.asList(new RuleIrNoOpAction());

        when(sourceRule.getCode()).thenReturn(RULE_CODE);
        when(sourceRule.getConditions()).thenReturn(RULE_CONDITIONS);
        when(sourceRule.getActions()).thenReturn(RULE_ACTIONS);

        when(compilerContext.getRule()).thenReturn(sourceRule);

        when(ruleConditionsService.convertConditionsFromString(RULE_CONDITIONS, compilerContext.getConditionDefinitions()))
                .thenReturn(ruleConditions);
        when(ruleActionsService.convertActionsFromString(RULE_ACTIONS, compilerContext.getActionDefinitions()))
                .thenReturn(ruleActions);

        when(ruleConditionsTranslator.translate(compilerContext, ruleConditions)).thenThrow(new RuleCompilerException());
        when(ruleActionsTranslator.translate(compilerContext, ruleActions)).thenReturn(ruleIrActions);

        // expect
        expectedException.expect(RuleCompilerException.class);

        // when
        sourceRuleSourceCodeTranslator.translate(compilerContext);
    }

    @Test
    public void failToTranslateActions() {
        // given
        final RuleConditionData ruleConditionData = new RuleConditionData();
        ruleConditionData.setChildren(Collections.emptyList());
        ruleConditionData.setParameters(Collections.emptyMap());
        final List<RuleConditionData> ruleConditions = Arrays.asList(ruleConditionData);

        final RuleActionData ruleActionData = new RuleActionData();
        ruleActionData.setParameters(Collections.emptyMap());
        final List<RuleActionData> ruleActions = Arrays.asList(ruleActionData);

        final List<RuleIrCondition> ruleIrConditions = Arrays.asList(new RuleIrFalseCondition());

        when(sourceRule.getCode()).thenReturn(RULE_CODE);
        when(sourceRule.getConditions()).thenReturn(RULE_CONDITIONS);
        when(sourceRule.getActions()).thenReturn(RULE_ACTIONS);

        when(compilerContext.getRule()).thenReturn(sourceRule);

        when(ruleConditionsService.convertConditionsFromString(RULE_CONDITIONS, compilerContext.getConditionDefinitions()))
                .thenReturn(ruleConditions);
        when(ruleActionsService.convertActionsFromString(RULE_ACTIONS, compilerContext.getActionDefinitions()))
                .thenReturn(ruleActions);

        when(ruleConditionsTranslator.translate(compilerContext, ruleConditions)).thenReturn(ruleIrConditions);
        when(ruleActionsTranslator.translate(compilerContext, ruleActions)).thenThrow(new RuleCompilerException());

        // expect
        expectedException.expect(RuleCompilerException.class);

        // when
        sourceRuleSourceCodeTranslator.translate(compilerContext);
    }
}
