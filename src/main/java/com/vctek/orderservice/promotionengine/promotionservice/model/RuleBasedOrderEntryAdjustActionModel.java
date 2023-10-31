package com.vctek.orderservice.promotionengine.promotionservice.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.math.BigDecimal;

@Entity
public class RuleBasedOrderEntryAdjustActionModel extends AbstractRuleBasedPromotionActionModel {
    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "order_entry_number")
    private Integer orderEntryNumber;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "order_entry_quantity")
    private Long orderEntryQuantity;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getOrderEntryNumber() {
        return orderEntryNumber;
    }

    public void setOrderEntryNumber(Integer orderEntryNumber) {
        this.orderEntryNumber = orderEntryNumber;
    }

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
