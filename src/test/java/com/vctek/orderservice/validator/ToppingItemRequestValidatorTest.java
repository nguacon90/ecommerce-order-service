package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.request.ToppingItemRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ToppingItemRequestValidatorTest {

    private ToppingItemRequestValidator validator;
    private ToppingItemRequest request;

    @Before
    public void setUp() {
        request = new ToppingItemRequest();
        validator = new ToppingItemRequestValidator();
    }

    @Test
    public void validate_EmptyProductId() {
        try {
            request.setCompanyId(1l);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_PRODUCT_ID.code(), e.getCode());
        }
    }

    @Test
    public void validate_InvalidQuantity() {
        try {
            request.setProductId(1l);
            request.setCompanyId(1l);
            request.setPrice(2000d);
            request.setQuantity(-1);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_QUANTITY.code(), e.getCode());
        }
    }

    @Test
    public void validate_emptyCompanyId() {
        try {
            request.setProductId(1l);
            request.setPrice(2000d);
            request.setQuantity(1);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_COMPANY_ID.code(), e.getCode());
        }
    }

    @Test
    public void validate() {
        request.setProductId(1l);
        request.setPrice(2000d);
        request.setQuantity(1);
        request.setCompanyId(1l);
        validator.validate(request);
    }
}
