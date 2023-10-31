package com.vctek.orderservice.kafka.producer.impl;

import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.message.KafkaMessageType;
import com.vctek.kafka.service.KafkaProducerService;
import com.vctek.migration.dto.InvoiceLinkDto;
import com.vctek.migration.kafka.stream.SyncInvoiceLinkOutStream;
import com.vctek.orderservice.kafka.producer.InvoiceLinkProducer;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InvoiceLinkProducerImpl implements InvoiceLinkProducer {

    private KafkaProducerService kafkaProducerService;
    private SyncInvoiceLinkOutStream syncInvoiceLinkOutStream;

    public InvoiceLinkProducerImpl(KafkaProducerService kafkaProducerService,
                                   SyncInvoiceLinkOutStream syncInvoiceLinkOutStream) {
        this.kafkaProducerService = kafkaProducerService;
        this.syncInvoiceLinkOutStream = syncInvoiceLinkOutStream;
    }

    @Override
    public void produce(List<InvoiceLinkDto> invoiceLinkList) {
        KafkaMessage<List<InvoiceLinkDto>> kafkaMessage = new KafkaMessage<>();
        kafkaMessage.setContent(invoiceLinkList);
        kafkaMessage.setType(KafkaMessageType.LINK_ORDER_TO_INVOICE);
        kafkaProducerService.send(kafkaMessage, syncInvoiceLinkOutStream.produce());
    }
}
