package com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain;

public abstract class AbstractCharge {
    private ChargeType chargeType;
    private boolean disabled;
    private final AbstractAmount amount;

    public AbstractCharge(AbstractAmount amount) {
        if (amount == null) {
            throw new IllegalArgumentException("The amount was null");
        } else {
            this.amount = amount;
        }
    }

    public boolean isDisabled() {
        return this.disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public AbstractAmount getAmount() {
        return this.amount;
    }

    public void setChargeType(ChargeType chargeType) {
        this.chargeType = chargeType;
    }

    public ChargeType getChargeType() {
        return this.chargeType;
    }
}
