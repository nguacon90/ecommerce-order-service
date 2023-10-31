package com.vctek.orderservice.promotionengine.ruleengineservice.rao;

import java.util.Set;

public class ProductRAO extends AbstractActionedRAO {
    private Long id;
    private SupplierRAO supplier;
    private Set<CategoryRAO> categories;

    private String dtype;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<CategoryRAO> getCategories() {
        return categories;
    }

    public void setCategories(Set<CategoryRAO> categories) {
        this.categories = categories;
    }


    @Override
    public boolean equals(final Object o) {

        if (o == null) return false;
        if (this.getClass() != o.getClass()) return false;
        if (o == this) return true;

        final ProductRAO other = (ProductRAO) o;
        return new org.apache.commons.lang.builder.EqualsBuilder()
                .append(getId(), other.getId())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new org.apache.commons.lang.builder.HashCodeBuilder()
                .append(getId())
                .toHashCode();
    }

    public SupplierRAO getSupplier() {
        return supplier;
    }

    public void setSupplier(SupplierRAO supplier) {
        this.supplier = supplier;
    }

    public String getDtype() {
        return dtype;
    }

    public void setDtype(String dtype) {
        this.dtype = dtype;
    }
}
