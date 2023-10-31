package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.validation.impl;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.AbstractRuleDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class RuleRequiredParameterValidatorTest {
    @Mock
    private RuleCompilerContext context;
    private AbstractRuleDefinitionData ruleDefinition;
    private RuleParameterData parameter;
    private RuleParameterDefinitionData parameterDefinition;

    private RuleRequiredParameterValidator ruleRequiredParameterValidator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        ruleDefinition = new AbstractRuleDefinitionData();
        parameter = new RuleParameterData();
        parameterDefinition = new RuleParameterDefinitionData();
        ruleRequiredParameterValidator = new RuleRequiredParameterValidator();
    }

    @Test
    public void testValueIsNull() {
        try {
            parameter.setValue(null);
            ruleRequiredParameterValidator.validate(context, ruleDefinition, parameter, parameterDefinition);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_PROMOTION_PARAMETER_VALUE.code(), e.getCode());
        }
    }

    @Test
    public void testValueIsEmptyString() {
        try {
            parameter.setValue(StringUtils.EMPTY);
            ruleRequiredParameterValidator.validate(context, ruleDefinition, parameter, parameterDefinition);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_PROMOTION_PARAMETER_VALUE.code(), e.getCode());
        }
    }

    @Test
    public void testValueIsEmptyCollection() {
        try {
            parameter.setValue(CollectionUtils.EMPTY_COLLECTION);

            ruleRequiredParameterValidator.validate(context, ruleDefinition, parameter, parameterDefinition);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_PROMOTION_PARAMETER_VALUE.code(), e.getCode());
        }
    }

    @Test
    public void testValueIsEmptyMap() {
        try {
            parameter.setValue(MapUtils.EMPTY_SORTED_MAP);
            ruleRequiredParameterValidator.validate(context, ruleDefinition, parameter, parameterDefinition);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_PROMOTION_PARAMETER_VALUE.code(), e.getCode());
        }

    }
}
