package com.vctek.orderservice.promotionengine.ruleengineservice.rao;

import java.io.Serializable;

public class SupplierRAO implements Serializable {
    private static final long serialVersionUID = -3211458241356793286L;
    private Long supplierId;

    @Override
    public boolean equals(final Object o) {

        if (o == null) return false;
        if (o == this) return true;
        if (this.getClass() != o.getClass()) return false;
        final SupplierRAO other = (SupplierRAO) o;
        return new org.apache.commons.lang.builder.EqualsBuilder()
                .append(getSupplierId(), other.getSupplierId())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new org.apache.commons.lang.builder.HashCodeBuilder()
                .append(getSupplierId())
                .toHashCode();
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }
}
