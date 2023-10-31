package com.vctek.orderservice.promotionengine.ruleengineservice.rao;

import java.io.Serializable;

public class ProductConsumedRAO implements Serializable {
    private OrderEntryRAO orderEntry;
    private int availableQuantity;

    public OrderEntryRAO getOrderEntry() {
        return orderEntry;
    }

    public void setOrderEntry(OrderEntryRAO orderEntry) {
        this.orderEntry = orderEntry;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    @Override
    public boolean equals(final Object o) {

        if (o == null) return false;
        if (this.getClass() != o.getClass()) return false;
        if (o == this) return true;

        final ProductConsumedRAO other = (ProductConsumedRAO) o;
        return new org.apache.commons.lang.builder.EqualsBuilder()
                .append(getOrderEntry(), other.getOrderEntry())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new org.apache.commons.lang.builder.HashCodeBuilder()
                .append(getOrderEntry())
                .toHashCode();
    }

}
