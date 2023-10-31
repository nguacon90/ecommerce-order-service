package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.vctek.orderservice.promotionengine.ruledefinition.condition.builders.RuleIrAttributeConditionBuilder;
import com.vctek.orderservice.promotionengine.ruledefinition.condition.builders.RuleIrAttributeRelConditionBuilder;
import com.vctek.orderservice.promotionengine.ruledefinition.condition.builders.RuleIrGroupConditionBuilder;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrFalseCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrGroupCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleIrAttributeOperator;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleIrGroupOperator;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("ruleQualifyingSuppliersConditionTranslator")
public class RuleQualifyingSuppliersConditionTranslator extends AbstractRuleConditionTranslator {
    public static final String SUPPLIER_RAO_SUPPLIER_ID_ATTRIBUTE = "supplierId";
    public static final String SUPPLIERS_PARAM = "suppliers";
    @Override
    public RuleIrCondition translate(RuleCompilerContext context, RuleConditionData condition,
                                     RuleConditionDefinitionData conditionDefinition) {
        Preconditions.checkNotNull(context, "Rule Compiler Context is not expected to be NULL here");
        Preconditions.checkNotNull(condition, "Rule Condition Data is not expected to be NULL here");
        RuleParameterData couponsParameter = condition.getParameters().get(SUPPLIERS_PARAM);
        if (couponsParameter == null) {
            return new RuleIrFalseCondition();
        }

        List<Long> suppliers = (List) couponsParameter.getValue();
        if (CollectionUtils.isEmpty(suppliers)) {
            return new RuleIrFalseCondition();
        }

        String supplierRaoVariable = context.generateVariable(SupplierRAO.class);
        String productRaoVariable = context.generateVariable(ProductRAO.class);
        String orderEntryRaoVariable = context.generateVariable(OrderEntryRAO.class);
        String cartRaoVariable = context.generateVariable(CartRAO.class);
        String productConsumedRaoVariable = context.generateVariable(ProductConsumedRAO.class);
        List<RuleIrCondition> irConditions = Lists.newArrayList();
        irConditions.add(RuleIrAttributeConditionBuilder
                .newAttributeConditionFor(supplierRaoVariable).withAttribute(SUPPLIER_RAO_SUPPLIER_ID_ATTRIBUTE)
                .withOperator(RuleIrAttributeOperator.IN).withValue(suppliers).build());
        irConditions.add(RuleIrAttributeRelConditionBuilder
                .newAttributeRelationConditionFor(productRaoVariable)
                .withAttribute(PRODUCT_RAO_SUPPLIER_ATTRIBUTE)
                .withOperator(RuleIrAttributeOperator.EQUAL)
                .withTargetVariable(supplierRaoVariable).build());
        irConditions.add(RuleIrAttributeRelConditionBuilder
                .newAttributeRelationConditionFor(orderEntryRaoVariable)
                .withAttribute(ORDER_ENTRY_RAO_PRODUCT_ATTRIBUTE)
                .withOperator(RuleIrAttributeOperator.EQUAL)
                .withTargetVariable(productRaoVariable).build());
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

        RuleIrGroupCondition irQualifyingSuppliersCondition = RuleIrGroupConditionBuilder
                .newGroupConditionOf(RuleIrGroupOperator.AND).build();

        irQualifyingSuppliersCondition.getChildren().addAll(irConditions);
        return irQualifyingSuppliersCondition;
    }

}
