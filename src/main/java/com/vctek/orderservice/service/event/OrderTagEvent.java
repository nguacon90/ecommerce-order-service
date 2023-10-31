package com.vctek.orderservice.service.event;

import com.vctek.orderservice.model.OrderModel;
import org.springframework.context.ApplicationEvent;

public class OrderTagEvent {
    private OrderModel orderModel;
    public OrderTagEvent(OrderModel model) {
        this.orderModel = model;
    }

    public OrderModel getOrderModel() {
        return orderModel;
    }
}
