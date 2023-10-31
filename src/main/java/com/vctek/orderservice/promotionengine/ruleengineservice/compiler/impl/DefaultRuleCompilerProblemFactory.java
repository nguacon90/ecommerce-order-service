package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.impl;

import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerParameterProblem;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerProblem;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerProblemFactory;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@Component
public class DefaultRuleCompilerProblemFactory implements RuleCompilerProblemFactory {

    @Override
    public RuleCompilerProblem createProblem(RuleCompilerProblem.Severity severity, String messageKey, Object... parameters) {
        MessageFormat messageFormat = new MessageFormat(messageKey);
        String message = messageFormat.format(parameters, new StringBuffer(), null).toString();
        return new DefaultRuleCompilerProblem(severity, message);
    }

    @Override
    public RuleCompilerParameterProblem createParameterProblem(RuleCompilerProblem.Severity severity, String messageKey,
                                                                RuleParameterData parameterData,
                                                               RuleParameterDefinitionData parameterDefinitionData,
                                                               Object... parameters) {
        MessageFormat messageFormat = new MessageFormat(messageKey);
        String message = messageFormat.format(parameters, new StringBuffer(), null).toString();
        return new DefaultRuleCompilerParameterProblem(severity, message, parameterData, parameterDefinitionData);
    }
}
