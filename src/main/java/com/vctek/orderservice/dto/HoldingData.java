package com.vctek.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HoldingData {
    private Long companyId;
    private Long productId;
    private Long quantity;
    private boolean isPreOrder;
    private boolean isHolding;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public boolean isPreOrder() {
        return isPreOrder;
    }

    public void setPreOrder(boolean preOrder) {
        isPreOrder = preOrder;
    }

    public boolean isHolding() {
        return isHolding;
    }

    public void setHolding(boolean holding) {
        isHolding = holding;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }
}
