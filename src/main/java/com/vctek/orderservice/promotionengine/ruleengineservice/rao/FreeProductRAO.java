package com.vctek.orderservice.promotionengine.ruleengineservice.rao;

public class FreeProductRAO extends AbstractRuleActionRAO {

    private OrderEntryRAO addedOrderEntry;

    public OrderEntryRAO getAddedOrderEntry() {
        return addedOrderEntry;
    }

    public void setAddedOrderEntry(OrderEntryRAO addedOrderEntry) {
        this.addedOrderEntry = addedOrderEntry;
    }
}
