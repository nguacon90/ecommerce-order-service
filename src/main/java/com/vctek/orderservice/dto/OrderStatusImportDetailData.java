package com.vctek.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderStatusImportDetailData {
    private Long id;
    private String orderCode;
    private String oldOrderStatus;
    private String newOrderStatus;
    private String status;
    private String note;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getOldOrderStatus() {
        return oldOrderStatus;
    }

    public void setOldOrderStatus(String oldOrderStatus) {
        this.oldOrderStatus = oldOrderStatus;
    }

    public String getNewOrderStatus() {
        return newOrderStatus;
    }

    public void setNewOrderStatus(String newOrderStatus) {
        this.newOrderStatus = newOrderStatus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
