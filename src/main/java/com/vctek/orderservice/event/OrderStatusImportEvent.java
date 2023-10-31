package com.vctek.orderservice.event;

import com.vctek.orderservice.model.OrderStatusImportModel;

public class OrderStatusImportEvent {
    private OrderStatusImportModel orderStatusImportModel;

    public OrderStatusImportEvent(OrderStatusImportModel orderStatusImportModel) {
        this.orderStatusImportModel = orderStatusImportModel;
    }

    public OrderStatusImportModel getOrderStatusImportModel() {
        return orderStatusImportModel;
    }

    public void setOrderStatusImportModel(OrderStatusImportModel orderStatusImportModel) {
        this.orderStatusImportModel = orderStatusImportModel;
    }
}
