package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.kafka.data.loyalty.TransactionRequest;
import com.vctek.orderservice.model.OrderModel;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TransactionRequestPopulatorTest {
    private Populator<OrderModel, TransactionRequest> populator;
    private OrderModel orderModel;

    @Before
    public void setUp() {
        populator = new TransactionRequestPopulator();
        orderModel = new OrderModel();
        orderModel.setCompanyId(1L);
        orderModel.setWarehouseId(2L);
        orderModel.setCardNumber("cardNumber");
        orderModel.setCode("orderCode");
        orderModel.setType("orderType");
    }

    @Test
    public void populate() {
        TransactionRequest request = new TransactionRequest();
        populator.populate(orderModel, request);
        assertEquals(1L, request.getCompanyId(), 0);
        assertEquals(2L, request.getWarehouseId(), 0);
        assertEquals("cardNumber", request.getCardNumber());
    }
}
