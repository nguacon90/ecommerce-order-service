package com.vctek.orderservice.feignclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InvoiceData {
    private Long id;
    private Double finalAmount;
    private ItemData paymentMethod;
    private ItemData moneySource;
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(Double finalAmount) {
        this.finalAmount = finalAmount;
    }

    public ItemData getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(ItemData paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public ItemData getMoneySource() {
        return moneySource;
    }

    public void setMoneySource(ItemData moneySource) {
        this.moneySource = moneySource;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
