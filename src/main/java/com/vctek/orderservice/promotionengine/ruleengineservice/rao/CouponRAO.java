package com.vctek.orderservice.promotionengine.ruleengineservice.rao;

import java.io.Serializable;

public class CouponRAO implements Serializable {
    private static final long serialVersionUID = 5295851768395165623L;
    private Long couponId;
    private String couponCode;
    private Integer totalRedemption;

    @Override
    public boolean equals(final Object o) {

        if (o == null) return false;
        if (o == this) return true;
        if (this.getClass() != o.getClass()) return false;
        final CouponRAO other = (CouponRAO) o;
        return new org.apache.commons.lang.builder.EqualsBuilder()
                .append(getCouponId(), other.getCouponId())
                .append(getCouponCode(), other.getCouponCode())
                .append(getTotalRedemption(), other.getTotalRedemption())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new org.apache.commons.lang.builder.HashCodeBuilder()
                .append(getCouponId())
                .append(getCouponCode())
                .append(getTotalRedemption())
                .toHashCode();
    }

    public Long getCouponId() {
        return couponId;
    }

    public void setCouponId(Long couponId) {
        this.couponId = couponId;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public Integer getTotalRedemption() {
        return totalRedemption;
    }

    public void setTotalRedemption(Integer totalRedemption) {
        this.totalRedemption = totalRedemption;
    }
}
