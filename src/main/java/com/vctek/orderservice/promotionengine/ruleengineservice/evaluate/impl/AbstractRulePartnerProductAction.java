package com.vctek.orderservice.promotionengine.ruleengineservice.evaluate.impl;

import com.vctek.orderservice.promotionengine.ruleengine.model.eveluation.RuleActionContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.OrderEntrySelectionStrategy;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.EntriesSelectionStrategyRPD;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.OrderEntryRAO;

import java.util.*;

public class AbstractRulePartnerProductAction extends AbstractRuleExecutableSupport {
    protected List<EntriesSelectionStrategyRPD> createSelectionStrategyRPDsQualifyingProducts(RuleActionContext context,
                                                                  OrderEntrySelectionStrategy selectionStrategy,
                                                                  Map<String, Integer> qualifyingProductsContainers) {
        List<EntriesSelectionStrategyRPD> entriesSelectionStrategyRPDs = new ArrayList<>();
        if (Objects.nonNull(qualifyingProductsContainers)) {
            Iterator var6 = qualifyingProductsContainers.entrySet().iterator();

            while(var6.hasNext()) {
                Map.Entry<String, Integer> entry = (Map.Entry)var6.next();
                Set<OrderEntryRAO> orderEntries = this.getOrderEntries(context, entry);
                entriesSelectionStrategyRPDs.add(this.createSelectionStrategyRPD(selectionStrategy, entry.getValue(),
                        orderEntries, false));
            }
        }

        return entriesSelectionStrategyRPDs;
    }

    protected List<EntriesSelectionStrategyRPD> createSelectionStrategyRPDsTargetProducts(RuleActionContext context,
                                                                      OrderEntrySelectionStrategy selectionStrategy,
                                                                      Map<String, Integer> targetProductsContainers) {
        List<EntriesSelectionStrategyRPD> entriesSelectionStrategyRPDs = new ArrayList<>();
        if (targetProductsContainers != null) {
            Iterator var6 = targetProductsContainers.entrySet().iterator();

            while(var6.hasNext()) {
                Map.Entry<String, Integer> entry = (Map.Entry)var6.next();
                Set<OrderEntryRAO> orderEntries = this.getOrderEntries(context, entry);
                entriesSelectionStrategyRPDs.add(this.createSelectionStrategyRPD(selectionStrategy, entry.getValue(),
                        orderEntries, true));
            }
        }

        return entriesSelectionStrategyRPDs;
    }

    protected Set<OrderEntryRAO> getOrderEntries(RuleActionContext context, Map.Entry<String, Integer> entry) {
        String conditionsContainer = entry.getKey();
        return context.getValues(OrderEntryRAO.class, new String[]{conditionsContainer});
    }

    protected EntriesSelectionStrategyRPD createSelectionStrategyRPD(OrderEntrySelectionStrategy selectionStrategy,
                                                                     Integer quantity, Set<OrderEntryRAO> orderEntries,
                                                                     boolean isAction) {
        EntriesSelectionStrategyRPD selectionStrategyRPD = new EntriesSelectionStrategyRPD();
        selectionStrategyRPD.setSelectionStrategy(selectionStrategy);
        selectionStrategyRPD.setOrderEntries(new ArrayList(orderEntries));
        selectionStrategyRPD.setQuantity(quantity);
        selectionStrategyRPD.setTargetOfAction(isAction);
        return selectionStrategyRPD;
    }
}
