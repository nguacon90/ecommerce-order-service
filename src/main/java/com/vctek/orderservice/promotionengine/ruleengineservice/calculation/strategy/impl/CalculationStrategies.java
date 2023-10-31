package com.vctek.orderservice.promotionengine.ruleengineservice.calculation.strategy.impl;

import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.strategy.RoundingStrategy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class CalculationStrategies {
    private RoundingStrategy roundingStrategy;
    private RoundingStrategy taxRoundingStrategry;

    public CalculationStrategies(@Qualifier("defaultRoundingStrategy") RoundingStrategy roundingStrategy,
                                 @Qualifier("defaultTaxRoundingStrategy") RoundingStrategy taxRoundingStrategry) {
        this.roundingStrategy = roundingStrategy;
        this.taxRoundingStrategry = taxRoundingStrategry;
    }

    public RoundingStrategy getRoundingStrategy() {
        return roundingStrategy;
    }

    public RoundingStrategy getTaxRoundingStrategry() {
        return taxRoundingStrategry;
    }
}
