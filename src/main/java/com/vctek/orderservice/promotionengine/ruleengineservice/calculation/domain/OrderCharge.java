package com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain;

import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.MissingCalculationDataException;

public class OrderCharge extends AbstractCharge implements Taxable {
    public OrderCharge(AbstractAmount amount, ChargeType chargeType) {
        super(amount);
        super.setChargeType(chargeType);
    }

    public OrderCharge(AbstractAmount amount) {
        super(amount);
    }

    public Money getTotal(Order context) {
        if (context == null) {
            throw new MissingCalculationDataException("Missing order context");
        } else if (!context.getCharges().contains(this)) {
            throw new IllegalArgumentException("Charge " + this + " doesnt belong to order " + context + " - cannot calculate total for it.");
        } else {
            return context.getTotalCharges().get(this);
        }
    }

    public String toString() {
        return this.getAmount().toString() + " dontCharge:" + this.isDisabled() + (this.getChargeType() == null ? "" : " type:" + this.getChargeType());
    }
}