package com.vctek.orderservice.promotionengine.droolsruleengineservice.compiler;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.vctek.orderservice.promotionengine.droolsruleengineservice.compiler.impl.DefaultDroolsRuleConditionsGenerator;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleIr;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleIrVariable;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.*;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.formatter.DroolsRuleValueFormatter;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleIrAttributeOperator;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleIrGroupOperator;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.*;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultDroolsRuleConditionsGeneratorTest extends AbstractGeneratorTest {
    private static final String ORDER_ENTRY_CLASS_NAME = "OrderEntryRAO";
    private static final String ORDER_ENTRY_VARIABLE_NAME = "orderEntry";
    private static final String INDENTATION = "  ";
    private static final String VARIABLE_PREFIX = "$";
    private static final String ATTRIBUTE_DELIMITER = ".";
    private static final String CART_VARIABLE_NAME = "cart";
    private static final String ENTRYGROUP_VARIABLE_NAME = "entryGroup";
    private static final String ENTRYGROUP_VARIABLE_CLASS_NAME = "OrderEntryGroupRAO";
    private static final String WEBSITE_VARIABLE_NAME = "website";
    private static final String WEBSITE_VARIABLE_CLASS_NAME = "WebsiteGroupRAO";
    private static final String CART_VARIABLE_CLASS_NAME = "CartRAO";

    private static final String PRODUCT_VARIABLE_NAME = "product";
    private static final String PRODUCT_VARIABLE_CLASS_NAME = "ProductRAO";

    private static final String RESULT_VARIABLE_NAME = "result";
    private static final String RESULT_VARIABLE_CLASS_NAME = "RuleEngineResultRAO";


    private static final String CATEGORY_VARIABLE_NAME = "category";
    private static final String CATEGORY_VARIABLE_CLASS_NAME = "CategoryRAO";

    private static final String ACTION_VARIABLE_NAME = "action";
    private static final String ACTION_VARIABLE_CLASS_NAME = "AbstractRuleActionRAO";
    private DroolsRuleGeneratorContext droolsRuleGeneratorContext;
    private DroolsRuleValueFormatter droolsRuleValueFormatter;
    private RuleIrVariable cartVariable;
    private RuleIrVariable productVariable;
    private RuleIrVariable resultVariable;
    private RuleIrVariable orderEntryVariable;
    private RuleIrVariable entryGroupVariable;
    private RuleIrVariable websiteGrpVariable;
    private RuleIrVariable categoryVariable;
    private RuleIrVariable actionVariable;

    private RuleIr ruleIr;
    private Map<String, RuleIrVariable> ruleIrVariables;
    private RuleIrTypeCondition ruleIrResultCondition;

    private DefaultDroolsRuleConditionsGenerator conditionsGenerator;

    @Before
    public void setUp()
    {
        droolsRuleGeneratorContext = mock(DroolsRuleGeneratorContext.class);
        droolsRuleValueFormatter = mock(DroolsRuleValueFormatter.class);
        cartVariable = new RuleIrVariable();
        cartVariable.setName(CART_VARIABLE_NAME);
        cartVariable.setType(CartRAO.class);

        productVariable = new RuleIrVariable();
        productVariable.setName(PRODUCT_VARIABLE_NAME);
        productVariable.setType(ProductRAO.class);

        orderEntryVariable = new RuleIrVariable();
        orderEntryVariable.setName(ORDER_ENTRY_VARIABLE_NAME);
        orderEntryVariable.setType(OrderEntryRAO.class);

        resultVariable = new RuleIrVariable();
        resultVariable.setName(RESULT_VARIABLE_NAME);
        resultVariable.setType(RuleEngineResultRAO.class);

        categoryVariable = new RuleIrVariable();
        categoryVariable.setName(CATEGORY_VARIABLE_NAME);
        categoryVariable.setType(CategoryRAO.class);

        actionVariable = new RuleIrVariable();
        actionVariable.setName(ACTION_VARIABLE_NAME);
        actionVariable.setType(AbstractRuleActionRAO.class);

        entryGroupVariable = new RuleIrVariable();
        entryGroupVariable.setName(ENTRYGROUP_VARIABLE_NAME);
        entryGroupVariable.setType(OrderEntryGroupRAO.class);

        websiteGrpVariable = new RuleIrVariable();
        websiteGrpVariable.setName(WEBSITE_VARIABLE_NAME);
        websiteGrpVariable.setType(WebsiteGroupRAO.class);

        ruleIr = new RuleIr();
        ruleIrVariables = new LinkedHashMap<>();

        ruleIrResultCondition = new RuleIrTypeCondition();
        ruleIrResultCondition.setVariable(RESULT_VARIABLE_NAME);

        when(droolsRuleGeneratorContext.getIndentationSize()).thenReturn(INDENTATION);
        when(droolsRuleGeneratorContext.getVariablePrefix()).thenReturn(VARIABLE_PREFIX);
        when(droolsRuleGeneratorContext.getAttributeDelimiter()).thenReturn(ATTRIBUTE_DELIMITER);
        when(droolsRuleGeneratorContext.getRuleIr()).thenReturn(ruleIr);
        when(droolsRuleGeneratorContext.getVariables()).thenReturn(ruleIrVariables);
        when(droolsRuleGeneratorContext.getLocalVariables()).thenReturn(new ArrayDeque<>());
        when(droolsRuleGeneratorContext.generateClassName(CartRAO.class)).thenReturn(CART_VARIABLE_CLASS_NAME);
        when(droolsRuleGeneratorContext.generateClassName(ProductRAO.class)).thenReturn(PRODUCT_VARIABLE_CLASS_NAME);
        when(droolsRuleGeneratorContext.generateClassName(RuleEngineResultRAO.class)).thenReturn(RESULT_VARIABLE_CLASS_NAME);
        when(droolsRuleGeneratorContext.generateClassName(OrderEntryRAO.class)).thenReturn(ORDER_ENTRY_CLASS_NAME);
        when(droolsRuleGeneratorContext.generateClassName(OrderEntryGroupRAO.class)).thenReturn(ENTRYGROUP_VARIABLE_CLASS_NAME);
        when(droolsRuleGeneratorContext.generateClassName(WebsiteGroupRAO.class)).thenReturn(WEBSITE_VARIABLE_CLASS_NAME);
        when(droolsRuleGeneratorContext.generateClassName(CategoryRAO.class)).thenReturn(CATEGORY_VARIABLE_CLASS_NAME);
        when(droolsRuleGeneratorContext.generateClassName(AbstractRuleActionRAO.class)).thenReturn(ACTION_VARIABLE_CLASS_NAME);


        conditionsGenerator = new DefaultDroolsRuleConditionsGenerator(droolsRuleValueFormatter);
    }

    private RuleIrGroupCondition createIrGroupCondition(final RuleIrGroupOperator operator)
    {
        return createIrGroupCondition(operator, null);
    }


    private RuleIrGroupCondition createIrGroupCondition(final RuleIrGroupOperator operator, final List<RuleIrCondition> children)
    {
        final RuleIrGroupCondition groupCondition = new RuleIrGroupCondition();
        groupCondition.setOperator(operator);
        groupCondition.setChildren(children);
        return groupCondition;
    }

    private RuleIrAttributeRelCondition createRuleIrAttributeRelCondition(final String attribute,
                                                                          final RuleIrAttributeOperator operator, final String variable, final String targetVariable)
    {
        final RuleIrAttributeRelCondition condition = new RuleIrAttributeRelCondition();
        condition.setTargetVariable(targetVariable);
        condition.setAttribute(attribute);
        condition.setVariable(variable);
        condition.setOperator(operator);
        return condition;
    }

    private RuleIrAttributeCondition createRuleIrAttributeCondition(final String attribute, final RuleIrAttributeOperator operator,
                                                                    final Object value, final String variable)
    {
        final RuleIrAttributeCondition condition = new RuleIrAttributeCondition();
        condition.setAttribute(attribute);
        condition.setOperator(operator);
        condition.setValue(value);
        condition.setVariable(variable);
        return condition;
    }

    protected List<RuleIrCondition> createPatternConditions()
    {
        final String websiteGrpName = "electronicsPromoGroup";

        final RuleIrTypeCondition typeCondition1 = new RuleIrTypeCondition();
        typeCondition1.setVariable(CART_VARIABLE_NAME);
        final RuleIrTypeCondition typeCondition2 = new RuleIrTypeCondition();
        typeCondition2.setVariable(RESULT_VARIABLE_NAME);
        final RuleIrAttributeCondition attributeCondition = createRuleIrAttributeCondition("id",
                RuleIrAttributeOperator.EQUAL, websiteGrpName, WEBSITE_VARIABLE_NAME);

        ruleIrVariables.put(CART_VARIABLE_NAME, cartVariable);
        ruleIrVariables.put(RESULT_VARIABLE_NAME, resultVariable);
        ruleIrVariables.put(WEBSITE_VARIABLE_NAME, websiteGrpVariable);


        when(droolsRuleValueFormatter.formatValue(droolsRuleGeneratorContext, websiteGrpName))
                .thenReturn("\"electronicsPromoGroup\"");

        return Lists.newArrayList(typeCondition1, typeCondition2, attributeCondition);
    }

    private List<RuleIrCondition> createGroupConditions()
    {
        final RuleIrGroupCondition groupCondition1 = createIrGroupCondition(RuleIrGroupOperator.AND);
        final RuleIrGroupCondition groupCondition11 = createIrGroupCondition(RuleIrGroupOperator.AND);
        groupCondition1.setChildren(Collections.singletonList(groupCondition11));

        final RuleIrAttributeCondition attributeCondition111 = createRuleIrAttributeCondition("code",
                RuleIrAttributeOperator.IN, Lists.newArrayList("1234567"), "product");

        final RuleIrAttributeRelCondition attributeRelCondition112 = createRuleIrAttributeRelCondition("product",
                RuleIrAttributeOperator.EQUAL, "orderEntry", "product");
        final RuleIrAttributeCondition attributeCondition113 = createRuleIrAttributeCondition("quantity",
                RuleIrAttributeOperator.GREATER_THAN_OR_EQUAL, new Integer(1), "orderEntry");
        final RuleIrAttributeRelCondition attributeRelCondition114 = createRuleIrAttributeRelCondition("entries",
                RuleIrAttributeOperator.CONTAINS, "cart", "orderEntry");

        groupCondition11.setChildren(
                Lists.newArrayList(attributeCondition111, attributeRelCondition112, attributeCondition113, attributeRelCondition114));

        final RuleIrGroupCondition groupCondition21 = createIrGroupCondition(RuleIrGroupOperator.AND);
        final RuleIrGroupCondition groupCondition2 = createIrGroupCondition(RuleIrGroupOperator.AND, Collections.singletonList(groupCondition21));

        final RuleIrAttributeCondition attributeCondition211 = createRuleIrAttributeCondition("code",
                RuleIrAttributeOperator.IN, Lists.newArrayList("902"), "category");

        final RuleIrAttributeRelCondition attributeRelCondition212 = createRuleIrAttributeRelCondition("categories",
                RuleIrAttributeOperator.CONTAINS, "product", "category");
        final RuleIrAttributeCondition attributeCondition213 = createRuleIrAttributeCondition("quantity",
                RuleIrAttributeOperator.GREATER_THAN_OR_EQUAL, new Integer(1), "orderEntry");
        final RuleIrAttributeRelCondition attributeRelCondition214 = createRuleIrAttributeRelCondition("entries",
                RuleIrAttributeOperator.CONTAINS, "cart", "orderEntry");
        final RuleIrAttributeRelCondition attributeRelCondition215 = createRuleIrAttributeRelCondition("product",
                RuleIrAttributeOperator.EQUAL, "orderEntry", "product");

        groupCondition21.setChildren(
                Lists.newArrayList(attributeCondition211, attributeRelCondition212, attributeRelCondition215, attributeCondition213,
                        attributeRelCondition214));

        final RuleIrGroupCondition groupCondition31 = createIrGroupCondition(RuleIrGroupOperator.AND);
        final RuleIrGroupCondition groupCondition3 = createIrGroupCondition(RuleIrGroupOperator.AND, Collections.singletonList(groupCondition31));

        ruleIrVariables.put(CART_VARIABLE_NAME, cartVariable);
        ruleIrVariables.put(PRODUCT_VARIABLE_NAME, productVariable);
        ruleIrVariables.put(ORDER_ENTRY_VARIABLE_NAME, orderEntryVariable);
        ruleIrVariables.put(CATEGORY_VARIABLE_NAME, categoryVariable);

        when(droolsRuleValueFormatter.formatValue(droolsRuleGeneratorContext, Arrays.asList("1234567")))
                .thenReturn("(\"1234567\")");
        when(droolsRuleValueFormatter.formatValue(droolsRuleGeneratorContext, Arrays.asList("902")))
                .thenReturn("(\"902\")");
        when(droolsRuleValueFormatter.formatValue(droolsRuleGeneratorContext, Integer.valueOf(1)))
                .thenReturn("new Integer(1)");

        return Lists.newArrayList(groupCondition1, groupCondition2, groupCondition3);
    }

    private List<RuleIrCondition> createGroupNotConditions()
    {
        final RuleIrGroupCondition groupCondition1 = createIrGroupCondition(RuleIrGroupOperator.AND);

        final RuleIrGroupCondition groupCondition111 = createIrGroupCondition(RuleIrGroupOperator.AND);
        final RuleIrGroupCondition groupCondition11 = createIrGroupCondition(RuleIrGroupOperator.OR, Collections.singletonList(groupCondition111));

        final RuleIrAttributeCondition attributeCondition111 = createRuleIrAttributeCondition("currencyIsoCode",
                RuleIrAttributeOperator.EQUAL, "USD", "cart");
        final RuleIrAttributeCondition attributeCondition112 = createRuleIrAttributeCondition("total",
                RuleIrAttributeOperator.GREATER_THAN_OR_EQUAL, new BigDecimal("100"), "cart");

        groupCondition111.setChildren(Lists.newArrayList(attributeCondition111, attributeCondition112));

        final RuleIrLocalVariablesContainer localVariablesContainer12 = new RuleIrLocalVariablesContainer();
        localVariablesContainer12.setVariables(ImmutableMap.of("action", actionVariable));

        final RuleIrNotCondition notCondition12 = new RuleIrNotCondition();
        notCondition12.setVariablesContainer(localVariablesContainer12);

        final RuleIrAttributeCondition attributeCondition121 = createRuleIrAttributeCondition("firedRuleCode",
                RuleIrAttributeOperator.EQUAL, "test_rule_code", "action");
        notCondition12.setChildren(Collections.singletonList(attributeCondition121));

        groupCondition1.setChildren(Lists.newArrayList(groupCondition11, notCondition12));

        ruleIrVariables.put(CART_VARIABLE_NAME, cartVariable);
        ruleIrVariables.put(ACTION_VARIABLE_NAME, actionVariable);

        when(droolsRuleValueFormatter.formatValue(droolsRuleGeneratorContext, "USD"))
                .thenReturn("\"USD\"");
        when(droolsRuleValueFormatter.formatValue(droolsRuleGeneratorContext, BigDecimal.valueOf(100)))
                .thenReturn("new BigDecimal(\"100\")");
        when(droolsRuleValueFormatter.formatValue(droolsRuleGeneratorContext, "test_rule_code"))
                .thenReturn("\"test_rule_code\"");

        return Lists.newArrayList(groupCondition1);
    }

    @Test
    public void testSingleNotCondition() throws IOException
    {
        // given
        final String expectedDroolsCode = getResourceAsString(
                "/compiler/generatedConditionsForSingleNotCondition.bin");

        final RuleIrLocalVariablesContainer varContainer = new RuleIrLocalVariablesContainer();
        varContainer.setVariables(ruleIrVariables);
        ruleIrVariables.put(productVariable.getName(), productVariable);
        ruleIrVariables.put(orderEntryVariable.getName(), orderEntryVariable);

        final RuleIrAttributeRelCondition entry = createRuleIrAttributeRelCondition(productVariable.getName(),
                RuleIrAttributeOperator.EQUAL, orderEntryVariable.getName(), productVariable.getName());

        final RuleIrExistsCondition exists = new RuleIrExistsCondition();
        exists.setVariablesContainer(varContainer);
        exists.setChildren(Collections.singletonList(entry));

        final RuleIrNotCondition irNot = new RuleIrNotCondition();
        irNot.setChildren(Collections.singletonList(exists));

        ruleIr.setConditions(Collections.singletonList(irNot));
        final String generatedDroolsCode = conditionsGenerator.generateConditions(droolsRuleGeneratorContext, INDENTATION);
        assertEquals(expectedDroolsCode, generatedDroolsCode);

    }

    @Test
    public void testSingleCondition() throws IOException
    {
        // given
        final String expectedDroolsCode = getResourceAsString(
                "/compiler/generatedConditionsForSingleCondition.bin");

        final BigDecimal totalValue = BigDecimal.valueOf(20);

        final RuleIrAttributeCondition amountCondition = createRuleIrAttributeCondition("total",
                RuleIrAttributeOperator.GREATER_THAN, totalValue, CART_VARIABLE_NAME);

        ruleIrVariables.put(CART_VARIABLE_NAME, cartVariable);
        ruleIrVariables.put(RESULT_VARIABLE_NAME, resultVariable);
        ruleIr.setConditions(Arrays.asList(ruleIrResultCondition, amountCondition));

        when(droolsRuleValueFormatter.formatValue(droolsRuleGeneratorContext, totalValue)).thenReturn(
                "new java.math.BigDecimal(\"100\")");

        // when
        final String generatedDroolsCode = conditionsGenerator.generateConditions(droolsRuleGeneratorContext, INDENTATION);

        // then
        assertEquals(expectedDroolsCode, generatedDroolsCode);
    }

    @Test
    public void testMultipleConditions() throws IOException
    {
        // given
        final String expectedDroolsCode = getResourceAsString(
                "/compiler/generatedConditionsForMultipleConditions.bin");


        final String colorValue = "blue";
        final RuleIrAttributeCondition colorCondition = createRuleIrAttributeCondition("color",
                RuleIrAttributeOperator.EQUAL, colorValue, PRODUCT_VARIABLE_NAME);

        final BigDecimal totalValue = BigDecimal.valueOf(100);
        final RuleIrAttributeCondition amountCondition = createRuleIrAttributeCondition("total",
                RuleIrAttributeOperator.GREATER_THAN, totalValue, CART_VARIABLE_NAME);

        final RuleIrGroupCondition blueOrCartCondition = createIrGroupCondition(RuleIrGroupOperator.AND, Arrays.asList(colorCondition, amountCondition));

        final List<String> codeValues = Arrays.asList("123", "456", "789");
        final RuleIrAttributeCondition code = createRuleIrAttributeCondition("code",
                RuleIrAttributeOperator.IN, codeValues, PRODUCT_VARIABLE_NAME);

        final RuleIrGroupCondition group = createIrGroupCondition(RuleIrGroupOperator.OR, Arrays.asList(code, blueOrCartCondition));

        ruleIrVariables.put(PRODUCT_VARIABLE_NAME, productVariable);
        ruleIrVariables.put(CART_VARIABLE_NAME, cartVariable);
        ruleIrVariables.put(RESULT_VARIABLE_NAME, resultVariable);
        ruleIr.setConditions(Arrays.asList(ruleIrResultCondition, group));

        when(droolsRuleValueFormatter.formatValue(droolsRuleGeneratorContext, colorValue)).thenReturn("\"blue\"");
        when(droolsRuleValueFormatter.formatValue(droolsRuleGeneratorContext, totalValue)).thenReturn(
                "new java.math.BigDecimal(\"100\")");
        when(droolsRuleValueFormatter.formatValue(droolsRuleGeneratorContext, codeValues)).thenReturn("(\"123\",\"456\",\"789\")");

        // when
        final String generatedDroolsCode = conditionsGenerator.generateConditions(droolsRuleGeneratorContext, INDENTATION);

        // then
        assertEquals(expectedDroolsCode, generatedDroolsCode);
    }

    @Test
    public void testSingleRelConditionWithTargetAttribute() throws IOException
    {
        // given
        final String expectedDroolsCode = getResourceAsString(
                "/compiler/generatedConditionsForSingleRelConditionWithTargetAttribute.bin");

        final Integer quantity = Integer.valueOf(1);

        final RuleIrAttributeCondition amountCondition = createRuleIrAttributeCondition("quantity",
                RuleIrAttributeOperator.GREATER_THAN_OR_EQUAL, quantity, ORDER_ENTRY_VARIABLE_NAME);

        final RuleIrAttributeRelCondition relationCondition = createRuleIrAttributeRelCondition("entryGroupId",
                RuleIrAttributeOperator.MEMBER_OF, ENTRYGROUP_VARIABLE_NAME, ORDER_ENTRY_VARIABLE_NAME);
        relationCondition.setTargetAttribute("entryGroupNumbers");

        ruleIrVariables.put(ORDER_ENTRY_VARIABLE_NAME, orderEntryVariable);
        ruleIrVariables.put(ENTRYGROUP_VARIABLE_NAME, entryGroupVariable);
        ruleIr.setConditions(Arrays.asList(amountCondition, relationCondition));

        when(droolsRuleValueFormatter.formatValue(droolsRuleGeneratorContext, quantity)).thenReturn("new Integer(1)");

        final String generatedDroolsCode = conditionsGenerator.generateConditions(droolsRuleGeneratorContext, INDENTATION);
        assertEquals(expectedDroolsCode, generatedDroolsCode);
    }

    @Test
    public void testSinglePatternWhenCondition() throws IOException
    {
        // given
        final String expectedDroolsCode = getResourceAsString(
                "/compiler/generatedConditionsForPatternWhenCondition.bin");
        final List<RuleIrCondition> patternConditions = createPatternConditions();
        final List<RuleIrCondition> conditions = ruleIr.getConditions();
        if (CollectionUtils.isNotEmpty(conditions))
        {
            conditions.addAll(patternConditions);
        }
        else
        {
            ruleIr.setConditions(patternConditions);
        }

        // when
        final String generatedDroolsCode = conditionsGenerator.generateRequiredFactsCheckPattern(droolsRuleGeneratorContext);

        // then
        assertEquals(expectedDroolsCode, generatedDroolsCode);
    }

    @Test
    public void testPatternAndGroupWhenConditions() throws IOException
    {
        // given
        final String expectedDroolsCode = getResourceAsString(
                "/compiler/generatedConditionsForPatternAndGroupWhenConditions.bin");
        final List<RuleIrCondition> patternConditions = createPatternConditions();
        final List<RuleIrCondition> groupConditions = createGroupConditions();
        final List<RuleIrCondition> conditions = ruleIr.getConditions();
        if (CollectionUtils.isNotEmpty(conditions))
        {
            conditions.addAll(patternConditions);
        }
        else
        {
            ruleIr.setConditions(patternConditions);
        }
        ruleIr.getConditions().addAll(groupConditions);

        // when
        final String generatedDroolsCode = conditionsGenerator.generateRequiredFactsCheckPattern(droolsRuleGeneratorContext);

        // then
        assertEquals(expectedDroolsCode, generatedDroolsCode);
    }

    @Test
    public void testGroupWhenConditions() throws IOException
    {
        // given
        final String expectedDroolsCode = getResourceAsString(
                "/compiler/generatedConditionsForGroupWhenConditions.bin");
        final List<RuleIrCondition> conditions = ruleIr.getConditions();
        final List<RuleIrCondition> groupConditions = createGroupConditions();
        if (CollectionUtils.isNotEmpty(conditions))
        {
            conditions.addAll(groupConditions);
        }
        else
        {
            ruleIr.setConditions(groupConditions);
        }

        // when
        final String generatedDroolsCode = conditionsGenerator.generateRequiredFactsCheckPattern(droolsRuleGeneratorContext);

        // then
        assertEquals(expectedDroolsCode, generatedDroolsCode);
    }

    @Test
    public void testNotWhenConditions() throws IOException
    {
        // given
        final String expectedDroolsCode = getResourceAsString(
                "/compiler/generatedConditionsForGroupNotWhenConditions.bin");

        final List<RuleIrCondition> conditions = ruleIr.getConditions();
        final List<RuleIrCondition> groupConditions = createGroupNotConditions();
        if (CollectionUtils.isNotEmpty(conditions))
        {
            conditions.addAll(groupConditions);
        }
        else
        {
            ruleIr.setConditions(groupConditions);
        }

        // when
        final String generatedDroolsCode = conditionsGenerator.generateRequiredFactsCheckPattern(droolsRuleGeneratorContext);

        // then
        assertEquals(expectedDroolsCode, generatedDroolsCode);
    }

}
