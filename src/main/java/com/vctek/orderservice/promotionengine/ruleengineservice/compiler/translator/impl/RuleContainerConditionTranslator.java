package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.impl;

import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerProblem;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerProblemFactory;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleIrVariablesGenerator;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrFalseCondition;
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

@Component("ruleContainerConditionTranslator")
public class RuleContainerConditionTranslator implements RuleConditionTranslator, RuleConditionValidator {
    public static final String ID_PARAM = "id";
    public static final String NO_CHILDREN = "Container should contain at least one child condition";
    private RuleConditionsTranslator ruleConditionsTranslator;
    private RuleCompilerProblemFactory ruleCompilerProblemFactory;


    public void validate(RuleCompilerContext context, RuleConditionData condition, RuleConditionDefinitionData conditionDefinition) {
        if (CollectionUtils.isNotEmpty(condition.getChildren())) {
            this.ruleConditionsTranslator.validate(context, condition.getChildren());
        } else {
            context.addProblem(this.ruleCompilerProblemFactory.createProblem(RuleCompilerProblem.Severity.ERROR, NO_CHILDREN,
                    new Object[]{conditionDefinition.getName()}));
        }

    }

    public RuleIrCondition translate(RuleCompilerContext context, RuleConditionData condition, RuleConditionDefinitionData conditionDefinition) {
        RuleParameterData idParameter = condition.getParameters().get(ID_PARAM);
        if (idParameter == null) {
            return new RuleIrFalseCondition();
        } else {
            String id = (String) idParameter.getValue();
            if (id == null) {
                return new RuleIrFalseCondition();
            }

            RuleIrVariablesGenerator variablesGenerator = context.getVariablesGenerator();
            RuleIrGroupCondition var11;
            try {
                variablesGenerator.createContainer(id);
                RuleIrGroupOperator irOperator = RuleIrGroupOperator.AND;
                List<RuleIrCondition> irChildren = this.ruleConditionsTranslator.translate(context, condition.getChildren());
                RuleIrGroupCondition irGroupCondition = new RuleIrGroupCondition();
                irGroupCondition.setOperator(irOperator);
                irGroupCondition.setChildren(irChildren);
                var11 = irGroupCondition;
            } finally {
                variablesGenerator.closeContainer();
            }

            return var11;
        }
    }

    @Autowired
    public void setRuleConditionsTranslator(RuleConditionsTranslator ruleConditionsTranslator) {
        this.ruleConditionsTranslator = ruleConditionsTranslator;
    }

    @Autowired
    public void setRuleCompilerProblemFactory(RuleCompilerProblemFactory ruleCompilerProblemFactory) {
        this.ruleCompilerProblemFactory = ruleCompilerProblemFactory;
    }
}
