package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.impl;

import com.google.common.collect.Lists;
import com.vctek.orderservice.promotionengine.ruledefinition.condition.builders.IrConditions;
import com.vctek.orderservice.promotionengine.ruledefinition.condition.builders.RuleIrAttributeConditionBuilder;
import com.vctek.orderservice.promotionengine.ruledefinition.condition.builders.RuleIrAttributeRelConditionBuilder;
import com.vctek.orderservice.promotionengine.ruledefinition.condition.builders.RuleIrGroupConditionBuilder;
import com.vctek.orderservice.promotionengine.ruledefinition.enums.AmountOperator;
import com.vctek.orderservice.promotionengine.ruledefinition.enums.CollectionOperator;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.*;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleIrAttributeOperator;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleIrGroupOperator;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.CartRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.OrderEntryRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.ProductConsumedRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.ProductRAO;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component("ruleQualifyingProductsConditionTranslator")
public class RuleQualifyingProductsConditionTranslator extends AbstractRuleConditionTranslator {
    public static final String PRODUCTS_OPERATOR_PARAM = "products_operator";

    public final RuleIrCondition translate(RuleCompilerContext context, RuleConditionData condition, RuleConditionDefinitionData conditionDefinition) {
        Map<String, RuleParameterData> conditionParameters = condition.getParameters();
        RuleParameterData operatorParameter = conditionParameters.get(OPERATOR_PARAM);
        RuleParameterData quantityParameter = conditionParameters.get(QUANTITY_PARAM);
        RuleParameterData productsOperatorParameter = conditionParameters.get(PRODUCTS_OPERATOR_PARAM);
        RuleParameterData productsParameter = conditionParameters.get(PRODUCTS_PARAM);
        if (this.verifyAllPresent(new Object[]{operatorParameter, quantityParameter, productsOperatorParameter, productsParameter})) {
            AmountOperator operator = (AmountOperator)operatorParameter.getValue();
            Integer quantity = (Integer)quantityParameter.getValue();
            CollectionOperator productsOperator = (CollectionOperator)productsOperatorParameter.getValue();
            List<String> products = (List)productsParameter.getValue();
            if (this.verifyAllPresent(new Object[]{operator, quantity, productsOperator, products})) {
                return this.getQualifyingProductsCondition(context, operator, quantity, productsOperator, products);
            }
        }

        return IrConditions.newIrRuleFalseCondition();
    }

    protected RuleIrGroupCondition getQualifyingProductsCondition(RuleCompilerContext context, AmountOperator operator,
                              Integer quantity, CollectionOperator productsOperator, List<String> products) {
        RuleIrGroupCondition irQualifyingProductsCondition = RuleIrGroupConditionBuilder
                .newGroupConditionOf(RuleIrGroupOperator.AND).build();
        this.addQualifyingProductsCondition(context, operator, quantity, productsOperator,
                products, irQualifyingProductsCondition);
        return irQualifyingProductsCondition;
    }

    protected void addQualifyingProductsCondition(RuleCompilerContext context, AmountOperator operator, Integer quantity,
                                                CollectionOperator productsOperator, List<String> products,
                                                  RuleIrGroupCondition irQualifyingProductsCondition) {

        String productRaoVariable = context.generateVariable(ProductRAO.class);
        String orderEntryRaoVariable = context.generateVariable(OrderEntryRAO.class);
        String cartRaoVariable = context.generateVariable(CartRAO.class);
        String productConsumedRaoVariable = context.generateVariable(ProductConsumedRAO.class);
        List<RuleIrCondition> irConditions = Lists.newArrayList();
        RuleIrGroupCondition baseProductOrGroupCondition = RuleIrGroupConditionBuilder
                .newGroupConditionOf(RuleIrGroupOperator.OR).build();
        baseProductOrGroupCondition.getChildren().add(RuleIrAttributeConditionBuilder
                .newAttributeConditionFor(productRaoVariable).withAttribute(PRODUCT_RAO_CODE_ATTRIBUTE)
                .withOperator(RuleIrAttributeOperator.IN).withValue(products).build());
//        Iterator var14 = products.iterator();
//
//        while(var14.hasNext()) {
//            String product = (String)var14.next();
//            baseProductOrGroupCondition.getChildren().add(RuleIrAttributeConditionBuilder
//                    .newAttributeConditionFor(productRaoVariable).withAttribute("baseProductCodes")
//                    .withOperator(RuleIrAttributeOperator.CONTAINS).withValue(product).build());
//        }

        irConditions.add(baseProductOrGroupCondition);
        irConditions.add(RuleIrAttributeRelConditionBuilder
                .newAttributeRelationConditionFor(orderEntryRaoVariable)
                .withAttribute(ORDER_ENTRY_RAO_PRODUCT_ATTRIBUTE)
                .withOperator(RuleIrAttributeOperator.EQUAL)
                .withTargetVariable(productRaoVariable).build());
        irConditions.add(RuleIrAttributeConditionBuilder
                .newAttributeConditionFor(orderEntryRaoVariable)
                .withAttribute(QUANTITY_PARAM)
                .withOperator(RuleIrAttributeOperator.valueOf(operator.name()))
                .withValue(quantity).build());
        irConditions.add(RuleIrAttributeRelConditionBuilder
                .newAttributeRelationConditionFor(cartRaoVariable)
                .withAttribute(ORDER_RAO_ENTRIES_ATTRIBUTE)
                .withOperator(RuleIrAttributeOperator.CONTAINS)
                .withTargetVariable(orderEntryRaoVariable).build());
        irConditions.add(RuleIrAttributeRelConditionBuilder
                .newAttributeRelationConditionFor(productConsumedRaoVariable)
                .withAttribute(PRODUCT_CONSUMED_RAO_ENTRY_ATTRIBUTE)
                .withOperator(RuleIrAttributeOperator.EQUAL)
                .withTargetVariable(orderEntryRaoVariable).build());
        irConditions.add(RuleIrAttributeConditionBuilder
                .newAttributeConditionFor(productConsumedRaoVariable)
                .withAttribute(AVAILABLE_QUANTITY_PARAM)
                .withOperator(RuleIrAttributeOperator.GREATER_THAN_OR_EQUAL)
                .withValue(1).build());
        this.evaluateProductsOperator(context, operator, quantity, productsOperator, products,
                irQualifyingProductsCondition, irConditions);
    }

