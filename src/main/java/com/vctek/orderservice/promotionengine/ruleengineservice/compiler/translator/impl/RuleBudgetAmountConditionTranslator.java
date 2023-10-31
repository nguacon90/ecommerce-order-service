package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.impl;

import com.google.common.collect.Lists;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.ruledefinition.condition.builders.RuleIrAttributeConditionBuilder;
import com.vctek.orderservice.promotionengine.ruledefinition.condition.builders.RuleIrAttributeRelConditionBuilder;
import com.vctek.orderservice.promotionengine.ruledefinition.condition.builders.RuleIrGroupConditionBuilder;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrGroupCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleIrAttributeOperator;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleIrGroupOperator;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.CartRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.PromotionBudgetRAO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("ruleBudgetAmountConditionTranslator")
public class RuleBudgetAmountConditionTranslator extends AbstractRuleConditionTranslator {
    @Override
    public RuleIrCondition translate(RuleCompilerContext context, RuleConditionData ruleConditionData, RuleConditionDefinitionData ruleConditionDefinitionData) {
        PromotionSourceRuleModel sourceRuleModel = context.getRule();
        String promotionBudgetRaoVariable = context.generateVariable(PromotionBudgetRAO.class);
        String cartRaoVariable = context.generateVariable(CartRAO.class);
        List<RuleIrCondition> conditions = Lists.newArrayList();
        RuleIrGroupCondition budgetGroupCondition = RuleIrGroupConditionBuilder
                .newGroupConditionOf(RuleIrGroupOperator.AND)
                .withChildren(conditions).build();
        conditions.add(RuleIrAttributeConditionBuilder
                .newAttributeConditionFor(promotionBudgetRaoVariable)
                .withAttribute(SOURCE_RULE_ID_ATTRIBUTE)
                .withOperator(RuleIrAttributeOperator.EQUAL)
                .withValue(sourceRuleModel.getId()).build());

        conditions.add(RuleIrAttributeConditionBuilder
                .newAttributeConditionFor(promotionBudgetRaoVariable)
                .withAttribute(REMAIN_DISCOUNT_ATTRIBUTE)
                .withOperator(RuleIrAttributeOperator.GREATER_THAN)
                .withValue(0d).build());
        conditions.add(RuleIrAttributeRelConditionBuilder.newAttributeRelationConditionFor(cartRaoVariable)
                .withAttribute(PRODUCT_BUDGET_LIST_ATTRIBUTE).withOperator(RuleIrAttributeOperator.CONTAINS)
                .withTargetVariable(promotionBudgetRaoVariable).build());
        return budgetGroupCondition;
    }

}
