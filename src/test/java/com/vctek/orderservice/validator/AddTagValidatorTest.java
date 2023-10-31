package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.AddTagRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.TagModel;
import com.vctek.orderservice.service.OrderService;
import com.vctek.orderservice.service.TagService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class AddTagValidatorTest {
    private AddTagValidator validator;
    @Mock
    private TagService tagService;
    @Mock
    private OrderService orderService;
    private AddTagRequest request;

    @Before
    public void setUp() {
        request = new AddTagRequest();
        MockitoAnnotations.initMocks(this);
        validator = new AddTagValidator();
        validator.setOrderService(orderService);
        validator.setTagService(tagService);
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
    public void validate_invalidOrder() {
        try {
            request.setCompanyId(1l);
            request.setOrderCode("orderCode");
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(null);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_CODE.message(), e.getMessage());
        }
    }

    @Test
    public void validate_invalidTagEmptyId() {
        try {
            request.setCompanyId(1l);
            request.setOrderCode("orderCode");
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(new OrderModel());
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_TAG.message(), e.getMessage());
        }
    }

    @Test
    public void validate_invalidTagId() {
        try {
            request.setCompanyId(1l);
            request.setOrderCode("orderCode");
            request.setTagId(1l);
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(new OrderModel());
            when(tagService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(null);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_TAG.message(), e.getMessage());
        }
    }

    @Test
    public void validate_success() {
        request.setCompanyId(1l);
        request.setOrderCode("orderCode");
        request.setTagId(1l);
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(new OrderModel());
        when(tagService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(new TagModel());
        validator.validate(request);
        assertTrue("success", true);
    }
}
