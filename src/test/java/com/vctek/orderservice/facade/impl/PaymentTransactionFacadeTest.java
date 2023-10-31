package com.vctek.orderservice.facade.impl;

import com.vctek.converter.Converter;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.PaymentTransactionData;
import com.vctek.orderservice.dto.request.PaymentTransactionRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.PaymentTransactionFacade;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.PaymentTransactionModel;
import com.vctek.orderservice.service.OrderService;
import com.vctek.orderservice.service.PaymentTransactionService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class PaymentTransactionFacadeTest {
    private PaymentTransactionFacade paymentTransactionFacade;
    private PaymentTransactionService paymentTransactionService;
    private Converter<PaymentTransactionModel, PaymentTransactionData> converter;
    private ArgumentCaptor<PaymentTransactionModel> captor;
    private PaymentTransactionModel model;
    private PaymentTransactionRequest request;
    private OrderService orderService;

    @Before
    public void setUp() {
        model = new PaymentTransactionModel();
        model.setId(1l);
        orderService = mock(OrderService.class);
        captor = ArgumentCaptor.forClass(PaymentTransactionModel.class);
        request = new PaymentTransactionRequest();
        paymentTransactionService = mock(PaymentTransactionService.class);
        converter = mock(Converter.class);
        paymentTransactionFacade = new PaymentTransactionFacadeImpl(paymentTransactionService, converter, orderService);
    }

    @Test
    public void create_success() {
        request.setNote("name");
        request.setAmount(1.0);
        request.setOrderId(1l);
        request.setMoneySourceId(1l);
        request.setPaymentMethodId(2l);
        OrderModel orderModel = new OrderModel();
        orderModel.setId(1l);
        when(orderService.findById(anyLong())).thenReturn(orderModel);
        when(paymentTransactionService.findById(anyLong())).thenReturn(model);
        paymentTransactionFacade.create(request);
        verify(paymentTransactionService).save(captor.capture());
    }

    @Test
    public void create_InvalidId() {
        try {
            request.setNote("name");
            request.setAmount(1.0);
            request.setOrderId(1l);
            request.setMoneySourceId(1l);
            request.setPaymentMethodId(2l);
            OrderModel orderModel = new OrderModel();
            orderModel.setId(1l);
            when(orderService.findById(anyLong())).thenReturn(orderModel);
            when(paymentTransactionService.findById(anyLong())).thenReturn(null);
            paymentTransactionFacade.create(request);
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_PAYMENT_TRANSACTION_ID.code(), e.getCode());
        }
    }

    @Test
    public void update_success() {
        request.setId(1l);
        request.setNote("name");
        request.setAmount(1.0);
        request.setOrderId(1l);
        request.setMoneySourceId(1l);
        request.setPaymentMethodId(2l);
        OrderModel orderModel = new OrderModel();
        orderModel.setId(1l);
        when(orderService.findById(anyLong())).thenReturn(orderModel);
        when(paymentTransactionService.findById(anyLong())).thenReturn(model);
        paymentTransactionFacade.update(request);
        verify(paymentTransactionService).save(captor.capture());
    }

    @Test
    public void findById() {
        when(paymentTransactionService.findById(anyLong())).thenReturn(model);
        paymentTransactionFacade.findById(1l);
        verify(paymentTransactionService).findById(1l);
    }

    @Test
    public void findById_invalid() {
        try {
            when(paymentTransactionService.findById(anyLong())).thenReturn(null);
            paymentTransactionFacade.findById(1l);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_PAYMENT_TRANSACTION_ID.code(), e.getCode());
        }
    }

    @Test
    public void findAll() {
        paymentTransactionFacade.findAll();
        verify(paymentTransactionService).findAll();
    }

    @Test
    public void delete() {
        when(paymentTransactionService.findById(anyLong())).thenReturn(model);
        paymentTransactionFacade.delete(1l);
        verify(paymentTransactionService).delete(model);
    }

    @Test
    public void delete_InValidId() {
        try {
            when(paymentTransactionService.findById(anyLong())).thenReturn(null);
            paymentTransactionFacade.delete(1l);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_PAYMENT_TRANSACTION_ID.code(), e.getCode());
        }
    }
}
