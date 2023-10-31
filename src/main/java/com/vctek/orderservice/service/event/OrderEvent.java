package com.vctek.orderservice.service.event;

import com.vctek.orderservice.dto.OrderEntryData;
import com.vctek.orderservice.model.OrderEntryModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.util.EventType;
import com.vctek.util.OrderStatus;

import java.util.List;

public class OrderEvent {
    private EventType eventType;
    private OrderStatus oldOrderStatus;
    private OrderModel orderModel;
    private List<OrderEntryData> oldEntries;
    private Long importDetailId;
    private Long currentUserId ;
    private boolean ecommerceOrder;

    public OrderEvent(OrderModel orderModel) {
        this.orderModel = orderModel;
    }

    public OrderModel getOrderModel() {
        return orderModel;
    }

    public void setOrderModel(OrderModel orderModel) {
        this.orderModel = orderModel;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public EventType getEventType() {
        return eventType;
    }

    public Long getImportDetailId() {
        return importDetailId;
    }

    public void setImportDetailId(Long importDetailId) {
        this.importDetailId = importDetailId;
    }

    public List<OrderEntryData> getOldEntries() {
        return oldEntries;
    }

    public void setOldEntries(List<OrderEntryData> oldEntries) {
        this.oldEntries = oldEntries;
    }

    public OrderStatus getOldOrderStatus() {
        return oldOrderStatus;
    }

    public void setOldOrderStatus(OrderStatus oldOrderStatus) {
        this.oldOrderStatus = oldOrderStatus;
    }

    public Long getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(Long currentUserId) {
        this.currentUserId = currentUserId;
    }

    public boolean isEcommerceOrder() {
        return ecommerceOrder;
    }

    public void setEcommerceOrder(boolean ecommerceOrder) {
        this.ecommerceOrder = ecommerceOrder;
    }
}
