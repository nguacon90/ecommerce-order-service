package com.vctek.orderservice.elasticsearch.model.returnorder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vctek.orderservice.elasticsearch.model.ElasticItemModel;
import com.vctek.orderservice.elasticsearch.model.PaymentTransactionData;
import com.vctek.orderservice.feignclient.dto.BillRequest;
import com.vctek.orderservice.util.ElasticSearchIndex;
import org.mvel2.ast.DoUntilNode;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(indexName = ElasticSearchIndex.RETURN_ORDER_INDEX)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReturnOrderDocument extends ElasticItemModel {
    private Long companyId;
    private Long employeeId;
    private String employeeName;
    private Double amount;
    private String note;
    private Long exchangeWarehouseId;
    private Long returnWarehouseId;
    private Long originOrderSourceId;
    private String originOrderSourceName;
    private Double shippingFee;
    private Double companyShippingFee;
    private Double collaboratorShippingFee;
    private Double vat;



    @Field(type = FieldType.Nested)
    private OriginOrder originOrder;

    @Field(type = FieldType.Nested)
    private ReturnOrderBill bill;

    @Field(type = FieldType.Nested)
    private ExchangeOrder exchangeOrder;

    @Field(type = FieldType.Date)
    private Date creationTime;

    @Field(type = FieldType.Nested)
    private List<PaymentTransactionData> paymentTransactions = new ArrayList<>();

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Long getExchangeWarehouseId() {
        return exchangeWarehouseId;
    }

    public void setExchangeWarehouseId(Long exchangeWarehouseId) {
        this.exchangeWarehouseId = exchangeWarehouseId;
    }

    public Long getReturnWarehouseId() {
        return returnWarehouseId;
    }

    public void setReturnWarehouseId(Long returnWarehouseId) {
        this.returnWarehouseId = returnWarehouseId;
    }

    public OriginOrder getOriginOrder() {
        return originOrder;
    }

    public void setOriginOrder(OriginOrder originOrder) {
        this.originOrder = originOrder;
    }

    public ReturnOrderBill getBill() {
        return bill;
    }

    public void setBill(ReturnOrderBill bill) {
        this.bill = bill;
    }

    public ExchangeOrder getExchangeOrder() {
        return exchangeOrder;
    }

    public void setExchangeOrder(ExchangeOrder exchangeOrder) {
        this.exchangeOrder = exchangeOrder;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public List<PaymentTransactionData> getPaymentTransactions() {
        return paymentTransactions;
    }

    public void setPaymentTransactions(List<PaymentTransactionData> paymentTransactions) {
        this.paymentTransactions = paymentTransactions;
    }

    public Long getOriginOrderSourceId() {
        return originOrderSourceId;
    }

    public void setOriginOrderSourceId(Long originOrderSourceId) {
        this.originOrderSourceId = originOrderSourceId;
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

    public String getOriginOrderSourceName() {
        return originOrderSourceName;
    }

    public void setOriginOrderSourceName(String originOrderSourceName) {
        this.originOrderSourceName = originOrderSourceName;
    }

    public Double getVat() {
        return vat;
    }

    public void setVat(Double vat) {
        this.vat = vat;
    }

}
