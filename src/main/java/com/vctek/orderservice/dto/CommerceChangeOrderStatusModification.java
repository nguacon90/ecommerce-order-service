package com.vctek.orderservice.dto;

import com.vctek.orderservice.model.OrderModel;

public class CommerceChangeOrderStatusModification {
    private OrderModel orderModel;
    private String retailOrderCode;
    public CommerceChangeOrderStatusModification(OrderModel order) {
        this.orderModel = order;
    }

    public OrderModel getOrderModel() {
        return orderModel;
    }

    public void setOrderModel(OrderModel orderModel) {
        this.orderModel = orderModel;
    }

    public String getRetailOrderCode() {
        return retailOrderCode;
    }

    public void setRetailOrderCode(String retailOrderCode) {
        this.retailOrderCode = retailOrderCode;
    }
}
