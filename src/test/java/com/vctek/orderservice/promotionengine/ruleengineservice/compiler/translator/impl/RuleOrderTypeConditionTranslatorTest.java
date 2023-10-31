package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.impl;

import com.vctek.orderservice.promotionengine.ruledefinition.enums.MembershipOperator;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrAttributeCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrGroupCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleIrGroupOperator;
import com.vctek.util.OrderType;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class RuleOrderTypeConditionTranslatorTest extends AbstractRuleConditionTranslatorTest {
    private List<String> values = new ArrayList<>();

    @Before
    public void setUp() {
        valueParameter.setValue(values);
        setTranslator(new RuleOrderTypeConditionTranslator());
    }

    @Test
    public void translate() {
        values.add(OrderType.RETAIL.toString());
        values.add(OrderType.ONLINE.toString());
        values.add(OrderType.WHOLESALE.toString());
        operatorParameter.setValue(MembershipOperator.IN);

        RuleIrCondition ruleIrCondition = translator.translate(context, condition, conditionDefinition);
        assertThat(ruleIrCondition, instanceOf(RuleIrGroupCondition.class));
        RuleIrGroupCondition ruleIrGroupCondition = (RuleIrGroupCondition) ruleIrCondition;
        assertEquals(RuleIrGroupOperator.OR, ruleIrGroupCondition.getOperator());
        List<RuleIrCondition> children = ruleIrGroupCondition.getChildren();
        assertEquals(1, children.size());
        RuleIrCondition condition = children.get(0);
        assertThat(condition, instanceOf(RuleIrAttributeCondition.class));

        RuleIrAttributeCondition attributeCondition = (RuleIrAttributeCondition) condition;
        assertEquals(values, attributeCondition.getValue());
        assertEquals(RuleOrderTypeConditionTranslator.ORDER_TYPE_ATTRIBUTE, attributeCondition.getAttribute());
        assertEquals(MembershipOperator.IN.toString(), attributeCondition.getOperator().value());
    }
}
