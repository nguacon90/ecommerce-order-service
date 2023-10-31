package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.validation.impl;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.validation.RuleParameterValidator;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.AbstractRuleDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("rulePercentageParameterValidator")
public class RulePercentageParameterValidator implements RuleParameterValidator {

    @Override
    public void validate(RuleCompilerContext context, AbstractRuleDefinitionData ruleDefinition,
                         RuleParameterData parameter, RuleParameterDefinitionData parameterDefinition) {
        BigDecimal percentage = (BigDecimal)parameter.getValue();
        if (percentage != null && (percentage.doubleValue() <= 0.0D || percentage.doubleValue() > 100.0D)) {
            ErrorCodes err = ErrorCodes.INVALID_PROMOTION_PERCENTAGE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }
}
