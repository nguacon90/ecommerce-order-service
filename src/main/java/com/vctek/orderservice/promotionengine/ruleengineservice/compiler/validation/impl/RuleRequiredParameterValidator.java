package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.validation.impl;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.validation.RuleParameterValidator;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.AbstractRuleDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Component("ruleRequiredParameterValidator")
public class RuleRequiredParameterValidator implements RuleParameterValidator {

    @Override
    public void validate(RuleCompilerContext context, AbstractRuleDefinitionData ruleDefinition,
                         RuleParameterData parameter, RuleParameterDefinitionData parameterDefinition) {
        if (!BooleanUtils.isFalse(parameterDefinition.getRequired())) {
            if (this.isEmptyValue(parameter.getValue())) {
                ErrorCodes err = ErrorCodes.EMPTY_PROMOTION_PARAMETER_VALUE;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
        }
    }

    protected boolean isEmptyValue(Object value) {
        if (value == null) {
            return true;
        } else if (value instanceof Map) {
            Map<?, ?> mapToValidate = (Map)value;
            return MapUtils.isEmpty(mapToValidate) || Collections.frequency(mapToValidate.values(), null) > 0 ||
                    Collections.frequency(mapToValidate.keySet(), null) > 0;
        } else if (value instanceof Collection) {
            return CollectionUtils.isEmpty((Collection)value);
        } else {
            return value instanceof String ? StringUtils.isBlank((String)value) : false;
        }
    }
}
