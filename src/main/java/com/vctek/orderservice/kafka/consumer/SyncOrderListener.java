package com.vctek.orderservice.kafka.consumer;

import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.message.KafkaMessageType;
import com.vctek.kafka.service.KafkaListener;
import com.vctek.migration.dto.MigrateBillDto;
import com.vctek.migration.kafka.stream.SyncBIllInStream;
import com.vctek.orderservice.facade.SyncOrderFacade;
import com.vctek.orderservice.facade.SyncReturnOrderFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class SyncOrderListener implements KafkaListener<MigrateBillDto> {
    private SyncOrderFacade syncOrderFacade;
    private SyncReturnOrderFacade syncReturnOrderFacade;

    @Override
    @StreamListener(SyncBIllInStream.SYNC_BILL_IN)
    public void handleMessage(@Payload KafkaMessage<MigrateBillDto> kafkaMessage) {
        if (KafkaMessageType.MIGRATION_ORDER.equals(kafkaMessage.getType()) || KafkaMessageType.MIGRATION_ONLY_ORDER.equals(kafkaMessage.getType())) {
            MigrateBillDto data = kafkaMessage.getContent();
            syncOrderFacade.processSyncOrderMessage(data);
        } else if (KafkaMessageType.MIGRATE_RETURN_ORDER.equals(kafkaMessage.getType())) {
            MigrateBillDto data = kafkaMessage.getContent();
            syncReturnOrderFacade.processSyncReturnOrderMessage(data);
        }

    }

    @Autowired
    public void setSyncOrderFacade(SyncOrderFacade syncOrderFacade) {
        this.syncOrderFacade = syncOrderFacade;
    }

    @Autowired
    public void setSyncReturnOrderFacade(SyncReturnOrderFacade syncReturnOrderFacade) {
        this.syncReturnOrderFacade = syncReturnOrderFacade;
    }
}
