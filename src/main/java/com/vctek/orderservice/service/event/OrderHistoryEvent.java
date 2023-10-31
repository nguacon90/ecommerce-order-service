package com.vctek.orderservice.service.event;

import com.vctek.orderservice.model.OrderHistoryModel;

public class OrderHistoryEvent {
    private OrderHistoryModel orderHistoryModel;

    public OrderHistoryEvent(OrderHistoryModel orderHistoryModel) {
        this.orderHistoryModel = orderHistoryModel;
    }

    public OrderHistoryModel getOrderHistoryModel() {
        return orderHistoryModel;
    }

    public void setOrderHistoryModel(OrderHistoryModel orderHistoryModel) {
        this.orderHistoryModel = orderHistoryModel;
    }
}
