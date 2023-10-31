package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.impl;

import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;
import org.springframework.stereotype.Component;

@Component("rulePriceTypeConditionTranslator")
public class RulePriceTypeConditionTranslator extends AbstractRuleConditionTranslator {

    @Override
    public RuleIrCondition translate(RuleCompilerContext context, RuleConditionData condition,
                                     RuleConditionDefinitionData conditionDefinition) {
        return translateCartAttributeConditions(context, condition, conditionDefinition, PRICE_TYPE_ATTRIBUTE);
    }

}
