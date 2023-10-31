package com.vctek.orderservice.promotionengine.ruleengineservice.enums;

public enum OrderEntrySelectionStrategy {
    DEFAULT("DEFAULT"),
    CHEAPEST("CHEAPEST");
    private String code;

    OrderEntrySelectionStrategy(String code) {
        this.code = code;
    }

    public String code() {
        return this.code;
    }
}
