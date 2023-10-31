package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.impl;

import com.vctek.orderservice.promotionengine.ruledefinition.enums.RuleGroupOperator;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrGroupCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.RuleConditionTranslator;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.RuleConditionValidator;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.RuleConditionsTranslator;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleIrGroupOperator;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("ruleGroupConditionTranslator")
public class RuleGroupConditionTranslator implements RuleConditionTranslator, RuleConditionValidator {
    public static final String OPERATOR_PARAM = "operator";
    private RuleConditionsTranslator ruleConditionsTranslator;



    public void validate(RuleCompilerContext context, RuleConditionData condition, RuleConditionDefinitionData conditionDefinition) {
        if (CollectionUtils.isNotEmpty(condition.getChildren())) {
            this.ruleConditionsTranslator.validate(context, condition.getChildren());
        }

    }

    public RuleIrCondition translate(RuleCompilerContext context, RuleConditionData condition, RuleConditionDefinitionData conditionDefinition) {
        RuleGroupOperator operator;
        RuleParameterData operatorParameter = condition.getParameters().get(OPERATOR_PARAM);
        if (operatorParameter != null && operatorParameter.getValue() != null) {
            operator = (RuleGroupOperator)operatorParameter.getValue();
        } else {
            operator = RuleGroupOperator.AND;
        }

        RuleIrGroupOperator irOperator = RuleIrGroupOperator.valueOf(operator.name());
        List<RuleIrCondition> irChildren = this.ruleConditionsTranslator.translate(context, condition.getChildren());
        RuleIrGroupCondition irGroupCondition = new RuleIrGroupCondition();
        irGroupCondition.setOperator(irOperator);
        irGroupCondition.setChildren(irChildren);
        return irGroupCondition;
    }

    @Autowired
    public void setRuleConditionsTranslator(RuleConditionsTranslator ruleConditionsTranslator) {
        this.ruleConditionsTranslator = ruleConditionsTranslator;
    }
}
