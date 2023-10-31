package com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain;

public class NumberedLineItem extends LineItem {
    private Integer entryNumber;

    public NumberedLineItem(Money basePrice) {
        super(basePrice);
    }

    public NumberedLineItem(Money basePrice, int numberOfUnits) {
        super(basePrice, numberOfUnits);
    }

    public Integer getEntryNumber() {
        return this.entryNumber;
    }

    public void setEntryNumber(Integer entryNumber) {
        this.entryNumber = entryNumber;
    }
}