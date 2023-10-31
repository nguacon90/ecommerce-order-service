package com.vctek.orderservice.validator;

import com.vctek.orderservice.converter.populator.OrderPaymentTransactionRequestPopulator;
import com.vctek.orderservice.dto.PaymentMethodData;
import com.vctek.orderservice.dto.UpdateOrderParameter;
import com.vctek.orderservice.dto.request.CustomerRequest;
import com.vctek.orderservice.dto.request.OrderRequest;
import com.vctek.orderservice.dto.request.PaymentTransactionRequest;
import com.vctek.orderservice.dto.request.VatRequest;
import com.vctek.orderservice.feignclient.dto.LoyaltyCardData;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.PaymentTransactionModel;
import com.vctek.orderservice.service.FinanceService;
import com.vctek.orderservice.service.LoyaltyService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class OrderPaymentTransactionRequestPopulatorTest {
    @Mock
    private OrderRequest orderRequest;
    @Mock
    private UpdateOrderParameter paramter;
    @Mock
    private PaymentTransactionRequest payment1;
    @Mock
    private PaymentTransactionRequest payment2;

    private PaymentTransactionModel paymentModel1;

    @Mock
    private OrderModel order;
    @Mock
    private FinanceService financeService;
    @Mock
    private LoyaltyService loyaltyService;

    private Set<PaymentTransactionModel> orderPayments = new HashSet<>();

    private OrderPaymentTransactionRequestPopulator populator;
    private List<PaymentTransactionRequest> payments = new ArrayList<>();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(orderRequest.getCode()).thenReturn("code");
        when(orderRequest.getPayments()).thenReturn(payments);
        when(paramter.getOrder()).thenReturn(order);
        paymentModel1 = new PaymentTransactionModel();
        paymentModel1.setId(1l);
        paymentModel1.setAmount(20d);

        populator = new OrderPaymentTransactionRequestPopulator(financeService, loyaltyService);
    }

    @Test
    public void populate_filterNewPaymentWithAmountIs0() {
        when(order.getPaymentTransactions()).thenReturn(orderPayments);
        when(payment1.getAmount()).thenReturn(10d);
        when(payment2.getAmount()).thenReturn(0d);
        when(orderRequest.getVatInfo()).thenReturn(new VatRequest());
        when(orderRequest.getCustomer()).thenReturn(new CustomerRequest());
        PaymentMethodData paymentMethodData = new PaymentMethodData();
        paymentMethodData.setId(1L);
        when(financeService.getPaymentMethodByCode(anyString())).thenReturn(paymentMethodData);
        when(loyaltyService.findByCardNumber(any(), any())).thenReturn(new LoyaltyCardData());
        payments.add(payment1);
        payments.add(payment2);

        populator.populate(payments, order);
        List<PaymentTransactionModel> actualPayments = orderPayments.stream().collect(Collectors.toList());
        assertEquals(1, actualPayments.size());
        assertEquals(10d, actualPayments.get(0).getAmount(), 0);

    }

    @Test
    public void populate_UpdateAllExistedPaymentsInOrder() {
        orderPayments.add(paymentModel1);
        when(order.getPaymentTransactions()).thenReturn(orderPayments);
        when(payment1.getId()).thenReturn(paymentModel1.getId());
        when(payment1.getAmount()).thenReturn(30d);
        PaymentMethodData paymentMethodData = new PaymentMethodData();
        paymentMethodData.setId(1L);
        when(financeService.getPaymentMethodByCode(anyString())).thenReturn(paymentMethodData);
        when(loyaltyService.findByCardNumber(any(), any())).thenReturn(new LoyaltyCardData());
        payments.add(payment1);

        populator.populate(payments, order);
        List<PaymentTransactionModel> actualPayments = orderPayments.stream().collect(Collectors.toList());
        assertEquals(1, actualPayments.size());
        assertEquals(30d, actualPayments.get(0).getAmount(), 0);

    }

    @Test
    public void populate_UpdateAddNewPaymentsInOrder() {
        orderPayments.add(paymentModel1);
        when(order.getPaymentTransactions()).thenReturn(orderPayments);
        when(payment1.getId()).thenReturn(paymentModel1.getId());
        when(payment1.getAmount()).thenReturn(30d);
        when(payment2.getId()).thenReturn(null);
        when(payment2.getAmount()).thenReturn(20d);
        when(payment2.getMoneySourceId()).thenReturn(222l);
        when(payment2.getPaymentMethodId()).thenReturn(223l);
        PaymentMethodData paymentMethodData = new PaymentMethodData();
        paymentMethodData.setId(1L);
        when(financeService.getPaymentMethodByCode(anyString())).thenReturn(paymentMethodData);
        when(loyaltyService.findByCardNumber(any(), any())).thenReturn(new LoyaltyCardData());
        payments.add(payment1);
        payments.add(payment2);

        populator.populate(payments, order);
        List<PaymentTransactionModel> actualPayments = orderPayments.stream().collect(Collectors.toList());
        assertEquals(2, actualPayments.size());
        assertEquals(1l, actualPayments.get(0).getId(), 0);
        assertEquals(30d, actualPayments.get(0).getAmount(), 0);

        assertNull(actualPayments.get(1).getId());
        assertEquals(20d, actualPayments.get(1).getAmount(), 0);

    }

    @Test
    public void populate_UpdateAddNewPaymentsInOrder_TheSameMoneySourceAndPaymentMethod() {
        orderPayments.add(paymentModel1);
        paymentModel1.setMoneySourceId(222l);
        paymentModel1.setPaymentMethodId(223l);
        when(order.getPaymentTransactions()).thenReturn(orderPayments);
        when(payment2.getId()).thenReturn(null);
        when(payment2.getAmount()).thenReturn(20d);
        when(payment2.getMoneySourceId()).thenReturn(222l);
        when(payment2.getPaymentMethodId()).thenReturn(223l);
        PaymentMethodData paymentMethodData = new PaymentMethodData();
        paymentMethodData.setId(1L);
        when(financeService.getPaymentMethodByCode(anyString())).thenReturn(paymentMethodData);
        when(loyaltyService.findByCardNumber(any(), any())).thenReturn(new LoyaltyCardData());
        payments.add(payment2);

        populator.populate(payments, order);
        List<PaymentTransactionModel> actualPayments = orderPayments.stream().collect(Collectors.toList());
        assertEquals(1, actualPayments.size());
        assertEquals(1l, actualPayments.get(0).getId(), 0);
        assertEquals(20d, actualPayments.get(0).getAmount(), 0);
    }
}
