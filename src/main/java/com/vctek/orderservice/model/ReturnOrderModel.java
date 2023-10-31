package com.vctek.orderservice.model;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "return_order")
@EntityListeners({AuditingEntityListener.class})
public class ReturnOrderModel extends ItemModel {
    @Column(name = "note")
    private String note;

    @Column(name = "billId")
    private Long billId;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "created_time")
    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTime;

    @Column(name = "modified_time")
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedTime;

    @CreatedBy
    @Column(name = "created_by")
    private Long createdBy;

    @LastModifiedBy
    @Column(name = "modified_by")
    private Long modifiedBy;

    @Column(name = "exchange_order_code")
    private String exchangeOrderCode;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "origin_order_id")
    private OrderModel originOrder;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "exchange_order_id")
    private OrderModel exchangeOrder;

    @OneToMany(mappedBy = "returnOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PaymentTransactionModel> paymentTransactions;

    @Column(name = "refund_amount")
    private Double refundAmount;

    @Column(name = "revert_amount")
    private Double revertAmount;

    @Column(name = "conversion_rate")
    private Double conversionRate;

    @Column(name = "compensate_revert")
    private Double compensateRevert;

    @Column(name = "redeem_amount")
    private  Double redeemAmount;

    @Column(name = "shipping_fee")
    private  Double shippingFee;

    @Column(name = "company_shipping_fee")
    private  Double companyShippingFee;

    @Column(name = "collaborator_shipping_fee")
    private  Double collaboratorShippingFee;

    @Column(name = "export_external_id")
    private  Long exportExternalId;

    @Column(name = "external_id")
    private Long externalId;

    @Column(name = "vat")
    private Double vat;

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(Long modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public OrderModel getOriginOrder() {
        return originOrder;
    }

    public void setOriginOrder(OrderModel originOrder) {
        this.originOrder = originOrder;
    }

    public OrderModel getExchangeOrder() {
        return exchangeOrder;
    }

    public void setExchangeOrder(OrderModel exchangeOrder) {
        this.exchangeOrder = exchangeOrder;
    }

    public Set<PaymentTransactionModel> getPaymentTransactions() {
        return paymentTransactions;
    }

    public void setPaymentTransactions(Set<PaymentTransactionModel> paymentTransactions) {
        this.paymentTransactions = paymentTransactions;
    }

    public String getExchangeOrderCode() {
        return exchangeOrderCode;
    }

    public void setExchangeOrderCode(String exchangeOrderCode) {
        this.exchangeOrderCode = exchangeOrderCode;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Double getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(Double refundAmount) {
        this.refundAmount = refundAmount;
    }

    public Double getRevertAmount() {
        return revertAmount;
    }

    public void setRevertAmount(Double revertAmount) {
        this.revertAmount = revertAmount;
    }

    public Double getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(Double conversionRate) {
        this.conversionRate = conversionRate;
    }

    public Double getCompensateRevert() {
        return compensateRevert;
    }

    public void setCompensateRevert(Double compensateRevert) {
        this.compensateRevert = compensateRevert;
    }

    public Double getRedeemAmount() {
        return redeemAmount ;
    }

    public void setRedeemAmount(Double redeemAmount) {
        this.redeemAmount = redeemAmount;
    }

    public Long getExportExternalId() {
        return exportExternalId;
    }

    public void setExportExternalId(Long exportExternalId) {
        this.exportExternalId = exportExternalId;
    }

    public Long getExternalId() {
        return externalId;
    }

    public void setExternalId(Long externalId) {
        this.externalId = externalId;
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

    public Double getVat() {
        return vat;
    }

    public void setVat(Double vat) {
        this.vat = vat;
    }
}
