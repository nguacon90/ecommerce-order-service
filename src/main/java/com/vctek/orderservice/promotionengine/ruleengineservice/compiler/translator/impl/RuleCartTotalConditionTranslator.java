package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.impl;

import com.vctek.orderservice.promotionengine.ruledefinition.condition.builders.IrConditions;
import com.vctek.orderservice.promotionengine.ruledefinition.condition.builders.RuleIrAttributeConditionBuilder;
import com.vctek.orderservice.promotionengine.ruledefinition.condition.builders.RuleIrGroupConditionBuilder;
import com.vctek.orderservice.promotionengine.ruledefinition.enums.AmountOperator;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrGroupCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleIrAttributeOperator;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleIrGroupOperator;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.CartRAO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component("ruleCartTotalConditionTranslator")
public class RuleCartTotalConditionTranslator extends AbstractRuleConditionTranslator {

    @Override
    public RuleIrCondition translate(RuleCompilerContext context, RuleConditionData condition,
                                     RuleConditionDefinitionData conditionDefinition) {
        Map<String, RuleParameterData> conditionParameters = condition.getParameters();
        RuleParameterData operatorParameter = conditionParameters.get(OPERATOR_PARAM);
        RuleParameterData valueParameter = conditionParameters.get(VALUE_PARAM);
        if (this.verifyAllPresent(new Object[]{operatorParameter, valueParameter})) {
            AmountOperator operator = (AmountOperator)operatorParameter.getValue();
            Map<String, BigDecimal> value = (Map)valueParameter.getValue();
            if (this.verifyAllPresent(new Object[]{operator, value})) {
                return this.getCartTotalConditions(context, operator, value);
            }
        }

        return IrConditions.newIrRuleFalseCondition();
    }

    protected RuleIrGroupCondition getCartTotalConditions(RuleCompilerContext context, AmountOperator operator, Map<String, BigDecimal> value) {
        RuleIrGroupCondition irOrderTotalCondition = RuleIrGroupConditionBuilder.newGroupConditionOf(RuleIrGroupOperator.OR).build();
        this.addCartTotalConditions(context, operator, value, irOrderTotalCondition);
        return irOrderTotalCondition;
    }

    protected void addCartTotalConditions(RuleCompilerContext context, AmountOperator operator, Map<String, BigDecimal> value, RuleIrGroupCondition irOrderTotalCondition) {
        String orderRaoVariable = context.generateVariable(CartRAO.class);
        Iterator iterator = value.entrySet().iterator();

        while(iterator.hasNext()) {
            Map.Entry<String, BigDecimal> entry = (Map.Entry)iterator.next();
            if (this.verifyAllPresent(new Object[]{entry.getKey(), entry.getValue()})) {
                RuleIrGroupCondition irCurrencyGroupCondition = RuleIrGroupConditionBuilder.newGroupConditionOf(RuleIrGroupOperator.AND).build();
                List<RuleIrCondition> ruleIrConditions = irCurrencyGroupCondition.getChildren();
                ruleIrConditions.add(RuleIrAttributeConditionBuilder.newAttributeConditionFor(orderRaoVariable)
                        .withAttribute(ORDER_RAO_CURRENCY_ATTRIBUTE)
                        .withOperator(RuleIrAttributeOperator.EQUAL)
                        .withValue(entry.getKey()).build());
                ruleIrConditions.add(RuleIrAttributeConditionBuilder
                        .newAttributeConditionFor(orderRaoVariable)
                        .withAttribute(ORDER_RAO_TOTAL_ATTRIBUTE)
                        .withOperator(RuleIrAttributeOperator.valueOf(operator.name()))
                        .withValue(entry.getValue()).build());
                irOrderTotalCondition.getChildren().add(irCurrencyGroupCondition);
            }
        }


    }
}
