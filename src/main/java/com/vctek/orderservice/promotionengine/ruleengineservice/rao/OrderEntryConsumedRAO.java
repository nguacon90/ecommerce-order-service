package com.vctek.orderservice.promotionengine.ruleengineservice.rao;

import java.io.Serializable;
import java.math.BigDecimal;

public class OrderEntryConsumedRAO implements Serializable {
    private String firedRuleCode;
    private OrderEntryRAO orderEntry;
    private int quantity;
    private BigDecimal adjustedUnitPrice;


    public void setFiredRuleCode(final String firedRuleCode) {
        this.firedRuleCode = firedRuleCode;
    }


    public String getFiredRuleCode() {
        return firedRuleCode;
    }


    public void setOrderEntry(final OrderEntryRAO orderEntry) {
        this.orderEntry = orderEntry;
    }


    public OrderEntryRAO getOrderEntry() {
        return orderEntry;
    }


    public void setQuantity(final int quantity) {
        this.quantity = quantity;
    }


    public int getQuantity() {
        return quantity;
    }


    public void setAdjustedUnitPrice(final BigDecimal adjustedUnitPrice) {
        this.adjustedUnitPrice = adjustedUnitPrice;
    }


    public BigDecimal getAdjustedUnitPrice() {
        return adjustedUnitPrice;
    }


    @Override
    public boolean equals(final Object o) {

        if (o == null) return false;
        if (this.getClass() != o.getClass()) return false;
        if (o == this) return true;
        final OrderEntryConsumedRAO other = (OrderEntryConsumedRAO) o;
        return new org.apache.commons.lang.builder.EqualsBuilder()
                .append(getFiredRuleCode(), other.getFiredRuleCode())
                .append(getOrderEntry(), other.getOrderEntry())
                .append(getQuantity(), other.getQuantity())
                .append(getAdjustedUnitPrice(), other.getAdjustedUnitPrice())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new org.apache.commons.lang.builder.HashCodeBuilder()
                .append(getFiredRuleCode())
                .append(getOrderEntry())
                .append(getQuantity())
                .append(getAdjustedUnitPrice())
                .toHashCode();
    }
}
