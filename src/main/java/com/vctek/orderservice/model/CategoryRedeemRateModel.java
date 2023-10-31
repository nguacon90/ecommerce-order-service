package com.vctek.orderservice.model;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class CategoryRedeemRateModel extends LoyaltyRedeemRateUseModel {
    @Column(name = "category_id")
    private Long categoryId;

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}
