package com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain;

public class OrderDiscount extends AbstractDiscount {
    public OrderDiscount(AbstractAmount amount) {
        super(amount);
    }

    public String toString() {
        return this.getAmount().toString();
    }
}
