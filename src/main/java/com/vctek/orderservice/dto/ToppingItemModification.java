package com.vctek.orderservice.dto;

import com.vctek.orderservice.model.ToppingItemModel;

public class ToppingItemModification {
    private ToppingItemModel toppingItemModel;
    private Long toppingItemId;
    private Long productId;
    private Integer quantity;
    private Integer oldQuantity;
    private boolean isDeleted;

    public Long getToppingItemId() {
        return toppingItemId;
    }

    public void setToppingItemId(Long toppingItemId) {
        this.toppingItemId = toppingItemId;
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

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public ToppingItemModel getToppingItemModel() {
        return toppingItemModel;
    }

    public void setToppingItemModel(ToppingItemModel toppingItemModel) {
        this.toppingItemModel = toppingItemModel;
    }

    public Integer getOldQuantity() {
        return oldQuantity;
    }

    public void setOldQuantity(Integer oldQuantity) {
        this.oldQuantity = oldQuantity;
    }
}
