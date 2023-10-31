package com.vctek.orderservice.promotionengine.ruleengineservice.calculation.strategy;

import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain.Currency;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain.Money;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain.Percentage;

import java.math.BigDecimal;

public interface RoundingStrategy {
    Money divide(Money money, BigDecimal value);

    Money multiply(Money money, BigDecimal value);

    Money getPercentValue(Money money, Percentage percentage);

    Money roundToMoney(BigDecimal money, Currency currency);
}
