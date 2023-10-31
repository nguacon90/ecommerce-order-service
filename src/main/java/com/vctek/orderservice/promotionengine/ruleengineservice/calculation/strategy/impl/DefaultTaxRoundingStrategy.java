package com.vctek.orderservice.promotionengine.ruleengineservice.calculation.strategy.impl;

import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain.Currency;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain.Money;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component("defaultTaxRoundingStrategy")
public class DefaultTaxRoundingStrategy extends DefaultRoundingStrategy {

    protected Money createMoney(BigDecimal amount, Currency curr) {
        return new Money(amount.setScale(curr.getDigits(), RoundingMode.DOWN), curr);
    }
}
