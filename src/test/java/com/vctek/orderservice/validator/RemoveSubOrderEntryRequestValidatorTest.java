package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.request.RemoveSubOrderEntryRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class RemoveSubOrderEntryRequestValidatorTest {

    private RemoveSubOrderEntryRequestValidator validator;
    private RemoveSubOrderEntryRequest request;

    @Before
    public void setUp() {
        request = new RemoveSubOrderEntryRequest();
        validator = new RemoveSubOrderEntryRequestValidator();
    }

    @Test
    public void validate_EmptyCompanyId() {
        try {
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_COMPANY_ID.code(), e.getCode());
        }
    }

    @Test
    public void validate_EmptySubEntryId() {
        try {
            request.setCompanyId(1l);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_SUB_ORDER_ENTRY_ID.code(), e.getCode());
        }
    }

    @Test
    public void validate_success() {
        request.setCompanyId(1l);
        request.setSubEntryId(1l);
        validator.validate(request);
    }

}
