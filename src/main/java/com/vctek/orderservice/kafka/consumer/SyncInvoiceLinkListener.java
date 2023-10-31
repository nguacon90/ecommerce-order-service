package com.vctek.orderservice.kafka.consumer;

import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.message.KafkaMessageType;
import com.vctek.kafka.service.KafkaListener;
import com.vctek.migration.dto.InvoiceLinkDto;
import com.vctek.migration.kafka.stream.SyncInvoiceLinkInStream;
import com.vctek.orderservice.service.SyncInvoiceLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SyncInvoiceLinkListener implements KafkaListener<List<InvoiceLinkDto>> {

    private SyncInvoiceLinkService syncInvoiceLinkService;

    @Override
    @StreamListener(SyncInvoiceLinkInStream.SYNC_INVOICE_LINK_IN)
    public void handleMessage(@Payload KafkaMessage<List<InvoiceLinkDto>> kafkaMessage) {
        if (KafkaMessageType.LINK_INVOICE_TO_ORDER.equals(kafkaMessage.getType())) {
            List<InvoiceLinkDto> linkDtoList = kafkaMessage.getContent();
            syncInvoiceLinkService.processInvoiceLinkMessage(linkDtoList);
        }
    }

    @Autowired
    public void setSyncInvoiceLinkService(SyncInvoiceLinkService syncInvoiceLinkService) {
        this.syncInvoiceLinkService = syncInvoiceLinkService;
    }
}
