package com.vctek.orderservice.promotionengine.ruleengineservice.rao;

import java.io.Serializable;

public class CategoryRAO implements Serializable {
    private Long code;

    public void setCode(final Long code) {
        this.code = code;
    }

    public Long getCode() {
        return code;
    }


    @Override
    public boolean equals(final Object o) {

        if (o == null) return false;
        if (this.getClass() != o.getClass()) return false;
        if (o == this) return true;

        final CategoryRAO other = (CategoryRAO) o;
        return new org.apache.commons.lang.builder.EqualsBuilder()
                .append(getCode(), other.getCode())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new org.apache.commons.lang.builder.HashCodeBuilder()
                .append(getCode())
                .toHashCode();
    }
}
