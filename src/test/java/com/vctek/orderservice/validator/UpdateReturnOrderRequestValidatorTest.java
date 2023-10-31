package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.request.PaymentTransactionRequest;
import com.vctek.orderservice.dto.request.ReturnOrderEntryRequest;
import com.vctek.orderservice.dto.request.ReturnOrderRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.OrderEntryModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.ReturnOrderModel;
import com.vctek.orderservice.service.OrderService;
import com.vctek.orderservice.service.ReturnOrderService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UpdateReturnOrderRequestValidatorTest {
    private UpdateReturnOrderRequestValidator validator;

    @Mock
    private ReturnOrderService returnOrderService;
    private ReturnOrderRequest request = new ReturnOrderRequest();
    @Mock
    private OrderModel order;
    @Mock
    private OrderEntryModel entry;
    private ReturnOrderEntryRequest returnOrderEntryRequest = new ReturnOrderEntryRequest();
    private List<AbstractOrderEntryModel> entries = new ArrayList<>();
    private List<ReturnOrderEntryRequest> returnOrderEntries = new ArrayList<>();
    private ReturnOrderModel returnOrder;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        returnOrderEntries.add(returnOrderEntryRequest);
        entries.add(entry);
        when(order.getEntries()).thenReturn(entries);
        when(entry.getEntryNumber()).thenReturn(0);
        when(entry.getId()).thenReturn(0l);
        when(entry.getQuantity()).thenReturn(3l);
        validator = new UpdateReturnOrderRequestValidator();
        validator.setReturnOrderService(returnOrderService);

        returnOrder = new ReturnOrderModel();
        returnOrder.setId(1l);
    }

    @Test
    public void validate_emptyCompanyId() {
        try {
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_COMPANY_ID.code(), e.getCode());
        }
    }

    @Test
    public void invalid_return_order_id() {
        try {
            request.setId(2l);
            request.setNote("note");
            request.setVatNumber("123");
            request.setOriginOrderCode("1233");
            request.setCompanyId(1l);
            when(returnOrderService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(null);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_RETURN_ORDER_ID.code(), e.getCode());
        }
    }

    @Test
    public void validate_shipping_mustBeLargeZero() {
        try {
            request.setId(2l);
            request.setNote("note");
            request.setVatNumber("123");
            request.setOriginOrderCode("1233");
            request.setCompanyId(1l);
            request.setShippingFee(-10d);

            returnOrder.setRefundAmount(10000d);

            when(returnOrderService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(returnOrder);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.SHIPPING_FEE_MUST_BE_LARGE_ZERO.code(), e.getCode());
        }
    }

    @Test
    public void validate_noteOverMaxLength() {
        try {
            request.setId(12L);
            request.setCompanyId(1l);
            request.setNote(OrderRequestValidatorTest.NOTE + OrderRequestValidatorTest.NOTE);
            when(returnOrderService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(returnOrder);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.NOTE_OVER_MAX_LENGTH.code(), e.getCode());
        }
    }

    @Test
    public void validate_success() {
        request.setId(1l);
        request.setCompanyId(1l);
        request.setOriginOrderCode("originOrderCode");
        request.setNote(OrderRequestValidatorTest.NOTE);
        returnOrder.setExchangeOrder(order);
        when(returnOrderService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(returnOrder);
        validator.validate(request);
        verify(returnOrderService).findByIdAndCompanyId(anyLong(), anyLong());
    }

}
