package com.vctek.orderservice.kafka.producer.impl;

import com.vctek.kafka.data.loyalty.TransactionRequest;
import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.message.KafkaMessageType;
import com.vctek.kafka.service.KafkaProducerService;
import com.vctek.kafka.stream.loyalty.LoyaltyRewardRequestOutStream;
import com.vctek.orderservice.kafka.producer.OrderLoyaltyRewardRequestProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderLoyaltyRewardRequestProducerImpl implements OrderLoyaltyRewardRequestProducer {
    private KafkaProducerService kafkaProducerService;
    private LoyaltyRewardRequestOutStream loyaltyRewardRequestOutStream;

    @Override
    public void sendLoyaltyRewardRequestKafka(TransactionRequest request, KafkaMessageType type) {
        KafkaMessage<TransactionRequest> kafkaMessage = new KafkaMessage<>();
        kafkaMessage.setContent(request);
        kafkaMessage.setType(type);
        kafkaProducerService.send(kafkaMessage, loyaltyRewardRequestOutStream.produceLoyaltyRewardRequestTopic());
    }

    @Autowired
    public void setKafkaProducerService(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    @Autowired
    public OrderLoyaltyRewardRequestProducerImpl(KafkaProducerService kafkaProducerService, LoyaltyRewardRequestOutStream loyaltyRewardRequestOutStream) {
        this.kafkaProducerService = kafkaProducerService;
        this.loyaltyRewardRequestOutStream = loyaltyRewardRequestOutStream;
    }
}
