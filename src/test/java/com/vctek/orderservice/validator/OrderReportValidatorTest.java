package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.request.OrderReportRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

public class OrderReportValidatorTest {
    private OrderReportValidator orderReportValidator;
    @Mock
    private OrderReportRequest request;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        orderReportValidator = new OrderReportValidator();
    }

    @Test
    public void validateEmptyCompanyId() {
        try {
            when(request.getCompanyId()).thenReturn(null);
            orderReportValidator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_COMPANY_ID.code(), e.getCode());
        }
    }

    @Test
    public void validateEmptyFromDate() {
        try {
            when(request.getCompanyId()).thenReturn(1l);
            orderReportValidator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_FROM_DATE.code(), e.getCode());
        }
    }

}
