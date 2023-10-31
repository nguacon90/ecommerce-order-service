package com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain;

public interface Taxable {
    Money getTotal(Order order);
}
