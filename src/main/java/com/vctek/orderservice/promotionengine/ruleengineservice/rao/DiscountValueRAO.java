package com.vctek.orderservice.promotionengine.ruleengineservice.rao;

import java.io.Serializable;
import java.math.BigDecimal;

public class DiscountValueRAO implements Serializable {
    private BigDecimal value;
    private String discountType;

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    @Override
    public boolean equals(final Object o) {

        if (o == null) return false;
        if (this.getClass() != o.getClass()) return false;
        if (o == this) return true;

        final DiscountValueRAO other = (DiscountValueRAO) o;
        return new org.apache.commons.lang.builder.EqualsBuilder()
                .append(getValue(), other.getValue())
                .append(getDiscountType(), other.getDiscountType())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new org.apache.commons.lang.builder.HashCodeBuilder()
                .append(getValue())
                .append(getDiscountType())
                .toHashCode();
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }
}
