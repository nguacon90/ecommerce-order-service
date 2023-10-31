package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.impl;

import com.vctek.orderservice.promotionengine.ruledefinition.enums.AmountOperator;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrAttributeCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrGroupCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleIrAttributeOperator;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleIrGroupOperator;
import com.vctek.orderservice.promotionengine.util.CurrencyIsoCode;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class RuleCartTotalConditionTranslatorTest extends AbstractRuleConditionTranslatorTest {

    private Map<String, BigDecimal> value = new HashMap<>();

    @Before
    public void setUp() {
        valueParameter.setValue(value);
        super.setTranslator(new RuleCartTotalConditionTranslator());
    }

    @Test
    public void translate() {
        value.put(CurrencyIsoCode.VND.toString(), new BigDecimal(2000000));
        operatorParameter.setValue(AmountOperator.GREATER_THAN_OR_EQUAL);

        RuleIrCondition ruleIrCondition = translator.translate(context, condition, conditionDefinition);
        assertThat(ruleIrCondition, instanceOf(RuleIrGroupCondition.class));
        RuleIrGroupCondition condition = (RuleIrGroupCondition) ruleIrCondition;
        assertEquals(RuleIrGroupOperator.OR, condition.getOperator());
        checkConditionChild(condition);
    }

    protected void checkConditionChild(final RuleIrGroupCondition condition)
    {
        assertThat(condition, CoreMatchers.instanceOf(RuleIrGroupCondition.class));

        final List<RuleIrCondition> ruleIrGroupConditions = condition.getChildren();
        assertEquals(1, ruleIrGroupConditions.size());
        RuleIrCondition ruleIrCondition = ruleIrGroupConditions.get(0);
        assertThat(ruleIrCondition, instanceOf(RuleIrGroupCondition.class));
        RuleIrGroupCondition irGroupCondition = (RuleIrGroupCondition) ruleIrCondition;
        List<RuleIrCondition> children = irGroupCondition.getChildren();
        assertEquals(2, children.size());
        assertThat(children.get(0), CoreMatchers.instanceOf(RuleIrAttributeCondition.class));
        final RuleIrAttributeCondition currencyAttribute = (RuleIrAttributeCondition) children.get(0);
        assertEquals(RuleIrAttributeOperator.EQUAL, currencyAttribute.getOperator());
        assertEquals(RuleCartTotalConditionTranslator.ORDER_RAO_CURRENCY_ATTRIBUTE, currencyAttribute.getAttribute());
        assertEquals(CurrencyIsoCode.VND.toString(), currencyAttribute.getValue());

        assertThat(children.get(1), CoreMatchers.instanceOf(RuleIrAttributeCondition.class));
        final RuleIrAttributeCondition ruleIrTotalCondition = (RuleIrAttributeCondition) children.get(1);
        assertEquals(RuleIrAttributeOperator.GREATER_THAN_OR_EQUAL, ruleIrTotalCondition.getOperator());
        assertEquals(RuleCartTotalConditionTranslator.ORDER_RAO_TOTAL_ATTRIBUTE, ruleIrTotalCondition.getAttribute());
        assertEquals(new BigDecimal(2000000), ruleIrTotalCondition.getValue());
    }
}
