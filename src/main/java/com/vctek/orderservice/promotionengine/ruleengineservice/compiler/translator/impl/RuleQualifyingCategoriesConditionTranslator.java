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
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("ruleQualifyingCategoriesConditionTranslator")
public class RuleQualifyingCategoriesConditionTranslator extends AbstractRuleConditionTranslator {
    public static final String EXCLUDED_CATEGORIES_PARAM = "excluded_categories";
    public static final String EXCLUDED_PRODUCTS_PARAM = "excluded_products";

    @Override
    public RuleIrCondition translate(RuleCompilerContext context, RuleConditionData condition,
                                     RuleConditionDefinitionData conditionDefinition) {
        Map<String, RuleParameterData> conditionParameters = condition.getParameters();
        RuleParameterData operatorParameter = conditionParameters.get(OPERATOR_PARAM);
        RuleParameterData quantityParameter = conditionParameters.get(QUANTITY_PARAM);
        RuleParameterData categoriesOperatorParameter = conditionParameters.get(CATEGORIES_OPERATOR_PARAM);
        RuleParameterData categoriesParameter = conditionParameters.get(CATEGORIES_PARAM);
        RuleParameterData excludedCategoriesParameter = conditionParameters.get(EXCLUDED_CATEGORIES_PARAM);
        RuleParameterData excludedProductsParameter = conditionParameters.get(EXCLUDED_PRODUCTS_PARAM);
        Object[] objectParameters = {operatorParameter, quantityParameter, categoriesOperatorParameter, categoriesParameter};
        if (this.verifyAllPresent(objectParameters)) {
            AmountOperator operator = (AmountOperator)operatorParameter.getValue();
            Integer quantity = (Integer)quantityParameter.getValue();
            CollectionOperator categoriesOperator = (CollectionOperator)categoriesOperatorParameter.getValue();
            List<Long> categories = (List)categoriesParameter.getValue();
            if (this.verifyAllPresent(new Object[]{operator, quantity, categoriesOperator, categories})) {
                RuleIrGroupCondition irQualifyingCategoriesCondition = RuleIrGroupConditionBuilder
                        .newGroupConditionOf(RuleIrGroupOperator.AND).build();

                this.addQualifyingCategoriesConditions(context, operator, quantity,
                        categoriesOperator, categories, irQualifyingCategoriesCondition);
                if (!CollectionOperator.NOT_CONTAINS.equals(categoriesOperator)) {
                    this.addExcludedProductsAndCategoriesConditions(context, excludedCategoriesParameter,
                            excludedProductsParameter, irQualifyingCategoriesCondition);
                }

                return irQualifyingCategoriesCondition;
            }
        }

        return IrConditions.newIrRuleFalseCondition();
    }

    protected void addQualifyingCategoriesConditions(RuleCompilerContext context, AmountOperator operator, Integer quantity,
                                                     CollectionOperator categoriesOperator, List<Long> categories,
                                                     RuleIrGroupCondition irQualifyingCategoriesCondition) {
        String categoryRaoVariable = context.generateVariable(CategoryRAO.class);
        String productRaoVariable = context.generateVariable(ProductRAO.class);
        String orderEntryRaoVariable = context.generateVariable(OrderEntryRAO.class);
        String cartRaoVariable = context.generateVariable(CartRAO.class);
        String productConsumedRaoVariable = context.generateVariable(ProductConsumedRAO.class);
        List<RuleIrCondition> irConditions = Lists.newArrayList();
        irConditions.add(RuleIrAttributeConditionBuilder
                .newAttributeConditionFor(categoryRaoVariable).withAttribute(CATEGORY_RAO_CODE_ATTRIBUTE)
                .withOperator(RuleIrAttributeOperator.IN).withValue(categories).build());
        irConditions.add(RuleIrAttributeRelConditionBuilder
                .newAttributeRelationConditionFor(productRaoVariable)
                .withAttribute(PRODUCT_RAO_CATEGORIES_ATTRIBUTE)
                .withOperator(RuleIrAttributeOperator.CONTAINS)
                .withTargetVariable(categoryRaoVariable).build());
        irConditions.add(RuleIrAttributeRelConditionBuilder
                .newAttributeRelationConditionFor(orderEntryRaoVariable)
                .withAttribute(ORDER_ENTRY_RAO_PRODUCT_ATTRIBUTE)
                .withOperator(RuleIrAttributeOperator.EQUAL)
                .withTargetVariable(productRaoVariable).build());
        irConditions.add(RuleIrAttributeConditionBuilder
                .newAttributeConditionFor(orderEntryRaoVariable)
                .withAttribute(QUANTITY_PARAM)
                .withOperator(RuleIrAttributeOperator
                        .valueOf(operator.name())).withValue(quantity).build());
        irConditions.add(RuleIrAttributeRelConditionBuilder
                .newAttributeRelationConditionFor(cartRaoVariable)
                .withAttribute(ORDER_RAO_ENTRIES_ATTRIBUTE)
                .withOperator(RuleIrAttributeOperator.CONTAINS)
                .withTargetVariable(orderEntryRaoVariable).build());
        irConditions.add(RuleIrAttributeRelConditionBuilder
                .newAttributeRelationConditionFor(productConsumedRaoVariable)
                .withAttribute(PRODUCT_CONSUMED_RAO_ENTRY_ATTRIBUTE).withOperator(RuleIrAttributeOperator.EQUAL)
                .withTargetVariable(orderEntryRaoVariable).build());
        irConditions.add(RuleIrAttributeConditionBuilder
                .newAttributeConditionFor(productConsumedRaoVariable)
                .withAttribute(AVAILABLE_QUANTITY_PARAM)
                .withOperator(RuleIrAttributeOperator.GREATER_THAN_OR_EQUAL).withValue(1).build());
        this.evaluateCategoriesOperator(context, categoriesOperator, categories, irQualifyingCategoriesCondition, irConditions);
    }

