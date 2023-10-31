package com.vctek.orderservice.model;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class ProductLoyaltyRewardRateModel extends LoyaltyRewardRateModel {
    @Column(name = "product_id")
    private Long productId;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }
}
