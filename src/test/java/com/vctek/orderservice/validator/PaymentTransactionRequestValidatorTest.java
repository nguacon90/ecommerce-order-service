package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.MoneySourceData;
import com.vctek.orderservice.dto.PaymentMethodData;
import com.vctek.orderservice.dto.request.PaymentTransactionRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.FinanceClient;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.PaymentTransactionModel;
import com.vctek.orderservice.service.OrderService;
import com.vctek.orderservice.service.PaymentTransactionService;
import com.vctek.validate.Validator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PaymentTransactionRequestValidatorTest {
    private Validator<PaymentTransactionRequest> validator;
    private PaymentTransactionRequest request;
    private PaymentTransactionService service;
    private PaymentTransactionModel model;
    private FinanceClient financeClient;
    private OrderService orderService;

    @Before
    public void setUp() {
        orderService = mock(OrderService.class);
        financeClient = mock(FinanceClient.class);
        model = new PaymentTransactionModel();
        model.setId(1l);
        service = mock(PaymentTransactionService.class);
        request = new PaymentTransactionRequest();
        validator = new PaymentTransactionRequestValidator(service, orderService, financeClient);
    }

    @Test
    public void validate_paymentTransactionId_Invalid() {
        request.setId(1l);
        when(service.findById(anyLong())).thenReturn(null);
        try {
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_PAYMENT_TRANSACTION_ID.code(), e.getCode());
        }
    }

    @Test
    public void validate_EmptyCompanyId() {
        request.setId(1l);
        when(service.findById(anyLong())).thenReturn(model);
        try {
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_COMPANY_ID.code(), e.getCode());
        }
    }

    @Test
    public void validate_EmptyOrderId() {
        request.setId(1l);
        request.setCompanyId(1l);
        when(service.findById(anyLong())).thenReturn(model);
        try {
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_ORDER_ID.code(), e.getCode());
        }
    }


    @Test
    public void validate_InvalidOrderId() {
        request.setId(1l);
        request.setCompanyId(1l);
        request.setOrderId(1l);
        when(service.findById(anyLong())).thenReturn(model);
        when(orderService.findById(anyLong())).thenReturn(null);
        try {
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_ID.code(), e.getCode());
        }
    }

    @Test
    public void validate_EmptyMoneySourceId() {
        request.setId(1l);
        request.setCompanyId(1l);
        request.setOrderId(1l);
        when(service.findById(anyLong())).thenReturn(model);
        when(orderService.findById(anyLong())).thenReturn(new OrderModel());
        try {
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_MONEY_SOURCE_ID.code(), e.getCode());
        }
    }

    @Test
    public void validate_InvalidMoneySourceId() {
        request.setId(1l);
        request.setCompanyId(1l);
        request.setOrderId(1l);
        request.setMoneySourceId(1l);
        when(service.findById(anyLong())).thenReturn(model);
        when(orderService.findById(anyLong())).thenReturn(new OrderModel());
        when(financeClient.getMoneySource(anyLong(),anyLong())).thenReturn(null);
        try {
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_MONEY_SOURCE_ID.code(), e.getCode());
        }
    }

    @Test
    public void validate_emptyPaymentSourceId() {
        request.setId(1l);
        request.setCompanyId(1l);
        request.setOrderId(1l);
        request.setMoneySourceId(1l);
        when(service.findById(anyLong())).thenReturn(model);
        when(orderService.findById(anyLong())).thenReturn(new OrderModel());
        MoneySourceData moneySourceData = new MoneySourceData();
        moneySourceData.setId(1l);
        when(financeClient.getMoneySource(anyLong(),anyLong())).thenReturn(moneySourceData);
        try {
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_PAYMENT_METHOD_ID.code(), e.getCode());
        }
    }

    @Test
    public void validate_InvalidPaymentSourceId() {
        request.setId(1l);
        request.setCompanyId(1l);
        request.setOrderId(1l);
        request.setMoneySourceId(1l);
        request.setPaymentMethodId(1l);
        when(service.findById(anyLong())).thenReturn(model);
        when(orderService.findById(anyLong())).thenReturn(new OrderModel());
        MoneySourceData moneySourceData = new MoneySourceData();
        moneySourceData.setId(1l);
        when(financeClient.getMoneySource(anyLong(),anyLong())).thenReturn(moneySourceData);
        when(financeClient.getPaymentMethodData(anyLong())).thenReturn(null);
        try {
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_PAYMENT_METHOD_ID.code(), e.getCode());
        }
    }

    @Test
    public void validator() {
        request.setId(1l);
        request.setCompanyId(1l);
        request.setOrderId(1l);
        request.setMoneySourceId(1l);
        request.setPaymentMethodId(1l);
        when(service.findById(anyLong())).thenReturn(model);
        when(orderService.findById(anyLong())).thenReturn(new OrderModel());
        MoneySourceData moneySourceData = new MoneySourceData();
        moneySourceData.setId(1l);
        when(financeClient.getMoneySource(anyLong(),anyLong())).thenReturn(moneySourceData);
        PaymentMethodData paymentMethodData = new PaymentMethodData();
        paymentMethodData.setId(1l);
        when(financeClient.getPaymentMethodData(anyLong())).thenReturn(paymentMethodData);
        validator.validate(request);
    }
}
