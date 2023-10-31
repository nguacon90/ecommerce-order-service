package com.vctek.orderservice.feignclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InvoiceOrderData {
    private Map<Long, Long> invoicePaymentMap = new HashMap<>();

    public Map<Long, Long> getInvoicePaymentMap() {
        return invoicePaymentMap;
    }

    public void setInvoicePaymentMap(Map<Long, Long> invoicePaymentMap) {
        this.invoicePaymentMap = invoicePaymentMap;
    }
}
