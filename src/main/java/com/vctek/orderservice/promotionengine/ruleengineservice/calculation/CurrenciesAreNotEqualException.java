package com.vctek.orderservice.promotionengine.ruleengineservice.calculation;

public class CurrenciesAreNotEqualException extends RuntimeException {
    public CurrenciesAreNotEqualException(String message) {
        super(message);
    }
}
