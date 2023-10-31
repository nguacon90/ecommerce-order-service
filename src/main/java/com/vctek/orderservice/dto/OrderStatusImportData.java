package com.vctek.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vctek.orderservice.model.OrderStatusImportDetailModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderStatusImportData {
    private Long id;
    private Long companyId;
    private String orderStatus;
    private String status;
    private Date createdTime;
    private Long createdBy;
    private List<OrderStatusImportDetailData> details = new ArrayList<>();

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

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public List<OrderStatusImportDetailData> getDetails() {
        return details;
    }

    public void setDetails(List<OrderStatusImportDetailData> details) {
        this.details = details;
    }
}
