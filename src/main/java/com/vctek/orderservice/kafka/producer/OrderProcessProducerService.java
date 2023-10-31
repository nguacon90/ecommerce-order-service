package com.vctek.orderservice.kafka.producer;

import com.vctek.kafka.data.order.OrderProcessData;

public interface OrderProcessProducerService {

    void sendOrderStatusImportKafka(OrderProcessData orderProcessData);
}
