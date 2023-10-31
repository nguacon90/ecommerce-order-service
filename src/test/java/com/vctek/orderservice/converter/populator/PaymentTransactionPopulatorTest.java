package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.PaymentTransactionData;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.PaymentTransactionModel;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PaymentTransactionPopulatorTest {
    private Populator<PaymentTransactionModel, PaymentTransactionData> paymentTransactionDataPopulator;

    @Before
    public void setUp() {
        paymentTransactionDataPopulator = new PaymentTransactionPopulator();
    }

    @Test
    public void populate() {
        PaymentTransactionModel source = new PaymentTransactionModel();
        source.setId(1L);
        source.setNote("name");
        source.setAmount(1.0);
        source.setMoneySourceId(1l);
        source.setPaymentMethodId(1l);
        OrderModel orderModel = new OrderModel();
        orderModel.setId(1l);
        source.setOrderModel(orderModel);

        PaymentTransactionData data = new PaymentTransactionData();
        paymentTransactionDataPopulator.populate(source, data);

        assertEquals(source.getId(), data.getId());
        assertEquals(source.getNote(), data.getNote());
        assertEquals(source.getAmount(), data.getAmount());
        assertEquals(source.getMoneySourceId(), data.getMoneySourceId());
        assertEquals(source.getPaymentMethodId(), data.getPaymentMethodId());
        assertEquals(source.getOrderModel().getId(), data.getOrderId());
    }
}
