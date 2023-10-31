package com.vctek.orderservice.kafka.producer;

import com.vctek.migration.dto.InvoiceLinkDto;

import java.util.List;

public interface InvoiceLinkProducer {
    void produce(List<InvoiceLinkDto> invoiceLinkList);
}
