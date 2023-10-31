package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.request.ReturnOrderUpdateParameter;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.ReturnOrderModel;
import com.vctek.orderservice.service.ReturnOrderService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public class ReturnOrderUpdateParameterValidatorTest {
    private ReturnOrderUpdateParameterValidator validator;
    @Mock
    private ReturnOrderService returnOrderService;
    @Mock
    private ReturnOrderUpdateParameter param;
    @Mock
    private ReturnOrderModel returnOrder;
    @Mock
    private OrderModel exchangeOrder;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        validator = new ReturnOrderUpdateParameterValidator();
        validator.setReturnOrderService(returnOrderService);
    }

    @Test
    public void validate_emptyCompanyId() {
        when(param.getCompanyId()).thenReturn(null);
        try {
            validator.validate(param);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_COMPANY_ID.code(), e.getCode());
        }
    }

    @Test
    public void validate_emptyWarehouseId() {
        try {
            when(param.getCompanyId()).thenReturn(1l);
            when(param.getWarehouseId()).thenReturn(null);
            validator.validate(param);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_WAREHOUSE_ID.code(), e.getCode());
        }
    }

    @Test
    public void validate_invalidReturnOrderId() {
        try {
            when(param.getCompanyId()).thenReturn(1l);
            when(param.getWarehouseId()).thenReturn(17l);
            when(param.getReturnOrderId()).thenReturn(1l);
            when(returnOrderService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(null);

            validator.validate(param);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_RETURN_ORDER_ID.code(), e.getCode());
        }
    }

    @Test
    public void validate_invalidExchangeCode_InCaseReturnOrderHasNotExchangeOrder() {
        try {
            when(param.getCompanyId()).thenReturn(1l);
            when(param.getWarehouseId()).thenReturn(17l);
            when(param.getReturnOrderId()).thenReturn(1l);
            when(returnOrderService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(returnOrder);
            when(returnOrder.getExchangeOrder()).thenReturn(null);

            validator.validate(param);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_EXCHANGE_ORDER_CODE_IN_RETURN_ORDER.code(), e.getCode());
        }
    }

    @Test
    public void validate_invalidExchangeCode_InCaseRExchangeOrderHasNotCode() {
        try {
            when(param.getCompanyId()).thenReturn(1l);
            when(param.getWarehouseId()).thenReturn(17l);
            when(param.getReturnOrderId()).thenReturn(1l);
            when(returnOrderService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(returnOrder);
            when(returnOrder.getExchangeOrder()).thenReturn(exchangeOrder);
            when(exchangeOrder.getCode()).thenReturn(null);

            validator.validate(param);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_EXCHANGE_ORDER_CODE_IN_RETURN_ORDER.code(), e.getCode());
        }
    }

    @Test
    public void validate_invalidExchangeCode_InCaseRExchangeOrderHasCodeDifferentFromRequestOrderCode() {
        try {
            when(param.getCompanyId()).thenReturn(1l);
            when(param.getWarehouseId()).thenReturn(17l);
            when(param.getReturnOrderId()).thenReturn(1l);
            when(returnOrderService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(returnOrder);
            when(returnOrder.getExchangeOrder()).thenReturn(exchangeOrder);
            when(exchangeOrder.getCode()).thenReturn("exchangeCode");
            when(param.getExchangeOrderCode()).thenReturn("abc");

            validator.validate(param);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_EXCHANGE_ORDER_CODE_IN_RETURN_ORDER.code(), e.getCode());
        }
    }

    @Test
    public void validate_success() {
        when(param.getCompanyId()).thenReturn(1l);
        when(param.getWarehouseId()).thenReturn(17l);
        when(param.getReturnOrderId()).thenReturn(1l);
        when(returnOrderService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(returnOrder);
        when(returnOrder.getExchangeOrder()).thenReturn(exchangeOrder);
        when(exchangeOrder.getCode()).thenReturn("exchangeCode");
        when(param.getExchangeOrderCode()).thenReturn("exchangeCode");

        validator.validate(param);
    }
}
