package com.vctek.orderservice.promotionengine.promotionservice.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.math.BigDecimal;

@Entity
@DiscriminatorValue("RuleBasedOrderAdjustTotalActionModel")
public class RuleBasedOrderAdjustTotalActionModel extends AbstractRuleBasedPromotionActionModel {
    @Column(name = "amount")
    private BigDecimal amount;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
