package com.vctek.orderservice.promotionengine.promotionservice.model;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class RuleBasedOrderAddProductActionModel extends AbstractRuleBasedPromotionActionModel {
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "quantity")
    private Integer quantity;

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
}
