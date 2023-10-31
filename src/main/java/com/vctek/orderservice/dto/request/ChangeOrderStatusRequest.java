package com.vctek.orderservice.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChangeOrderStatusRequest {
    private Long companyId;
    private String orderCode;
    private String orderStatus;
    private String cancelText;
    private boolean confirmDiscount;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getCancelText() {
        return cancelText;
    }

    public void setCancelText(String cancelText) {
        this.cancelText = cancelText;
    }

    public boolean isConfirmDiscount() {
        return confirmDiscount;
    }

    public void setConfirmDiscount(boolean confirmDiscount) {
        this.confirmDiscount = confirmDiscount;
    }
}
