package com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain;

public abstract class AbstractDiscount {
    private final AbstractAmount amount;

    protected AbstractDiscount(AbstractAmount amount) {
        if (amount == null) {
            throw new IllegalArgumentException("The amount was null");
        } else {
            this.amount = amount;
        }
    }

    public AbstractAmount getAmount() {
        return amount;
    }
}