    protected void evaluateProductsOperator(RuleCompilerContext context, AmountOperator operator,
            Integer quantity, CollectionOperator productsOperator, List<String> products,
            RuleIrGroupCondition irQualifyingProductsCondition, List<RuleIrCondition> irConditions) {
        if (CollectionOperator.NOT_CONTAINS.equals(productsOperator)) {
            RuleIrNotCondition irNotProductCondition = new RuleIrNotCondition();
            irNotProductCondition.setChildren(irConditions);
            irQualifyingProductsCondition.getChildren().add(irNotProductCondition);
        } else {
            irQualifyingProductsCondition.getChildren().addAll(irConditions);
            if (CollectionOperator.CONTAINS_ALL.equals(productsOperator)) {
                this.addContainsAllProductsConditions(context, operator, quantity, products,
                        irQualifyingProductsCondition);
            }
        }

    }

    protected void addContainsAllProductsConditions(RuleCompilerContext context, AmountOperator operator, Integer quantity, List<String> products, RuleIrGroupCondition irQualifyingProductsCondition) {
        String cartRaoVariable = context.generateVariable(CartRAO.class);
        Iterator var8 = products.iterator();

        while(var8.hasNext()) {
            String product = (String)var8.next();
            RuleIrLocalVariablesContainer variablesContainer = context.createLocalContainer();
            String containsProductRaoVariable = context.generateLocalVariable(variablesContainer, ProductRAO.class);
            String containsOrderEntryRaoVariable = context.generateLocalVariable(variablesContainer, OrderEntryRAO.class);
            String productConsumedRaoVariable = context.generateVariable(ProductConsumedRAO.class);
            List<RuleIrCondition> irConditions = Lists.newArrayList();
            irConditions.add(RuleIrAttributeConditionBuilder
                    .newAttributeConditionFor(containsProductRaoVariable)
                    .withAttribute("code")
                    .withOperator(RuleIrAttributeOperator.EQUAL)
                    .withValue(product).build());
            irConditions.add(RuleIrAttributeRelConditionBuilder
                    .newAttributeRelationConditionFor(containsOrderEntryRaoVariable)
                    .withAttribute("product")
                    .withOperator(RuleIrAttributeOperator.EQUAL)
                    .withTargetVariable(containsProductRaoVariable).build());
            irConditions.add(RuleIrAttributeConditionBuilder
                    .newAttributeConditionFor(containsOrderEntryRaoVariable)
                    .withAttribute("quantity")
                    .withOperator(RuleIrAttributeOperator.valueOf(operator.name()))
                    .withValue(quantity).build());
            irConditions.add(RuleIrAttributeRelConditionBuilder
                    .newAttributeRelationConditionFor(cartRaoVariable)
                    .withAttribute("entries")
                    .withOperator(RuleIrAttributeOperator.CONTAINS)
                    .withTargetVariable(containsOrderEntryRaoVariable).build());
            irConditions.add(RuleIrAttributeRelConditionBuilder
                    .newAttributeRelationConditionFor(productConsumedRaoVariable)
                    .withAttribute("orderEntry")
                    .withOperator(RuleIrAttributeOperator.EQUAL)
                    .withTargetVariable(containsOrderEntryRaoVariable).build());

            irConditions.add(RuleIrAttributeConditionBuilder
                    .newAttributeConditionFor(productConsumedRaoVariable)
                    .withAttribute("availableQuantity")
                    .withOperator(RuleIrAttributeOperator.GREATER_THAN_OR_EQUAL)
                    .withValue(1).build());

            RuleIrExistsCondition irExistsProductCondition = new RuleIrExistsCondition();
            irExistsProductCondition.setVariablesContainer(variablesContainer);
            irExistsProductCondition.setChildren(irConditions);
            irQualifyingProductsCondition.getChildren().add(irExistsProductCondition);
        }

    }
}