package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.validation.impl;

import com.google.common.base.Preconditions;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.validation.RuleParameterValidator;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.AbstractRuleDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

@Component("rulePositiveNumberParameterValidator")
public class RulePositiveNumberParameterValidator implements RuleParameterValidator {

    @Override
    public void validate(RuleCompilerContext context, AbstractRuleDefinitionData ruleDefinition, RuleParameterData parameter, RuleParameterDefinitionData parameterDefinition) {
        Object parameterValue = parameter.getValue();
        if (parameterValue instanceof Number) {
            Number number = (Number)parameter.getValue();
            this.validatePositiveNumber(context, ruleDefinition, parameter, parameterDefinition, number);
        } else if (parameterValue instanceof Collection) {
            this.validatePositiveCollectionValue(context, ruleDefinition, parameter, parameterDefinition);
        } else if (parameterValue instanceof Map) {
            this.validatePositiveMapValue(context, ruleDefinition, parameter, parameterDefinition);
        }
    }

    protected void validatePositiveNumber(RuleCompilerContext context, AbstractRuleDefinitionData ruleDefinition, RuleParameterData parameter,
                                          RuleParameterDefinitionData parameterDefinition, Number number) {
        if (this.checkIsNegativeNumber(number)) {
            ErrorCodes err = ErrorCodes.INVALID_PROMOTION_QUANTITY;
            throw new ServiceException(err.code(), err.message(), err.httpStatus(),
                    new Object[]{parameterDefinition.getName(), parameter.getUuid(), ruleDefinition.getName()});
        }
    }

    protected void validatePositiveMapValue(RuleCompilerContext context, AbstractRuleDefinitionData ruleDefinition, RuleParameterData parameter, RuleParameterDefinitionData parameterDefinition) {
        Map<?, Number> mapValue = (Map)parameter.getValue();
        Iterator var7 = mapValue.entrySet().iterator();

        while(var7.hasNext()) {
            Map.Entry<?, Number> entry = (Map.Entry)var7.next();
            Number number = entry.getValue();
            this.validatePositiveNumber(context, ruleDefinition, parameter, parameterDefinition, number);
        }

    }

    protected void validatePositiveCollectionValue(RuleCompilerContext context, AbstractRuleDefinitionData ruleDefinition, RuleParameterData parameter, RuleParameterDefinitionData parameterDefinition) {
        Collection<Number> collectionValue = (Collection)parameter.getValue();
        Iterator var7 = collectionValue.iterator();

        while(var7.hasNext()) {
            Number number = (Number)var7.next();
            this.validatePositiveNumber(context, ruleDefinition, parameter, parameterDefinition, number);
        }

    }

    protected boolean checkIsNegativeNumber(Number number) {
        if (!Objects.isNull(number) && number instanceof Comparable) {
            return ((Comparable)number).compareTo(RulePositiveNumberParameterValidator.ZeroNumberFactory.newInstance(number.getClass())) < 0;
        } else {
            return true;
        }
    }

    protected static class ZeroNumberFactory {
        protected ZeroNumberFactory() {
        }

        protected static Number newInstance(Class<? extends Number> clazz) {
            Preconditions.checkArgument(clazz != null, "Valid class must be provided here");
            try {
                return clazz.getConstructor(String.class).newInstance("0");
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | InstantiationException var2) {
                throw new IllegalStateException("Cannot instantiate the class " + clazz.getName(), var2);
            }
        }
    }
}
