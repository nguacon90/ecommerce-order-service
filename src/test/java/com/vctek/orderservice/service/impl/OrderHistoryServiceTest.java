package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.OrderHistoryModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.repository.OrderHistoryRepository;
import com.vctek.util.OrderStatus;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class OrderHistoryServiceTest {

    private OrderHistoryServiceImpl orderHistoryService;
    private OrderHistoryRepository orderHistoryRepository;
    private OrderHistoryModel orderHistory = new OrderHistoryModel();
    private OrderHistoryModel orderHistory2 = new OrderHistoryModel();

    @Before
    public void setUp() {
        orderHistoryRepository = mock(OrderHistoryRepository.class);
        orderHistoryService = new OrderHistoryServiceImpl(orderHistoryRepository);
    }

    @Test
    public void findAllByOrderId() {
        when(orderHistoryRepository.findAllByOrderId(anyLong())).thenReturn(Collections.emptyList());
        orderHistoryService.findAllByOrderId(1l);
        verify(orderHistoryRepository).findAllByOrderId(anyLong());
    }

    @Test
    public void findAllByOrder() {
        when(orderHistoryRepository.findAllByOrderOrderByModifiedTimeDesc(any(AbstractOrderModel.class))).thenReturn(Arrays.asList(new OrderHistoryModel()));
        orderHistoryService.findAllByOrder(new OrderModel());
        verify(orderHistoryRepository).findAllByOrderOrderByModifiedTimeDesc(any(AbstractOrderModel.class));
    }

    @Test
    public void save() {
        OrderHistoryModel model = new OrderHistoryModel();
        model.setExtraData("gì đó");
        model.setCurrentStatus("gì đó");
        model.setPreviousStatus("gì đó");
        model.setOrder(new OrderModel());
        orderHistoryService.save(model);
        verify(orderHistoryRepository).save(model);
    }

    @Test
    public void hasChangeShippingToOtherStatus_falseCase() {
        OrderModel orderModel = new OrderModel();
        orderHistory.setPreviousStatus(OrderStatus.CONFIRMED.code());
        orderHistory.setCurrentStatus(OrderStatus.SHIPPING.code());
        orderModel.setOrderHistory(Arrays.asList(orderHistory));

        assertFalse(orderHistoryService.hasChangeShippingToOtherStatus(orderModel));
    }

    @Test
    public void hasChangeShippingToOtherStatus_trueCase() {
        OrderModel orderModel = new OrderModel();
        orderHistory.setPreviousStatus(OrderStatus.CONFIRMED.code());
        orderHistory.setCurrentStatus(OrderStatus.SHIPPING.code());

        orderHistory.setPreviousStatus(OrderStatus.SHIPPING.code());
        orderHistory.setCurrentStatus(OrderStatus.COMPLETED.code());
        orderModel.setOrderHistory(Arrays.asList(orderHistory, orderHistory2));

        assertTrue(orderHistoryService.hasChangeShippingToOtherStatus(orderModel));
    }
}