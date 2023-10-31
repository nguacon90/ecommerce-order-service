package com.vctek.orderservice.kafka.producer;

import com.vctek.orderservice.model.OrderModel;

import java.util.List;

public interface OrderBillLinkProducer {
    void produce(List<OrderModel> orders);
}
