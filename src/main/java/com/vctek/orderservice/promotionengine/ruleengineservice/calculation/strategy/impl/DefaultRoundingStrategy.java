package com.vctek.orderservice.promotionengine.ruleengineservice.calculation.strategy.impl;

import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain.Currency;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain.Money;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain.Percentage;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.strategy.RoundingStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component("defaultRoundingStrategy")
public class DefaultRoundingStrategy implements RoundingStrategy {

    @Override
    public Money divide(Money price, BigDecimal divisor) {
        Currency curr = price.getCurrency();
        BigDecimal divide = price.getAmount().divide(divisor, RoundingMode.HALF_UP);
        return this.createMoney(divide, curr);
    }

    @Override
    public Money multiply(Money price, BigDecimal multiplicant) {
        Currency curr = price.getCurrency();
        BigDecimal multiply = price.getAmount().multiply(multiplicant);
        return this.createMoney(multiply, curr);
    }

    @Override
    public Money getPercentValue(Money price, Percentage percent) {
        Currency curr = price.getCurrency();
        BigDecimal amount = price.getAmount().multiply(percent.getRate()).divide(BigDecimal.valueOf(100L));
        return this.createMoney(amount, curr);
    }

    @Override
    public Money roundToMoney(BigDecimal amount, Currency currency) {
        return this.createMoney(amount, currency);
    }

    protected Money createMoney(BigDecimal amount, Currency curr) {
        return new Money(amount.setScale(curr.getDigits(), RoundingMode.HALF_UP), curr);
    }
}
