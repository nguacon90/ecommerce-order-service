package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.validation.impl;

import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerProblem;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerProblemFactory;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.validation.RuleParameterValidator;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.AbstractRuleDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component("ruleContainerIdParameterValidator")
public class RuleContainerIdParameterValidator implements RuleParameterValidator {
    protected static final String MESSAGE_KEY = "Container id {0} is invalid for parameter {1}";
    protected static final Pattern CONTAINER_ID_PATTERN = Pattern.compile("[a-zA-Z0-9_-]*$");
    private RuleCompilerProblemFactory ruleCompilerProblemFactory;

    public void validate(RuleCompilerContext context, AbstractRuleDefinitionData ruleDefinition, RuleParameterData parameter, RuleParameterDefinitionData parameterDefinition) {
        if (parameter != null && !StringUtils.isBlank((String)parameter.getValue())) {
            String containerID = (String)parameter.getValue();
            if (!CONTAINER_ID_PATTERN.matcher(containerID).matches()) {
                context.addProblem(this.ruleCompilerProblemFactory.createParameterProblem(RuleCompilerProblem.Severity.ERROR,
                        MESSAGE_KEY, parameter, parameterDefinition, new Object[]{containerID, parameterDefinition.getName()}));
            }

        }
    }

    @Autowired
    public void setRuleCompilerProblemFactory(RuleCompilerProblemFactory ruleCompilerProblemFactory) {
        this.ruleCompilerProblemFactory = ruleCompilerProblemFactory;
    }
}
