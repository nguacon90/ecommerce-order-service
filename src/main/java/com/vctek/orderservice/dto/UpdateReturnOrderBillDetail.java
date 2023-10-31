package com.vctek.orderservice.dto;

public class UpdateReturnOrderBillDetail {
    private Long orderEntryId;
    private Long productId;
    private Integer quantity;
    private Integer originQuantity;
    private boolean deleted;

    public Long getOrderEntryId() {
        return orderEntryId;
    }

    public void setOrderEntryId(Long orderEntryId) {
        this.orderEntryId = orderEntryId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getOriginQuantity() {
        return originQuantity;
    }

    public void setOriginQuantity(Integer originQuantity) {
        this.originQuantity = originQuantity;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
