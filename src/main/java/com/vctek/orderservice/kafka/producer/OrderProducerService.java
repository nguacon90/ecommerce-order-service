package com.vctek.orderservice.kafka.producer;

import com.vctek.kafka.message.KafkaMessageType;
import com.vctek.orderservice.model.OrderHistoryModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.service.event.OrderEvent;

public interface OrderProducerService {

    void sendOrderKafka(OrderEvent event);

    void recalculateOrderReport(OrderModel orderModel, KafkaMessageType kafkaMessageType, Long currentUserId);

    void sendChangeStatusKafka(OrderHistoryModel orderHistoryModel);

    void produceUpdateOrderTag(OrderModel orderModel);
}
