package com.vctek.orderservice.model;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "payment_transaction")
public class PaymentTransactionModel extends AuditModel {
    @Column(name = "note")
    private String note;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "money_source_id")
    private Long moneySourceId;

    @Column(name = "money_source_type")
    private String moneySourceType;

    @Column(name = "transaction_number")
    private String transactionNumber;

    @Column(name = "payment_method_id")
    private Long paymentMethodId;

    @Column(name = "order_code")
    private String orderCode;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    @Column(name = "transaction_date")
    private Date transactionDate;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "order_id")
    private OrderModel orderModel;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "return_order_id")
    private ReturnOrderModel returnOrder;

    @Column(name = "invoice_id")
    private Long invoiceId;

    @Column(name = "conversion_rate")
    private Double conversionRate;

    @Column(name = "deleted")
    private boolean deleted;

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

    public OrderModel getOrderModel() {
        return orderModel;
    }

    public void setOrderModel(OrderModel orderModel) {
        this.orderModel = orderModel;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getMoneySourceType() {
        return moneySourceType;
    }

    public void setMoneySourceType(String moneySourceType) {
        this.moneySourceType = moneySourceType;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PaymentTransactionModel)) return false;
        PaymentTransactionModel that = (PaymentTransactionModel) o;
        if(this.getId() == null && that.getId() == null) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getId());
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public ReturnOrderModel getReturnOrder() {
        return returnOrder;
    }

    public void setReturnOrder(ReturnOrderModel returnOrder) {
        this.returnOrder = returnOrder;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public Double getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(Double conversionRate) {
        this.conversionRate = conversionRate;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
