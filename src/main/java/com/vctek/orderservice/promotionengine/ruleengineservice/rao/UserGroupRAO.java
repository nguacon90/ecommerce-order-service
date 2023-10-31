package com.vctek.orderservice.promotionengine.ruleengineservice.rao;

import java.io.Serializable;

public class UserGroupRAO implements Serializable {
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(final Object o) {

        if (o == null) return false;
        if (this.getClass() != o.getClass()) return false;
        if (o == this) return true;

        final UserGroupRAO other = (UserGroupRAO) o;
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
}
