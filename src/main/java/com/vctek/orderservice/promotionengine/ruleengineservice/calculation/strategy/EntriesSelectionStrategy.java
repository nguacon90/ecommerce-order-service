package com.vctek.orderservice.promotionengine.ruleengineservice.calculation.strategy;

import com.vctek.orderservice.promotionengine.ruleengineservice.rao.EntriesSelectionStrategyRPD;

import java.util.Map;

public interface EntriesSelectionStrategy {
    Map<Integer, Integer> pickup(EntriesSelectionStrategyRPD strategy);

    Map<Integer, Integer> pickup(EntriesSelectionStrategyRPD strategy, Map<Integer, Integer> consumableQuantities);
}
