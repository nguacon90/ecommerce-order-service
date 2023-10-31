package com.vctek.orderservice.promotionengine.promotionservice.model;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class RuleBasedFixedPriceProductActionModel extends AbstractRuleBasedPromotionActionModel {
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "order_entry_quantity")
    private Long orderEntryQuantity;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getOrderEntryQuantity() {
        return orderEntryQuantity;
    }

    public void setOrderEntryQuantity(Long orderEntryQuantity) {
        this.orderEntryQuantity = orderEntryQuantity;
    }
}
