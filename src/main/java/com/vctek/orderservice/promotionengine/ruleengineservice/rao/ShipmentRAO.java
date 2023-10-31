package com.vctek.orderservice.promotionengine.ruleengineservice.rao;

public class ShipmentRAO extends AbstractRuleActionRAO {
    private DeliveryModeRAO mode;

    public DeliveryModeRAO getMode() {
        return mode;
    }

    public void setMode(DeliveryModeRAO mode) {
        this.mode = mode;
    }
}
