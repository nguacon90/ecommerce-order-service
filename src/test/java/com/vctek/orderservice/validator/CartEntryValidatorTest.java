package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.OrderEntryDTO;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.util.CurrencyType;
import com.vctek.util.OrderType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CartEntryValidatorTest {

    private CartEntryValidator validator;
    private OrderEntryDTO dto = new OrderEntryDTO();

    @Before
    public void setUp() {
        validator = new CartEntryValidator();
    }

    @Test
    public void validate_EmptyCompanyId() {
        try {
            validator.validate(dto);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_COMPANY_ID.code(), e.getCode());
        }
    }

    @Test
    public void validate_InvalidOrderId() {
        try {
            dto.setCompanyId(1l);
            validator.validate(dto);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_ID.code(), e.getCode());
        }
    }

    @Test
    public void validate_EmptyProductId() {
        try {
            dto.setCompanyId(1l);
            dto.setOrderCode("22l");
            validator.validate(dto);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_PRODUCT_ID.code(), e.getCode());
        }
    }

    @Test
    public void validate_InvalidQty() {
        try {
            dto.setCompanyId(1l);
            dto.setOrderCode("22l");
            dto.setProductId(333l);
            validator.validate(dto);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_QUANTITY.code(), e.getCode());
        }
    }

    @Test
    public void validate_InvalidQtySmallerThan0() {
        try {
            dto.setCompanyId(1l);
            dto.setOrderCode("22l");
            dto.setProductId(333l);
            dto.setQuantity(-20l);
            validator.validate(dto);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_QUANTITY.code(), e.getCode());
        }
    }

    @Test
    public void validate_InvalidDiscountSmallerThan0() {
        try {
            dto.setCompanyId(1l);
            dto.setOrderCode("22l");
            dto.setProductId(333l);
            dto.setQuantity(20l);
            dto.setDiscount(-10d);
            validator.validate(dto);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_DISCOUNT.code(), e.getCode());
        }
    }

    @Test
    public void validate_InvalidDiscountType() {
        try {
            dto.setCompanyId(1l);
            dto.setOrderCode("22l");
            dto.setProductId(333l);
            dto.setQuantity(20l);
            dto.setDiscount(10d);
            dto.setDiscountType("aaa");
            validator.validate(dto);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_DISCOUNT_TYPE.code(), e.getCode());
        }
    }

    @Test
    public void validate_InvalidOrderType() {
        try {
            dto.setCompanyId(1l);
            dto.setOrderCode("22l");
            dto.setProductId(333l);
            dto.setQuantity(20l);
            dto.setDiscount(10d);
            dto.setDiscountType(CurrencyType.PERCENT.toString());
            validator.validate(dto);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_TYPE.code(), e.getCode());
        }
    }

    @Test
    public void validate_success() {
        dto.setCompanyId(1l);
        dto.setOrderCode("22l");
        dto.setProductId(333l);
        dto.setQuantity(20l);
        dto.setOrderType(OrderType.ONLINE.toString());
        validator.validate(dto);
    }
}
