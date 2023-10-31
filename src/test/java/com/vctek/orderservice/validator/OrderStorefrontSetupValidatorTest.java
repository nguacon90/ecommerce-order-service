package com.vctek.orderservice.validator;

import com.vctek.dto.redis.OrderStorefrontSetupData;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.OrderSourceModel;
import com.vctek.orderservice.service.OrderSourceService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public class OrderStorefrontSetupValidatorTest {
    private OrderStorefrontSetupValidator validator;
    private OrderStorefrontSetupData request;
    @Mock
    private OrderSourceService orderSourceService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        request = new OrderStorefrontSetupData();
        validator = new OrderStorefrontSetupValidator();
        validator.setOrderSourceService(orderSourceService);
    }

    @Test
    public void validate_emptyCompanyId() {
        try {
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_COMPANY_ID.message(), e.getMessage());
        }
    }

    @Test
    public void validate_emptyWarehouseId() {
        try {
            request.setCompanyId(2L);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_WAREHOUSE_ID.message(), e.getMessage());
        }
    }

    @Test
    public void validate_emptyOrderSourceId() {
        try {
            request.setCompanyId(2L);
            request.setWarehouseId(2L);
            request.setShippingCompanyId(2L);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_ORDER_SOURCE_ID.message(), e.getMessage());
        }
    }

    @Test
    public void validate_invalidOrderSourceId() {
        try {
            request.setCompanyId(2L);
            request.setWarehouseId(2L);
            request.setShippingCompanyId(2L);
            request.setOrderSourceId(2L);
            when(orderSourceService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(null);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_SOURCE_ID.message(), e.getMessage());
        }
    }

    @Test
    public void validate() {
        request.setCompanyId(2L);
        request.setWarehouseId(2L);
        request.setShippingCompanyId(2L);
        request.setOrderSourceId(2L);
        when(orderSourceService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(new OrderSourceModel());
        validator.validate(request);
        assertTrue(true);
    }
}
