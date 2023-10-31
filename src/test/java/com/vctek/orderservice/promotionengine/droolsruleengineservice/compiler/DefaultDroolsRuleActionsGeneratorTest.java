package com.vctek.orderservice.promotionengine.droolsruleengineservice.compiler;

import com.vctek.orderservice.promotionengine.droolsruleengineservice.compiler.impl.DefaultDroolsRuleActionsGenerator;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleIr;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleIrVariable;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.actions.DefaultDroolsRuleActionContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.actions.RuleIrExecutableAction;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.formatter.DroolsRuleValueFormatter;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.CartRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.RuleEngineResultRAO;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultDroolsRuleActionsGeneratorTest extends AbstractGeneratorTest {
    public static final String INDENTATION = "  ";
    public static final String VARIABLE_PREFIX = "$";

    public static final String ORDER_VARIABLE_NAME = "cart";
    public static final String ORDER_VARIABLE_CLASS_NAME = "CartRAO";

    public static final String RESULT_VARIABLE_NAME = "result";
    public static final String RESULT_VARIABLE_CLASS_NAME = "RuleEngineResultRAO";

    public static final String MAP_CLASS_NAME = "Map";
    public static final String ACTION_CONTEXT_CLASS_NAME = "DefaultDroolsRuleActionContext";
    private DroolsRuleGeneratorContext droolsRuleGeneratorContext;
    private DroolsRuleValueFormatter droolsRuleValueFormatter;
    private RuleIrVariable orderVariable;
    private RuleIrVariable resultVariable;
    private DroolsRuleModel droolsRule;
    private PromotionSourceRuleModel rule;

    private RuleIr ruleIr;
    private Map<String, RuleIrVariable> ruleIrVariables;

    private DefaultDroolsRuleActionsGenerator actionsGenerator;

    @Before
    public void init()
    {
        droolsRuleGeneratorContext = mock(DroolsRuleGeneratorContext.class);
        droolsRuleValueFormatter = mock(DroolsRuleValueFormatter.class);
        droolsRule = mock(DroolsRuleModel.class);
        rule = mock(PromotionSourceRuleModel.class);
        orderVariable = new RuleIrVariable();
        orderVariable.setName(ORDER_VARIABLE_NAME);
        orderVariable.setType(CartRAO.class);

        resultVariable = new RuleIrVariable();
        resultVariable.setName(RESULT_VARIABLE_NAME);
        resultVariable.setType(RuleEngineResultRAO.class);

        ruleIr = new RuleIr();
        ruleIrVariables = new LinkedHashMap<>();

        when(droolsRuleGeneratorContext.getIndentationSize()).thenReturn(INDENTATION);
        when(droolsRuleGeneratorContext.getVariablePrefix()).thenReturn(VARIABLE_PREFIX);
        when(droolsRuleGeneratorContext.getRuleIr()).thenReturn(ruleIr);
        when(droolsRuleGeneratorContext.getVariables()).thenReturn(ruleIrVariables);
        when(droolsRuleGeneratorContext.generateClassName(CartRAO.class)).thenReturn(ORDER_VARIABLE_CLASS_NAME);
        when(droolsRuleGeneratorContext.generateClassName(RuleEngineResultRAO.class)).thenReturn(RESULT_VARIABLE_CLASS_NAME);
        when(droolsRuleGeneratorContext.generateClassName(Map.class)).thenReturn(MAP_CLASS_NAME);
        when(droolsRuleGeneratorContext.generateClassName(DefaultDroolsRuleActionContext.class)).thenReturn(
                ACTION_CONTEXT_CLASS_NAME);

        when(droolsRuleGeneratorContext.getDroolsRule()).thenReturn(droolsRule);
        when(droolsRule.getPromotionSourceRule()).thenReturn(rule);

        actionsGenerator = new DefaultDroolsRuleActionsGenerator(droolsRuleValueFormatter);
    }

    @Test
    public void testGenerateCode() throws IOException
    {
        // given
        final String expectedDroolsCode = getResourceAsString("/compiler/generatedActions.bin");

        final BigDecimal totalValue = BigDecimal.valueOf(20);
        final Map<String, Object> actionParameters = Collections.singletonMap("total", totalValue);

        final RuleIrExecutableAction ruleIrAction = new RuleIrExecutableAction();
        ruleIrAction.setActionId("testBeanID");
        ruleIrAction.setActionParameters(actionParameters);

        ruleIr.setActions(Collections.singletonList(ruleIrAction));
        ruleIrVariables.put(ORDER_VARIABLE_NAME, orderVariable);
        ruleIrVariables.put(RESULT_VARIABLE_NAME, resultVariable);

        when(droolsRuleValueFormatter.formatValue(droolsRuleGeneratorContext, actionParameters)).thenReturn(
                "[\"value\":new BigDecimal(20)]");

        final String generatedDroolsCode = actionsGenerator.generateActions(droolsRuleGeneratorContext, INDENTATION);

        assertEquals(expectedDroolsCode, generatedDroolsCode);
    }
}
