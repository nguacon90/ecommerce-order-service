package com.vctek.orderservice.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AddSubOrderEntryRequest {
    private Long productId;
    private Long comboId;
    private Long companyId;
    private String orderCode;
    private Long entryId;
    private Integer comboGroupNumber;
    private String orderType;
    private Integer quantity = 1;
    private boolean updateQuantity = false;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getComboId() {
        return comboId;
    }

    public void setComboId(Long comboId) {
        this.comboId = comboId;
    }

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

    public Integer getComboGroupNumber() {
        return comboGroupNumber;
    }

    public void setComboGroupNumber(Integer comboGroupNumber) {
        this.comboGroupNumber = comboGroupNumber;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public Long getEntryId() {
        return entryId;
    }

    public void setEntryId(Long entryId) {
        this.entryId = entryId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public boolean isUpdateQuantity() {
        return updateQuantity;
    }

    public void setUpdateQuantity(boolean updateQuantity) {
        this.updateQuantity = updateQuantity;
    }
}
