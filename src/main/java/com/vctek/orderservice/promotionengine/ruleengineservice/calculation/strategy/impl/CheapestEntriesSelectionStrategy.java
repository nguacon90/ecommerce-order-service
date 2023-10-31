package com.vctek.orderservice.promotionengine.ruleengineservice.calculation.strategy.impl;

import com.vctek.orderservice.promotionengine.ruleengineservice.rao.EntriesSelectionStrategyRPD;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.OrderEntryRAO;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component("cheapestEntriesSelectionStrategy")
public class CheapestEntriesSelectionStrategy extends DefaultEntriesSelectionStrategy {

    @Override
    protected List<OrderEntryRAO> getOrderEntriesToProcess(EntriesSelectionStrategyRPD strategy) {
        return strategy.getOrderEntries().stream()
                .sorted(Comparator.comparing(OrderEntryRAO::getBasePrice))
                .collect(Collectors.toList());
    }
}
