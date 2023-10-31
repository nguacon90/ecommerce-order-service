package com.vctek.orderservice.dto;

import com.vctek.orderservice.feignclient.dto.ReturnOrderBillData;

import java.util.Date;

public class ReturnOrderData {
    private Long id;
    private String note;
    private Long billId;
    private Long customerId;
    private String originOrderCode;
    private String originOrderType;
    private OrderData exchangeOrder;
    private ReturnOrderBillData billData;
    private Double revertPoint;
    private Double refundPoint;
    private Double conversionRate;
    private Double compensateRevert;
    private Double pendingPoint;
    private Double availablePoint;
    private Double redeemPoint;
    private Double shippingFee;
    private Double companyShippingFee;
    private Double collaboratorShippingFee;
    private Long createdBy;
    private Date createdTime;
    private Double vat ;
    private String cardNumber ;

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

    public OrderData getExchangeOrder() {
        return exchangeOrder;
    }

    public void setExchangeOrder(OrderData exchangeOrder) {
        this.exchangeOrder = exchangeOrder;
    }

    public String getOriginOrderCode() {
        return originOrderCode;
    }

    public void setOriginOrderCode(String originOrderCode) {
        this.originOrderCode = originOrderCode;
    }

    public ReturnOrderBillData getBillData() {
        return billData;
    }

    public void setBillData(ReturnOrderBillData billData) {
        this.billData = billData;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getOriginOrderType() {
        return originOrderType;
    }

    public void setOriginOrderType(String originOrderType) {
        this.originOrderType = originOrderType;
    }

    public Double getRevertPoint() {
        return revertPoint;
    }

    public void setRevertPoint(Double revertPoint) {
        this.revertPoint = revertPoint;
    }

    public Double getRefundPoint() {
        return refundPoint;
    }

    public void setRefundPoint(Double refundPoint) {
        this.refundPoint = refundPoint;
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

    public Double getPendingPoint() {
        return pendingPoint;
    }

    public void setPendingPoint(Double pendingPoint) {
        this.pendingPoint = pendingPoint;
    }

    public Double getAvailablePoint() {
        return availablePoint;
    }

    public void setAvailablePoint(Double availablePoint) {
        this.availablePoint = availablePoint;
    }

    public Double getRedeemPoint() {
        return redeemPoint;
    }

    public void setRedeemPoint(Double redeemPoint) {
        this.redeemPoint = redeemPoint;
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

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
}
