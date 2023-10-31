package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.impl;

import com.google.common.collect.Lists;
import com.vctek.orderservice.promotionengine.ruledefinition.condition.builders.IrConditions;
import com.vctek.orderservice.promotionengine.ruledefinition.condition.builders.RuleIrAttributeConditionBuilder;
import com.vctek.orderservice.promotionengine.ruledefinition.condition.builders.RuleIrAttributeRelConditionBuilder;
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
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.OrderEntryRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.ProductConsumedRAO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component("ruleProductPriceConditionTranslator")
public class RuleProductPriceConditionTranslator extends AbstractRuleConditionTranslator {

    public RuleIrCondition translate(RuleCompilerContext context, RuleConditionData condition, RuleConditionDefinitionData conditionDefinition) {
        Map<String, RuleParameterData> conditionParameters = condition.getParameters();
        RuleParameterData operatorParameter = conditionParameters.get("operator");
        RuleParameterData valueParameter = conditionParameters.get("value");
        if (this.verifyAllPresent(new Object[]{operatorParameter, valueParameter})) {
            AmountOperator operator = (AmountOperator) operatorParameter.getValue();
            Map<String, BigDecimal> value = (Map) valueParameter.getValue();
            if (this.verifyAllPresent(new Object[]{operator, value})) {
                return this.getProductPriceConditions(context, operator, value);
            }
        }

        return IrConditions.newIrRuleFalseCondition();
    }

    protected RuleIrGroupCondition getProductPriceConditions(RuleCompilerContext context, AmountOperator operator, Map<String, BigDecimal> value) {
        RuleIrGroupCondition irGroupCondition = RuleIrGroupConditionBuilder.newGroupConditionOf(RuleIrGroupOperator.OR).build();
        this.addProductPriceConditions(context, operator, value, irGroupCondition);
        return irGroupCondition;
    }

    protected void addProductPriceConditions(RuleCompilerContext context, AmountOperator operator, Map<String, BigDecimal> value, RuleIrGroupCondition irGroupCondition) {
        String orderEntryRaoVariable = context.generateVariable(OrderEntryRAO.class);
        String cartRaoVariable = context.generateVariable(CartRAO.class);
        String productConsumedRaoVariable = context.generateVariable(ProductConsumedRAO.class);
        Iterator var9 = value.entrySet().iterator();

        while (var9.hasNext()) {
            Map.Entry<String, BigDecimal> entry = (Map.Entry) var9.next();
            if (this.verifyAllPresent(new Object[]{entry.getKey(), entry.getValue()})) {
                List<RuleIrCondition> conditions = Lists.newArrayList();
                RuleIrGroupCondition irCurrencyGroupCondition = RuleIrGroupConditionBuilder
                        .newGroupConditionOf(RuleIrGroupOperator.AND)
                        .withChildren(conditions).build();
                conditions.add(RuleIrAttributeConditionBuilder
                        .newAttributeConditionFor(cartRaoVariable)
                        .withAttribute(ORDER_RAO_CURRENCY_ATTRIBUTE)
                        .withOperator(RuleIrAttributeOperator.EQUAL)
                        .withValue(entry.getKey()).build());
                conditions.add(RuleIrAttributeConditionBuilder
                        .newAttributeConditionFor(orderEntryRaoVariable)
                        .withAttribute(ORDER_ENTRY_RAO_BASE_PRICE_ATTRIBUTE)
                        .withOperator(RuleIrAttributeOperator.valueOf(operator.name()))
                        .withValue(entry.getValue()).build());
                conditions.add(RuleIrAttributeRelConditionBuilder.newAttributeRelationConditionFor(cartRaoVariable)
                        .withAttribute(ORDER_RAO_ENTRIES_ATTRIBUTE).withOperator(RuleIrAttributeOperator.CONTAINS)
                        .withTargetVariable(orderEntryRaoVariable).build());
                conditions.add(RuleIrAttributeRelConditionBuilder.newAttributeRelationConditionFor(productConsumedRaoVariable)
                        .withAttribute(PRODUCT_CONSUMED_RAO_ENTRY_ATTRIBUTE).withOperator(RuleIrAttributeOperator.EQUAL)
                        .withTargetVariable(orderEntryRaoVariable).build());
                conditions.add(RuleIrAttributeConditionBuilder.newAttributeConditionFor(productConsumedRaoVariable)
                        .withAttribute(AVAILABLE_QUANTITY_PARAM).withOperator(RuleIrAttributeOperator.GREATER_THAN_OR_EQUAL)
                        .withValue(1).build());
                irGroupCondition.getChildren().add(irCurrencyGroupCondition);
            }
        }

    }
}
