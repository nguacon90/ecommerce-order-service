package com.vctek.orderservice.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vctek.orderservice.util.DateUtil;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReturnOrderSearchRequest implements Serializable {
    private Long id;
    private Long companyId;
    private String product;
    private String customer;
    private String sortOrder;
    private String sortField;
    @DateTimeFormat(pattern = DateUtil.ISO_DATE_PATTERN)
    private Date fromCreatedTime;
    @DateTimeFormat(pattern = DateUtil.ISO_DATE_PATTERN)
    private Date toCreatedTime;
    private String orderTypes;
    private String paymentMethods;
    private Long exchangeWarehouseId;
    private Long returnWarehouseId;
    private Long originEmployeeId;
    private List<Long> originOrderSourceIds;

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

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public Date getFromCreatedTime() {
        return fromCreatedTime;
    }

    public void setFromCreatedTime(Date fromCreatedTime) {
        this.fromCreatedTime = fromCreatedTime;
    }

    public Date getToCreatedTime() {
        return toCreatedTime;
    }

    public void setToCreatedTime(Date toCreatedTime) {
        this.toCreatedTime = toCreatedTime;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public String getOrderTypes() {
        return orderTypes;
    }

    public void setOrderTypes(String orderTypes) {
        this.orderTypes = orderTypes;
    }

    public String getPaymentMethods() {
        return paymentMethods;
    }

    public void setPaymentMethods(String paymentMethods) {
        this.paymentMethods = paymentMethods;
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

    public Long getOriginEmployeeId() {
        return originEmployeeId;
    }

    public void setOriginEmployeeId(Long originEmployeeId) {
        this.originEmployeeId = originEmployeeId;
    }

    public List<Long> getOriginOrderSourceIds() {
        return originOrderSourceIds;
    }

    public void setOriginOrderSourceIds(List<Long> originOrderSourceIds) {
        this.originOrderSourceIds = originOrderSourceIds;
    }
}
