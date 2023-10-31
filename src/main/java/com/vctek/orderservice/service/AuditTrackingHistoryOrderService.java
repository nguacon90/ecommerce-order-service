package com.vctek.orderservice.service;

import com.vctek.kafka.data.OrderData;

public interface AuditTrackingHistoryOrderService {
    void compareChangeFields(OrderData orderData);

}
