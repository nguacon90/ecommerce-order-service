package com.vctek.orderservice.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vctek.orderservice.util.DateUtil;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentTransactionRequest {
    private Long id;
    private String note;
    private Double amount;
    private Long moneySourceId;
    private String type;
    private Long paymentMethodId;
    private Long orderId;
    private Long companyId;
    private String transactionNumber;
    private Long warehouseId;

    @DateTimeFormat(pattern = DateUtil.ISO_DATE_TIME_PATTERN)
    private Date transactionDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Long getMoneySourceId() {
        return moneySourceId;
    }

    public void setMoneySourceId(Long moneySourceId) {
        this.moneySourceId = moneySourceId;
    }

    public Long getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(Long paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }
}
