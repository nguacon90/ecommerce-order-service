package com.vctek.orderservice.promotionengine.ruleengineservice.compiler;

import com.vctek.orderservice.promotionengine.droolsruleengineservice.compiler.RuleTargetCodeGenerator;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.exception.RuleCompilerException;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.actions.RuleIrAction;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.actions.RuleIrExecutableAction;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrAttributeCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrGroupCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.processor.PromotionRuleIrProcessor;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.RuleSourceCodeTranslator;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.impl.RuleCartTotalConditionTranslator;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleIrAttributeOperator;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleIrGroupOperator;
import com.vctek.orderservice.promotionengine.ruleengineservice.maintenance.RuleCompilationContext;
import com.vctek.orderservice.promotionengine.util.CurrencyIsoCode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class DefaultRuleCompilerServiceTest {
    private static final String MODULE_NAME = "MODULE_NAME";

    private RuleIrVariablesGenerator variablesGenerator;

    @Mock
    private RuleIrVariablesGeneratorFactory ruleIrVariablesGeneratorFactory;

    @Mock
    private RuleSourceCodeTranslator ruleSourceCodeTranslator;

    private RuleIrProcessor ruleIrProcessor;

    @Mock
    private RuleTargetCodeGenerator ruleTargetCodeGenerator;

    @Mock
    private PromotionSourceRuleModel sourceRuleModel;

    private RuleIr ruleIr = new RuleIr();

    private ArgumentCaptor<RuleIr> ruleIrArgumentCaptor;

    @Mock
    private DefaultRuleCompilerContext context;

    private DefaultRuleCompilerService ruleCompilerService;
    private List<RuleIrCondition> conditions = new ArrayList<>();
    private List<RuleIrAction> actions = new ArrayList<>();
    private List<RuleIrCondition> children = new ArrayList<>();
    private Map<String, BigDecimal> value = new HashMap<>();
    @Mock
    private RuleCompilerContextFactory<DefaultRuleCompilerContext> ruleCompilerContextFactory;
    @Mock
    private RuleCompilationContext ruleCompilationContext;

    @Before
    public void setUp() {
        variablesGenerator = new DefaultRuleIrVariablesGenerator();
        ruleIrArgumentCaptor = ArgumentCaptor.forClass(RuleIr.class);
        MockitoAnnotations.initMocks(this);
        ruleIrProcessor = new PromotionRuleIrProcessor();
        when(ruleCompilerContextFactory.createContext(ruleCompilationContext, sourceRuleModel, MODULE_NAME, variablesGenerator))
                .thenReturn(context);
        when(context.getRule()).thenReturn(sourceRuleModel);
        ruleCompilerService = new DefaultRuleCompilerService(ruleIrVariablesGeneratorFactory, ruleTargetCodeGenerator,
                ruleSourceCodeTranslator, ruleIrProcessor);
        ruleCompilerService.setRuleCompilerContextFactory(ruleCompilerContextFactory);
        ruleCompilerService.setRuleCompilationContext(ruleCompilationContext);
        when(ruleIrVariablesGeneratorFactory.createVariablesGenerator()).thenReturn(variablesGenerator);
    }

    @Test(expected = RuleCompilerException.class)
    public void testCompileFails() {
        when(ruleSourceCodeTranslator.translate(any(RuleCompilerContext.class))).thenThrow(new RuleCompilerException());
        ruleCompilerService.compile(sourceRuleModel, MODULE_NAME);
    }

    @Test
    public void compile_emptyConditions() {
        when(sourceRuleModel.getCompanyId()).thenReturn(2l);
        when(ruleSourceCodeTranslator.translate(any(RuleCompilerContext.class))).thenReturn(ruleIr);
        ruleIr.setConditions(new ArrayList<>());
        ruleIr.setActions(new ArrayList<>());

        ruleCompilerService.compile(sourceRuleModel, MODULE_NAME);
        verify(ruleTargetCodeGenerator).generate(any(RuleCompilerContext.class), any(RuleIr.class));
    }

    @Test
    public void compile_CartThresholdConditions() {
        when(sourceRuleModel.getCompanyId()).thenReturn(2l);
        when(ruleSourceCodeTranslator.translate(any(RuleCompilerContext.class))).thenReturn(ruleIr);

        RuleIrAttributeCondition attributeCondition = new RuleIrAttributeCondition();
        attributeCondition.setAttribute(RuleCartTotalConditionTranslator.ORDER_RAO_TOTAL_ATTRIBUTE);
        attributeCondition.setOperator(RuleIrAttributeOperator.GREATER_THAN_OR_EQUAL);
        value.put(CurrencyIsoCode.VND.toString(), new BigDecimal(2000000));
        attributeCondition.setValue(value);
        children.add(attributeCondition);
        RuleIrGroupCondition cartThresholdCondition = new RuleIrGroupCondition();
        cartThresholdCondition.setOperator(RuleIrGroupOperator.OR);
        cartThresholdCondition.setChildren(children);
        conditions.add(cartThresholdCondition);
        ruleIr.setConditions(conditions);

        RuleIrExecutableAction action = new RuleIrExecutableAction();
        action.setActionParameters(new HashMap<>());
        actions.add(action);
        ruleIr.setActions(actions);

        ruleCompilerService.compile(sourceRuleModel, MODULE_NAME);
        verify(ruleTargetCodeGenerator).generate(any(RuleCompilerContext.class), ruleIrArgumentCaptor.capture());
        RuleIr actualRuleIr = ruleIrArgumentCaptor.getValue();
        assertNotNull(actualRuleIr);
        List<RuleIrAction> actions = actualRuleIr.getActions();
        assertEquals(1, actions.size());
        RuleIrExecutableAction ruleIrAction = (RuleIrExecutableAction) actions.get(0);
        Map<String, Object> actionParameters = ruleIrAction.getActionParameters();
        assertTrue(actionParameters.containsKey(PromotionRuleIrProcessor.ACTION_PARAMETER_CART_THRESHOLD));
    }
}
