package com.vctek.orderservice.promotionengine.ruleengineservice.rao;

import com.vctek.orderservice.promotionengine.ruleengineservice.enums.OrderEntrySelectionStrategy;

import java.io.Serializable;
import java.util.List;

public class EntriesSelectionStrategyRPD implements Serializable {
    private List<OrderEntryRAO> orderEntries;
    private OrderEntrySelectionStrategy selectionStrategy;
    private int quantity;
    private boolean targetOfAction;

    public void setOrderEntries(final List<OrderEntryRAO> orderEntries) {
        this.orderEntries = orderEntries;
    }


    public List<OrderEntryRAO> getOrderEntries() {
        return orderEntries;
    }


    public void setSelectionStrategy(final OrderEntrySelectionStrategy selectionStrategy) {
        this.selectionStrategy = selectionStrategy;
    }


    public OrderEntrySelectionStrategy getSelectionStrategy() {
        return selectionStrategy;
    }


    public void setQuantity(final int quantity) {
        this.quantity = quantity;
    }


    public int getQuantity() {
        return quantity;
    }


    public void setTargetOfAction(final boolean targetOfAction) {
        this.targetOfAction = targetOfAction;
    }


    public boolean isTargetOfAction() {
        return targetOfAction;
    }


    @Override
    public boolean equals(final Object o) {

        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof EntriesSelectionStrategyRPD)) return false;
        final EntriesSelectionStrategyRPD other = (EntriesSelectionStrategyRPD) o;
        return new org.apache.commons.lang.builder.EqualsBuilder()
                .append(getOrderEntries(), other.getOrderEntries())
                .append(getSelectionStrategy(), other.getSelectionStrategy())
                .append(getQuantity(), other.getQuantity())
                .append(isTargetOfAction(), other.isTargetOfAction())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new org.apache.commons.lang.builder.HashCodeBuilder()
                .append(getOrderEntries())
                .append(getSelectionStrategy())
                .append(getQuantity())
                .append(isTargetOfAction())
                .toHashCode();
    }
}
