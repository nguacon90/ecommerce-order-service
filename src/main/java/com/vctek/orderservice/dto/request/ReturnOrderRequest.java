package com.vctek.orderservice.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReturnOrderRequest {
    private Long id;
    private Long companyId;
    private String originOrderCode;
    private String exchangeCartCode;
    private String exchangeLoyaltyCard;
    private String note;
    private boolean isExchange;
    private Double totalTax;
    private List<ReturnOrderEntryRequest> returnOrderEntries;
    private List<PaymentTransactionRequest> payments;
    private String vatNumber;
    private Double revertAmount;
    private Double refundAmount;
    private Double shippingFee;
    private Double companyShippingFee;
    private Double collaboratorShippingFee;
    private Double vat;
    private Long returnOrderId;
    private List<PaymentTransactionRequest> exchangePayments;


    public Double getVat() {
        return vat;
    }
    public void setVat(Double vat) {
        this.vat = vat;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getOriginOrderCode() {
        return originOrderCode;
    }

    public void setOriginOrderCode(String originOrderCode) {
        this.originOrderCode = originOrderCode;
    }

    public String getExchangeCartCode() {
        return exchangeCartCode;
    }

    public void setExchangeCartCode(String exchangeCartCode) {
        this.exchangeCartCode = exchangeCartCode;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isExchange() {
        return isExchange;
    }

    public void setExchange(boolean exchange) {
        isExchange = exchange;
    }

    public Double getTotalTax() {
        return totalTax;
    }

    public void setTotalTax(Double totalTax) {
        this.totalTax = totalTax;
    }

    public List<ReturnOrderEntryRequest> getReturnOrderEntries() {
        return returnOrderEntries;
    }

    public void setReturnOrderEntries(List<ReturnOrderEntryRequest> returnOrderEntries) {
        this.returnOrderEntries = returnOrderEntries;
    }

    public List<PaymentTransactionRequest> getPayments() {
        return payments;
    }

    public void setPayments(List<PaymentTransactionRequest> payments) {
        this.payments = payments;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
    }

    public Double getRevertAmount() {
        return revertAmount;
    }

    public void setRevertAmount(Double revertAmount) {
        this.revertAmount = revertAmount;
    }

    public Double getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(Double refundAmount) {
        this.refundAmount = refundAmount;
    }

    public Long getReturnOrderId() {
        return returnOrderId;
    }

    public void setReturnOrderId(Long returnOrderId) {
        this.returnOrderId = returnOrderId;
    }

    public String getExchangeLoyaltyCard() {
        return exchangeLoyaltyCard;
    }

    public void setExchangeLoyaltyCard(String exchangeLoyaltyCard) {
        this.exchangeLoyaltyCard = exchangeLoyaltyCard;
    }

    public List<PaymentTransactionRequest> getExchangePayments() {
        return exchangePayments;
    }

    public void setExchangePayments(List<PaymentTransactionRequest> exchangePayments) {
        this.exchangePayments = exchangePayments;
    }

    public Double getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(Double shippingFee) {
        this.shippingFee = shippingFee;
    }

    public Double getCompanyShippingFee() {
        return companyShippingFee;
    }

    public void setCompanyShippingFee(Double companyShippingFee) {
        this.companyShippingFee = companyShippingFee;
    }

    public Double getCollaboratorShippingFee() {
        return collaboratorShippingFee;
    }

    public void setCollaboratorShippingFee(Double collaboratorShippingFee) {
        this.collaboratorShippingFee = collaboratorShippingFee;
    }
}
