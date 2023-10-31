package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.PaymentMethodData;
import com.vctek.orderservice.elasticsearch.model.PaymentTransactionData;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.PaymentTransactionModel;
import com.vctek.orderservice.model.ReturnOrderModel;
import com.vctek.orderservice.service.FinanceService;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrderPaymentTransactionPopulatorTest {

    private Populator<PaymentTransactionModel, PaymentTransactionData> populator;
    private FinanceService financeService;
    private PaymentTransactionModel source;
    private PaymentTransactionData data;


    @Before
    public void setUp() {
        financeService = mock(FinanceService.class);
        source = new PaymentTransactionModel();
        source.setId(1L);
        source.setNote("name");
        source.setAmount(1.0);
        source.setMoneySourceId(1l);
        source.setPaymentMethodId(1l);
        source.setInvoiceId(1L);
        data = new PaymentTransactionData();
    }

    @Test
    public void populate_order() {
        populator = new OrderPaymentTransactionPopulator();
        ((OrderPaymentTransactionPopulator) populator).setFinanceService(financeService);
        when(financeService.getPaymentMethod(anyLong())).thenReturn(new PaymentMethodData());
        OrderModel orderModel = new OrderModel();
        orderModel.setId(1l);
        source.setOrderModel(orderModel);
        populator.populate(source, data);
        assertEquals(source.getAmount(), data.getAmount());
        assertEquals(source.getMoneySourceId(), data.getMoneySourceId());
        assertEquals(source.getPaymentMethodId(), data.getPaymentMethodId());
        assertEquals(source.getOrderModel().getId(), data.getOrderId());
        assertEquals(source.getInvoiceId(), data.getInvoiceId());
    }

    @Test
    public void populate_returnOrder() {
        populator = new ReturnOrderPaymentTransactionPopulator();
        ((OrderPaymentTransactionPopulator) populator).setFinanceService(financeService);
        when(financeService.getPaymentMethod(anyLong())).thenReturn(new PaymentMethodData());

        ReturnOrderModel orderModel = new ReturnOrderModel();
        orderModel.setId(1l);
        source.setReturnOrder(orderModel);
        populator.populate(source, data);
        assertEquals(source.getAmount(), data.getAmount());
        assertEquals(source.getMoneySourceId(), data.getMoneySourceId());
        assertEquals(source.getPaymentMethodId(), data.getPaymentMethodId());
        assertEquals(source.getReturnOrder().getId(), data.getReturnOrderId());
        assertEquals(source.getInvoiceId(), data.getInvoiceId());
    }
}
