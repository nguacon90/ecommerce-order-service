package com.vctek.orderservice.elasticsearch.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.io.Serializable;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentTransactionData implements Serializable {
    private Double amount;
    private Long moneySourceId;
    private String type;
    private Long paymentMethodId;
    private Long orderId;
    private String moneySourceType;
    private Long returnOrderId;
    private Long invoiceId;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getMoneySourceType() {
        return moneySourceType;
    }

    public void setMoneySourceType(String moneySourceType) {
        this.moneySourceType = moneySourceType;
    }

    public Long getReturnOrderId() {
        return returnOrderId;
    }

    public void setReturnOrderId(Long returnOrderId) {
        this.returnOrderId = returnOrderId;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        PaymentTransactionData that = (PaymentTransactionData) o;

        return new EqualsBuilder()
                .append(invoiceId, that.invoiceId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return Objects.hash(invoiceId);
    }
}
