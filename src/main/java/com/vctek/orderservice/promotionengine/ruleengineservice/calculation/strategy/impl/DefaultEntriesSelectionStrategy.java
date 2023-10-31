package com.vctek.orderservice.promotionengine.ruleengineservice.calculation.strategy.impl;

import com.google.common.collect.Maps;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.strategy.EntriesSelectionStrategy;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.EntriesSelectionStrategyRPD;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.OrderEntryRAO;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("defaultEntriesSelectionStrategy")
public class DefaultEntriesSelectionStrategy implements EntriesSelectionStrategy {

    @Override
    public Map<Integer, Integer> pickup(EntriesSelectionStrategyRPD strategy) {
        Map<Integer, Integer> result = Maps.newHashMap();
        int itemsToConsume = strategy.getQuantity();
        OrderEntryRAO orderEntry;
        int applicableUnits;
        for(Iterator var5 = this.getOrderEntriesToProcess(strategy).iterator(); var5.hasNext(); result.put(orderEntry.getEntryNumber(), applicableUnits)) {
            orderEntry = (OrderEntryRAO)var5.next();
            if (itemsToConsume <= 0) {
                break;
            }

            if (itemsToConsume >= orderEntry.getQuantity()) {
                applicableUnits = orderEntry.getQuantity();
                itemsToConsume -= orderEntry.getQuantity();
            } else {
                applicableUnits = itemsToConsume;
                itemsToConsume = 0;
            }
        }

        if (itemsToConsume > 0) {
            throw new IllegalArgumentException("The Order Entries have less units than required to pickup.");
        } else {
            return result;
        }
    }

    @Override
    public Map<Integer, Integer> pickup(EntriesSelectionStrategyRPD strategy, Map<Integer, Integer> consumableQuantities) {
        Map<Integer, Integer> result = new HashMap<>();
        int itemsToConsume = strategy.getQuantity();

        OrderEntryRAO orderEntry;
        int applicableUnits;
        for(Iterator var6 = this.getOrderEntriesToProcess(strategy).iterator(); var6.hasNext(); result.put(orderEntry.getEntryNumber(), applicableUnits)) {
            orderEntry = (OrderEntryRAO)var6.next();
            Integer consumableQuantity = consumableQuantities.get(orderEntry.getEntryNumber());
            if (Objects.isNull(consumableQuantity)) {
                consumableQuantity = orderEntry.getQuantity();
            }

            if (itemsToConsume <= 0) {
                break;
            }

            if (itemsToConsume >= consumableQuantity) {
                applicableUnits = consumableQuantity;
                itemsToConsume -= consumableQuantity;
            } else {
                applicableUnits = itemsToConsume;
                itemsToConsume = 0;
            }
        }

        if (itemsToConsume > 0) {
            throw new IllegalArgumentException("The Order Entries have less units than required to pickup.");
        } else {
            return result;
        }
    }

    protected List<OrderEntryRAO> getOrderEntriesToProcess(EntriesSelectionStrategyRPD strategy) {
        return new ArrayList(strategy.getOrderEntries());
    }
}
