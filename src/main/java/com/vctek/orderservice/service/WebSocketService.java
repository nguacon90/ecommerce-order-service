package com.vctek.orderservice.service;

import com.vctek.orderservice.model.OrderModel;

public interface WebSocketService {
    void sendNotificationChangeOrderStatus(OrderModel orderModel);
}
