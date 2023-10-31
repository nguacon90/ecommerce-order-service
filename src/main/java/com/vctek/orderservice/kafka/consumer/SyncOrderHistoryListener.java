package com.vctek.orderservice.kafka.consumer;

import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.service.KafkaListener;
import com.vctek.migration.dto.MigrateOrderHistoryDto;
import com.vctek.migration.kafka.stream.SyncOrderHistoryInStream;
import com.vctek.orderservice.facade.OrderHistoryFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class SyncOrderHistoryListener implements KafkaListener<MigrateOrderHistoryDto> {

    private OrderHistoryFacade orderHistoryFacade;

    @Override
    @StreamListener(SyncOrderHistoryInStream.SYNC_ORDER_HISTORY_IN)
    public void handleMessage(@Payload KafkaMessage<MigrateOrderHistoryDto> kafkaMessage) {
        MigrateOrderHistoryDto migrateOrderHistoryDto = kafkaMessage.getContent();
        orderHistoryFacade.migrateOrderHistory(migrateOrderHistoryDto);
    }

    @Autowired
    public void setOrderHistoryFacade(OrderHistoryFacade orderHistoryFacade) {
        this.orderHistoryFacade = orderHistoryFacade;
    }
}
