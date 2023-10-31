package com.vctek.orderservice.kafka.producer;

import com.vctek.kafka.data.loyalty.TransactionRequest;
import com.vctek.kafka.message.KafkaMessageType;

public interface OrderLoyaltyRewardRequestProducer {
    void sendLoyaltyRewardRequestKafka(TransactionRequest request, KafkaMessageType type);
}
