package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.request.OrderSettingRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ComboPriceSettingValidatorTest {
    private ComboPriceSettingValidator validator;
    private OrderSettingRequest request = new OrderSettingRequest();

    @Before
    public void setup() {
        validator = new ComboPriceSettingValidator();
    }

    @Test
    public void validate_emptyAmount() {
        try {
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_AMOUNT_OF_COMBO_PRICE_SETTING.message(), e.getMessage());
        }
    }

    @Test
    public void validate_amountSmallerThan0() {
        try {
            request.setAmount(-10d);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_AMOUNT_OF_COMBO_PRICE_SETTING.message(), e.getMessage());
        }
    }

    @Test
    public void validate_success() {
        request.setAmount(0d);
        validator.validate(request);
        assertTrue("success", true);
    }
}