    protected void evaluateCategoriesOperator(RuleCompilerContext context, CollectionOperator categoriesOperator,
                                              List<Long> categories, RuleIrGroupCondition irQualifyingCategoriesCondition,
                                              List<RuleIrCondition> irConditions) {
        if (CollectionOperator.NOT_CONTAINS.equals(categoriesOperator)) {
            RuleIrNotCondition irNotProductCondition = new RuleIrNotCondition();
            irNotProductCondition.setChildren(irConditions);
            irQualifyingCategoriesCondition.getChildren().add(irNotProductCondition);
        } else {
            irQualifyingCategoriesCondition.getChildren().addAll(irConditions);
            if (CollectionOperator.CONTAINS_ALL.equals(categoriesOperator)) {
                this.addContainsAllCategoriesConditions(context, categories, irQualifyingCategoriesCondition);
            }
        }

    }

    protected void addContainsAllCategoriesConditions(RuleCompilerContext context, List<Long> categories,
                                                      RuleIrGroupCondition irQualifyingCategoriesCondition) {
        String productRaoVariable = context.generateVariable(ProductRAO.class);
        Iterator var6 = categories.iterator();

        while(var6.hasNext()) {
            Long category = (Long)var6.next();
            RuleIrLocalVariablesContainer variablesContainer = context.createLocalContainer();
            String containsCategoryRaoVariable = context.generateLocalVariable(variablesContainer, CategoryRAO.class);
            RuleIrAttributeCondition irContainsCategoryCondition = RuleIrAttributeConditionBuilder
                    .newAttributeConditionFor(containsCategoryRaoVariable)
                    .withAttribute(CATEGORY_RAO_CODE_ATTRIBUTE)
                    .withOperator(RuleIrAttributeOperator.EQUAL).withValue(category).build();
            RuleIrAttributeRelCondition irContainsProductCategoryRel = RuleIrAttributeRelConditionBuilder
                    .newAttributeRelationConditionFor(productRaoVariable)
                    .withAttribute(PRODUCT_RAO_CATEGORIES_ATTRIBUTE)
                    .withOperator(RuleIrAttributeOperator.CONTAINS)
                    .withTargetVariable(containsCategoryRaoVariable).build();
            RuleIrExistsCondition irExistsCategoryCondition = new RuleIrExistsCondition();
            irExistsCategoryCondition.setVariablesContainer(variablesContainer);
            irExistsCategoryCondition.setChildren(Arrays.asList(irContainsCategoryCondition, irContainsProductCategoryRel));
            irQualifyingCategoriesCondition.getChildren().add(irExistsCategoryCondition);
        }

    }

    protected void addExcludedProductsAndCategoriesConditions(RuleCompilerContext context,
                                                              RuleParameterData excludedCategoriesParameter,
                                                              RuleParameterData excludedProductsParameter,
                                                              RuleIrGroupCondition irQualifyingCategoriesCondition) {
        String productRaoVariable = context.generateVariable(ProductRAO.class);
        if (this.verifyAllPresent(new Object[]{excludedCategoriesParameter, excludedCategoriesParameter}) &&
                CollectionUtils.isNotEmpty((Collection)excludedCategoriesParameter.getValue())) {
            RuleIrLocalVariablesContainer variablesContainer = context.createLocalContainer();
            String excludedCategoryRaoVariable = context.generateLocalVariable(variablesContainer, CategoryRAO.class);
            RuleIrAttributeCondition irExcludedCategoryCondition = RuleIrAttributeConditionBuilder
                    .newAttributeConditionFor(excludedCategoryRaoVariable)
                    .withAttribute(CATEGORY_RAO_CODE_ATTRIBUTE)
                    .withOperator(RuleIrAttributeOperator.IN)
                    .withValue(excludedCategoriesParameter.getValue()).build();
            RuleIrAttributeRelCondition irExcludedProductCategoryRel = RuleIrAttributeRelConditionBuilder
                    .newAttributeRelationConditionFor(productRaoVariable)
                    .withAttribute(PRODUCT_RAO_CATEGORIES_ATTRIBUTE)
                    .withOperator(RuleIrAttributeOperator.CONTAINS)
                    .withTargetVariable(excludedCategoryRaoVariable).build();
            RuleIrNotCondition irExcludedCategoriesCondition = new RuleIrNotCondition();
            irExcludedCategoriesCondition.setVariablesContainer(variablesContainer);
            irExcludedCategoriesCondition.setChildren(Arrays.asList(irExcludedCategoryCondition, irExcludedProductCategoryRel));
            irQualifyingCategoriesCondition.getChildren().add(irExcludedCategoriesCondition);
        }

        if (excludedProductsParameter != null && CollectionUtils.isNotEmpty((Collection)excludedProductsParameter.getValue())) {
            RuleIrGroupCondition baseProductNotOrGroupCondition = RuleIrGroupConditionBuilder
                    .newGroupConditionOf(RuleIrGroupOperator.AND).build();
            List<Long> products = (List)excludedProductsParameter.getValue();
            baseProductNotOrGroupCondition.getChildren().add(RuleIrAttributeConditionBuilder
                    .newAttributeConditionFor(productRaoVariable)
                    .withAttribute(PRODUCT_RAO_CODE_ATTRIBUTE)
                    .withOperator(RuleIrAttributeOperator.NOT_IN).withValue(products).build());
        }

    }
}
