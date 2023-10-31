package com.vctek.orderservice.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "loyalty_transaction")
public class LoyaltyTransactionModel extends ItemModel {

    @Column(name = "order_code")
    private String orderCode;

    @Column(name = "invoice_number")
    private String invoiceNumber;

    @Column(name = "type")
    private String type;

    @Column(name = "conversion_rate")
    private Double conversionRate;

    @Column(name = "return_order_id")
    private  Long returnOrderId;

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(Double conversionRate) {
        this.conversionRate = conversionRate;
    }

    public Long getReturnOrderId() {
        return returnOrderId;
    }

    public void setReturnOrderId(Long returnOrderId) {
        this.returnOrderId = returnOrderId;
    }
}
