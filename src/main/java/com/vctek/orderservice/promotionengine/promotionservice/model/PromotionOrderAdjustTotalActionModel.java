package com.vctek.orderservice.promotionengine.promotionservice.model;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class PromotionOrderAdjustTotalActionModel extends AbstractPromotionActionModel {

    @Column(name = "amount")
    private Double amount;

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

}
