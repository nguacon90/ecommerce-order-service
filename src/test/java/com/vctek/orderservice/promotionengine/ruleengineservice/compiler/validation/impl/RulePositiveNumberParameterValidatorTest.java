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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.Assert.assertEquals;

public class RulePositiveNumberParameterValidatorTest {
    @Mock
    private RuleCompilerContext context;

    private AbstractRuleDefinitionData ruleDefinition;
    private RuleParameterData parameter;
    private RuleParameterDefinitionData parameterDefinition;

    private RulePositiveNumberParameterValidator rulePositiveNumberParameterValidator;
    private Map<String, Double> nagativeMap = new HashMap<>();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        ruleDefinition = new AbstractRuleDefinitionData();
        parameter = new RuleParameterData();
        parameterDefinition = new RuleParameterDefinitionData();
        rulePositiveNumberParameterValidator = new RulePositiveNumberParameterValidator();

    }

    @Test
    public void testValueIsNegative() {
        try {
            parameter.setValue(Double.valueOf(Double.NEGATIVE_INFINITY));
            rulePositiveNumberParameterValidator.validate(context, ruleDefinition, parameter, parameterDefinition);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_PROMOTION_QUANTITY.code(), e.getCode());
        }
    }

    @Test
    public void testValueIsNegativeMap() {
        try {
            nagativeMap.put("key", Double.NEGATIVE_INFINITY);
            parameter.setValue(nagativeMap);
            rulePositiveNumberParameterValidator.validate(context, ruleDefinition, parameter, parameterDefinition);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_PROMOTION_QUANTITY.code(), e.getCode());
        }
    }

    @Test
    public void testValueIsNegativeCollection() {
        try {
            parameter.setValue(Arrays.asList(-1, 2));
            rulePositiveNumberParameterValidator.validate(context, ruleDefinition, parameter, parameterDefinition);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_PROMOTION_QUANTITY.code(), e.getCode());
        }
    }

    @Test
    public void testCheckIsNegativeNumber() {
        assertThat(rulePositiveNumberParameterValidator.checkIsNegativeNumber(new Byte((byte) 0xFF))).isTrue();
        assertThat(rulePositiveNumberParameterValidator.checkIsNegativeNumber(new Byte((byte) 0x7F))).isFalse();
        assertThat(rulePositiveNumberParameterValidator.checkIsNegativeNumber(new Integer(-10))).isTrue();
        assertThat(rulePositiveNumberParameterValidator.checkIsNegativeNumber(new Integer(1))).isFalse();
        assertThat(rulePositiveNumberParameterValidator.checkIsNegativeNumber(new Double(-1D))).isTrue();
        assertThat(rulePositiveNumberParameterValidator.checkIsNegativeNumber(new Double(1D))).isFalse();
        assertThat(rulePositiveNumberParameterValidator.checkIsNegativeNumber(new Float(-1F))).isTrue();
        assertThat(rulePositiveNumberParameterValidator.checkIsNegativeNumber(new Float(1F))).isFalse();
        assertThat(rulePositiveNumberParameterValidator.checkIsNegativeNumber(new Short((short) -1))).isTrue();
        assertThat(rulePositiveNumberParameterValidator.checkIsNegativeNumber(new Short((short) 1))).isFalse();
        assertThat(rulePositiveNumberParameterValidator.checkIsNegativeNumber(new BigDecimal(-1))).isTrue();
        assertThat(rulePositiveNumberParameterValidator.checkIsNegativeNumber(new BigDecimal(1))).isFalse();
    }


}
