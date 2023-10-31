package com.vctek.orderservice.kafka.consumer;

import com.vctek.kafka.data.loyalty.TransactionData;
import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.service.KafkaListener;
import com.vctek.kafka.stream.loyalty.LoyaltyRewardResponseInStream;
import com.vctek.orderservice.service.LoyaltyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class LoyaltyRewardResponseListener implements KafkaListener<TransactionData> {

    private LoyaltyService loyaltyService;

    @Override
    @StreamListener(LoyaltyRewardResponseInStream.LOYALTY_REWARD_RESPONSE_IN)
    public void handleMessage(@Payload KafkaMessage<TransactionData> kafkaMessage) {
        TransactionData transactionData = kafkaMessage.getContent();
        loyaltyService.splitRewardAmountToEntriesAndCreateLoyaltyTransaction(transactionData);
    }

    @Autowired
    public void setLoyaltyService(LoyaltyService loyaltyService) {
        this.loyaltyService = loyaltyService;
    }
}
