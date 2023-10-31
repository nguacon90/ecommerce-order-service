package com.vctek.orderservice.promotionengine.ruleengineservice.rao;

import java.io.Serializable;
import java.util.Set;

public class UserRAO implements Serializable {
    private Long id;
    private Set<UserGroupRAO> groups;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<UserGroupRAO> getGroups() {
        return groups;
    }

    public void setGroups(Set<UserGroupRAO> groups) {
        this.groups = groups;
    }

    @Override
    public boolean equals(final Object o) {

        if (o == null) return false;
        if (this.getClass() != o.getClass()) return false;
        if (o == this) return true;

        final UserRAO other = (UserRAO) o;
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
