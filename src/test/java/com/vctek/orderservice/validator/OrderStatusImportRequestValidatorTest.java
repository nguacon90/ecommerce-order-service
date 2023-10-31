package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.request.OrderStatusImportRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.util.OrderStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.fail;

public class OrderStatusImportRequestValidatorTest {
    OrderStatusImportRequestValidator validator;
    OrderStatusImportRequest request;

    @Before
    public void setUp() {
        validator = new OrderStatusImportRequestValidator();
        validator.setMaxOrderSize(3);
        request = new OrderStatusImportRequest();
    }

    @Test
    public void validate() {
        request.setCompanyId(2L);
        request.setOrderStatus(OrderStatus.NEW.code());
        List<String> orderCodes = new ArrayList<>();
        orderCodes.add("ORDER_CODE_1");
        request.setOrderCodes(orderCodes);
        validator.validate(request);
    }

    @Test
    public void validateCompanyId_null() {
        try {
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            Assert.assertEquals(ErrorCodes.EMPTY_COMPANY_ID.message(), e.getMessage());
        }
    }

    @Test
    public void validateOrderStatus_null() {
        try {
            request.setCompanyId(2L);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            Assert.assertEquals(ErrorCodes.EMPTY_ORDER_STATUS.message(), e.getMessage());
        }
    }

    @Test
    public void validateOrderCode_null() {
        try {
            request.setCompanyId(2L);
            request.setOrderStatus(OrderStatus.NEW.code());
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            Assert.assertEquals(ErrorCodes.EMPTY_ORDER_CODE.message(), e.getMessage());
        }
    }


    @Test
    public void validateOrderCode_OverMaxOrderSize() {
        try {
            request.setCompanyId(2L);
            request.setOrderStatus(OrderStatus.NEW.code());
            request.setOrderCodes(Arrays.asList("1", "2", "3", "4"));
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            Assert.assertEquals(ErrorCodes.OVER_MAX_SUPPORTED_ORDER_SIZE_FOR_CHANGE_STATUS.message(), e.getMessage());
        }
    }

}