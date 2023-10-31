package com.vctek.orderservice.dto;

import com.vctek.orderservice.model.OrderModel;

public class CommerceOrderResult {
    private OrderModel orderModel;

    public OrderModel getOrderModel() {
        return orderModel;
    }

    public void setOrderModel(OrderModel orderModel) {
        this.orderModel = orderModel;
    }
}
