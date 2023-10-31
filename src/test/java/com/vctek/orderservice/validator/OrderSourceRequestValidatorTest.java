package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.request.OrderSourceRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.OrderSourceModel;
import com.vctek.orderservice.service.OrderSourceService;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrderSourceRequestValidatorTest {
    private OrderSourceService service;
    private OrderSourceRequest request;
    private OrderSourceRequestValidator validate;

    @Before
    public void setUp() {
        request = new OrderSourceRequest();
        service = mock(OrderSourceService.class);
        validate = new OrderSourceRequestValidator(service);
    }

    @Test
    public void validate_OrderSourceRequest_emptyName() {
        try {
            validate.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_ORDER_SOURCE_NAME.code(), e.getCode());
        }
    }

    @Test
    public void validate_orderSourdeIdNotNull_ShouldThrowException() {
        request.setId(11l);
        when(service.findById(anyLong())).thenReturn(null);
        try {
            validate.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_SOURCE_ID.code(), e.getCode());
        }
    }

    @Test
    public void validate_companyIdEmpty() {
        try {
            request.setName("name");
            validate.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_COMPANY_ID.code(), e.getCode());
        }
    }

    @Test
    public void validate_OrderSourceRequest() {
        request.setName("name");
        request.setCompanyId(11l);
        when(service.findById(anyLong())).thenReturn(new OrderSourceModel());
        validate.validate(request);
    }
}
