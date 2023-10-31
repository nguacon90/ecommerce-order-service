package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.impl;

import com.vctek.orderservice.promotionengine.ruledefinition.condition.builders.IrConditions;
import com.vctek.orderservice.promotionengine.ruledefinition.condition.builders.RuleIrAttributeConditionBuilder;
import com.vctek.orderservice.promotionengine.ruledefinition.condition.builders.RuleIrGroupConditionBuilder;
import com.vctek.orderservice.promotionengine.ruledefinition.enums.MembershipOperator;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrGroupCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.RuleConditionTranslator;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleIrAttributeOperator;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleIrGroupOperator;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.CartRAO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

public abstract class AbstractRuleConditionTranslator implements RuleConditionTranslator {
    public static final String OPERATOR_PARAM = "operator";
    public static final String VALUE_PARAM = "value";
    public static final String ORDER_RAO_CURRENCY_ATTRIBUTE = "currencyIsoCode";
    public static final String ORDER_CONSUMED_RAO_ATTRIBUTE = "cart";
    public static final String ORDER_RAO_TOTAL_ATTRIBUTE = "total";
    public static final String ORDER_RAO_ENTRIES_ATTRIBUTE = "entries";
    public static final String PRODUCT_CONSUMED_RAO_ENTRY_ATTRIBUTE = "orderEntry";
    public static final String ORDER_ENTRY_RAO_BASE_PRICE_ATTRIBUTE = "basePrice";
    public static final String QUANTITY_PARAM = "quantity";
    public static final String AVAILABLE_QUANTITY_PARAM = "availableQuantity";
    public static final String CATEGORIES_OPERATOR_PARAM = "categories_operator";
    public static final String CATEGORIES_PARAM = "categories";
    public static final String CATEGORY_RAO_CODE_ATTRIBUTE = "code";
    public static final String ORDER_ENTRY_RAO_PRODUCT_ATTRIBUTE = "product";
    public static final String PRODUCT_RAO_CODE_ATTRIBUTE = "id";
    public static final String PRODUCT_RAO_CATEGORIES_ATTRIBUTE = "categories";
    public static final String PRODUCT_RAO_SUPPLIER_ATTRIBUTE = "supplier";
    public static final String PRODUCTS_PARAM = "products";
    public static final String BASE_PRODUCT_CODES_ATTRIBUTE = "baseProductCodes";
    public static final String ORDER_TYPE_ATTRIBUTE = "type";
    public static final String WAREHOUSE_ATTRIBUTE = "warehouse";
    public static final String PRICE_TYPE_ATTRIBUTE = "priceType";

    public static final String ORDER_SOURCE_ATTRIBUTE = "orderSource";
    public static final String SOURCE_RULE_ID_ATTRIBUTE = "sourceRuleId";
    public static final String REMAIN_DISCOUNT_ATTRIBUTE = "remainDiscount";
    public static final String PRODUCT_BUDGET_LIST_ATTRIBUTE = "promotionBudgetList";

    protected boolean verifyAllPresent(Object... objects) {
        boolean isPresent = true;
        if (ArrayUtils.isNotEmpty(objects)) {
            isPresent = Arrays.stream(objects).map(this::covertToNullIfEmptyCollection).map(this::covertToNullIfEmptyMap).noneMatch(Objects::isNull);
        }

        return isPresent;
    }

    protected boolean verifyAnyPresent(Object... objects) {
        boolean anyPresent = true;
        if (ArrayUtils.isNotEmpty(objects)) {
            anyPresent = Arrays.stream(objects).map(this::covertToNullIfEmptyCollection).map(this::covertToNullIfEmptyMap).anyMatch(Objects::nonNull);
        }

        return anyPresent;
    }

    protected Object covertToNullIfEmptyCollection(Object seedObject) {
        return seedObject instanceof Collection && CollectionUtils.isEmpty((Collection)seedObject) ? null : seedObject;
    }

    protected Object covertToNullIfEmptyMap(Object seedObject) {
        return seedObject instanceof Map && MapUtils.isEmpty((Map)seedObject) ? null : seedObject;
    }


    protected RuleIrCondition translateCartAttributeConditions(RuleCompilerContext context, RuleConditionData condition,
                                                    RuleConditionDefinitionData conditionDefinition, String attributeName) {
        Map<String, RuleParameterData> conditionParameters = condition.getParameters();
        RuleParameterData operatorParameter = conditionParameters.get(OPERATOR_PARAM);
        RuleParameterData valueParameter = conditionParameters.get(VALUE_PARAM);
        if (this.verifyAllPresent(new Object[]{operatorParameter, valueParameter})) {
            MembershipOperator operator = (MembershipOperator) operatorParameter.getValue();
            List<String> values = (List) valueParameter.getValue();
            if (this.verifyAllPresent(new Object[]{operator, values})) {
                return this.populateCartAttributeConditions(context, operator, values, attributeName);

            }
        }

        return IrConditions.newIrRuleFalseCondition();
    }

    protected RuleIrGroupCondition populateCartAttributeConditions(RuleCompilerContext context, MembershipOperator operator,
                                                                   List<String> values, String attribute) {
        String orderRaoVariable = context.generateVariable(CartRAO.class);
        RuleIrGroupCondition ruleIrGroupCondition = RuleIrGroupConditionBuilder.newGroupConditionOf(RuleIrGroupOperator.OR).build();
        RuleIrCondition ruleIrCondition = RuleIrAttributeConditionBuilder
                .newAttributeConditionFor(orderRaoVariable).withAttribute(attribute)
                .withOperator(RuleIrAttributeOperator.valueOf(operator.toString()))
                .withValue(values).build();
        ruleIrGroupCondition.getChildren().add(ruleIrCondition);
        return ruleIrGroupCondition;
    }
}
