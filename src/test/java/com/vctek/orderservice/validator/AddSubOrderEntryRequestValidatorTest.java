package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.ComboData;
import com.vctek.orderservice.dto.request.AddSubOrderEntryRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.service.ProductService;
import com.vctek.util.OrderType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AddSubOrderEntryRequestValidatorTest {

    private ProductService productService;
    private AddSubOrderEntryRequestValidator validator;
    private AddSubOrderEntryRequest request;

    @Before
    public void setUp() {
        productService = mock(ProductService.class);
        request = new AddSubOrderEntryRequest();
        validator = new AddSubOrderEntryRequestValidator(productService);
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
    public void validate_EmptyComboId() {
        try {
            request.setCompanyId(1l);
            request.setProductId(1l);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_COMBO_ID.code(), e.getCode());
        }
    }

    @Test
    public void validate_Invalid_Product() {
        try {
            request.setCompanyId(1l);
            request.setComboId(1l);
            request.setProductId(1l);
            when(productService.getCombo(anyLong(), anyLong())).thenReturn(null);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_COMBO_ID.code(), e.getCode());
        }
    }

    @Test
    public void validate_Empty_OrderCode() {
        try {
            request.setCompanyId(1l);
            request.setComboId(1l);
            request.setProductId(1l);
            when(productService.getCombo(anyLong(), anyLong())).thenReturn(new ComboData());
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_ORDER_CODE.code(), e.getCode());
        }
    }

    @Test
    public void validate_Empty_EntryNumber() {
        try {
            request.setCompanyId(1l);
            request.setComboId(1l);
            request.setProductId(1l);
            request.setOrderCode("123");
            when(productService.getCombo(anyLong(), anyLong())).thenReturn(new ComboData());
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_ENTRY_NUMBER.code(), e.getCode());
        }
    }

    @Test
    public void validate_Empty_ComboGroupNumber() {
        try {
            request.setCompanyId(1l);
            request.setComboId(1l);
            request.setProductId(1l);
            request.setOrderCode("123");
            request.setEntryId(1l);
            request.setOrderType(OrderType.ONLINE.name());
            when(productService.getCombo(anyLong(), anyLong())).thenReturn(new ComboData());
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_COMBO_GROUP_NUMBER.code(), e.getCode());
        }
    }

    @Test
    public void validate_success() {
        request.setCompanyId(1l);
        request.setComboId(1l);
        request.setOrderCode("123");
        request.setProductId(1l);
        request.setEntryId(1l);
        request.setComboGroupNumber(1);
        when(productService.getCombo(anyLong(), anyLong())).thenReturn(new ComboData());
        validator.validate(request);
    }

}
