package com.vctek.orderservice.dto;

import com.vctek.orderservice.model.ToppingOptionModel;

import java.util.Set;

public class ToppingOptionModification {
    private ToppingOptionModel toppingOptionModel;
    private Long modifiedToppingOptionId;
    private boolean isDeleted;
    private Set<Long> productIds;
    private int quantityAdd;

    public Set<Long> getProductIds() {
        return productIds;
    }

    public void setProductIds(Set<Long> productIds) {
        this.productIds = productIds;
    }

    public Long getModifiedToppingOptionId() {
        return modifiedToppingOptionId;
    }

    public void setModifiedToppingOptionId(Long modifiedToppingOptionId) {
        this.modifiedToppingOptionId = modifiedToppingOptionId;
    }

    public ToppingOptionModel getToppingOptionModel() {
        return toppingOptionModel;
    }

    public void setToppingOptionModel(ToppingOptionModel toppingOptionModel) {
        this.toppingOptionModel = toppingOptionModel;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public int getQuantityAdd() {
        return quantityAdd;
    }

    public void setQuantityAdd(int quantityAdd) {
        this.quantityAdd = quantityAdd;
    }
}
