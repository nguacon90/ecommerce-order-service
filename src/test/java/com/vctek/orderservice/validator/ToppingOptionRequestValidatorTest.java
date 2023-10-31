package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.request.ToppingOptionRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ToppingOptionRequestValidatorTest {

    private ToppingOptionRequestValidator validator;
    private ToppingOptionRequest request;

    @Before
    public void setUp() {
        request = new ToppingOptionRequest();
        validator = new ToppingOptionRequestValidator();
    }

    @Test
    public void validate_InvalidQuantity() {
        try {
            request.setQuantity(-1);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_QUANTITY.code(), e.getCode());
        }
    }



    @Test
    public void validate_Empty_Percent_Sugar() {
        try {
            request.setQuantity(1);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_PERCENT_SUGAR.code(), e.getCode());
        }
    }

    @Test
    public void validate_Invalid_Percent_Sugar() {
        try {
            request.setQuantity(1);
            request.setSugar(2);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_PERCENT_SUGAR.code(), e.getCode());
        }
    }

    @Test
    public void validate_Empty_Percent_Ice() {
        try {
            request.setQuantity(1);
            request.setSugar(100);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_PERCENT_ICE.code(), e.getCode());
        }
    }

    @Test
    public void validate_Invalid_Percent_Ice() {
        try {
            request.setQuantity(1);
            request.setSugar(100);
            request.setIce(1);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_PERCENT_ICE.code(), e.getCode());
        }
    }

    @Test
    public void validate_Empty_EntryNumber() {
        try {
            request.setQuantity(1);
            request.setSugar(100);
            request.setIce(100);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_ENTRY_NUMBER.code(), e.getCode());
        }
    }

    @Test
    public void validate_Empty_companyId() {
        try {
            request.setQuantity(1);
            request.setSugar(100);
            request.setIce(100);
            request.setEntryId(1l);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_COMPANY_ID.code(), e.getCode());
        }
    }

    @Test
    public void validate() {
        request.setQuantity(1);
        request.setSugar(100);
        request.setIce(100);
        request.setEntryId(1l);
        request.setCompanyId(1l);
        validator.validate(request);
    }
}
