package com.vctek.orderservice.service;

import com.vctek.migration.dto.InvoiceLinkDto;

import java.util.List;

public interface SyncInvoiceLinkService {
    void processInvoiceLinkMessage(List<InvoiceLinkDto> linkDtoList);
}
