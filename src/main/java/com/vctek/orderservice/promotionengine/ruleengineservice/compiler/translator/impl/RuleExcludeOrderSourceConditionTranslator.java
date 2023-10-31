package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.impl;

import com.vctek.orderservice.promotionengine.ruledefinition.condition.builders.IrConditions;
import com.vctek.orderservice.promotionengine.ruledefinition.condition.builders.RuleIrAttributeConditionBuilder;
import com.vctek.orderservice.promotionengine.ruledefinition.condition.builders.RuleIrGroupConditionBuilder;
import com.vctek.orderservice.promotionengine.ruledefinition.enums.MembershipOperator;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrAttributeCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrGroupCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleIrAttributeOperator;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleIrGroupOperator;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.CartRAO;
import com.vctek.util.OrderType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component("ruleExcludeOrderSourcesConditionTranslator")
public class RuleExcludeOrderSourceConditionTranslator extends AbstractRuleConditionTranslator {

    @Override
    public RuleIrCondition translate(RuleCompilerContext context, RuleConditionData condition, RuleConditionDefinitionData conditionDefinition) {
        Map<String, RuleParameterData> conditionParameters = condition.getParameters();
        RuleParameterData operatorParameter = conditionParameters.get("operator");
        RuleParameterData valueParameter = conditionParameters.get("value");
        if (this.verifyAllPresent(new Object[]{operatorParameter, valueParameter})) {
            MembershipOperator operator = (MembershipOperator) operatorParameter.getValue();
            List<Long> orderSourceIds = (List) valueParameter.getValue();
            if (this.verifyAllPresent(new Object[]{operator, orderSourceIds})) {
                return this.getExcludeOrderSourceConditions(context, operator, orderSourceIds);
            }
        }

        return IrConditions.newIrRuleFalseCondition();
    }

    private RuleIrCondition getExcludeOrderSourceConditions(RuleCompilerContext context, MembershipOperator operator, List<Long> orderSourceIds) {
        RuleIrGroupCondition irGroupCondition = RuleIrGroupConditionBuilder.newGroupConditionOf(RuleIrGroupOperator.OR).build();
        String cartRaoVariable = context.generateVariable(CartRAO.class);
        RuleIrAttributeCondition orderTypeNotOnlineCondition = RuleIrAttributeConditionBuilder
                .newAttributeConditionFor(cartRaoVariable)
                .withAttribute(ORDER_TYPE_ATTRIBUTE)
                .withOperator(RuleIrAttributeOperator.NOT_EQUAL)
                .withValue(OrderType.ONLINE.toString()).build();
        irGroupCondition.getChildren().add(orderTypeNotOnlineCondition);

        RuleIrAttributeCondition excludeOrderSourcesCondition = RuleIrAttributeConditionBuilder
                .newAttributeConditionFor(cartRaoVariable)
                .withAttribute(ORDER_SOURCE_ATTRIBUTE)
                .withOperator(RuleIrAttributeOperator.valueOf(operator.name()))
                .withValue(orderSourceIds).build();
        irGroupCondition.getChildren().add(excludeOrderSourcesCondition);
        return irGroupCondition;
    }
}
