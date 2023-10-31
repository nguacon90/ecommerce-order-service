package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.validation.impl;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.AbstractRuleDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

public class RulePercentageParameterValidatorTest {
    private RulePercentageParameterValidator validator;
    @Mock
    private RuleCompilerContext context;
    @Mock
    private AbstractRuleDefinitionData ruleDefinition;
    @Mock
    private RuleParameterData parameter;
    @Mock
    private RuleParameterDefinitionData parameterDefinition;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        validator = new RulePercentageParameterValidator();
    }

    @Test
    public void validate_negativePercentage() {
        try {
            when(parameter.getValue()).thenReturn(new BigDecimal(-10));
            validator.validate(context, ruleDefinition, parameter, parameterDefinition);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_PROMOTION_PERCENTAGE.code(), e.getCode());
        }
    }

    @Test
    public void validate_zeroPercentage() {
        try {
            when(parameter.getValue()).thenReturn(new BigDecimal(0));
            validator.validate(context, ruleDefinition, parameter, parameterDefinition);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_PROMOTION_PERCENTAGE.code(), e.getCode());
        }
    }

    @Test
    public void validate_over100Percentage() {
        try {
            when(parameter.getValue()).thenReturn(new BigDecimal(110));
            validator.validate(context, ruleDefinition, parameter, parameterDefinition);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_PROMOTION_PERCENTAGE.code(), e.getCode());
        }
    }

    @Test
    public void validate_validPercentage() {
        when(parameter.getValue()).thenReturn(new BigDecimal(10));
        validator.validate(context, ruleDefinition, parameter, parameterDefinition);
    }
}
