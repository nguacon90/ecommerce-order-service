package com.vctek.orderservice.promotionengine.ruleengineservice.rao;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;

public abstract class AbstractOrderRAO extends AbstractActionedRAO {
    protected Long id;
    protected String code;
    protected String type;
    protected Long warehouse;
    protected Long orderSource;
    protected String totalCode;
    protected String priceType;
    protected String currencyIsoCode;
    protected BigDecimal total;
    protected BigDecimal subTotal;
    protected BigDecimal deliveryCost;
    protected BigDecimal paymentCost;
    protected Set<OrderEntryRAO> entries;
    protected UserRAO user;
    protected PaymentModeRAO paymentMode;
    protected List<CouponRAO> coupons;
    protected BigDecimal fixedOrderDiscount;
    protected Date createdDate;

    public DiscountValueRAO getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(DiscountValueRAO discountValue) {
        this.discountValue = discountValue;
    }

    protected DiscountValueRAO discountValue;

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getDeliveryCost() {
        return deliveryCost;
    }

    public void setDeliveryCost(BigDecimal deliveryCost) {
        this.deliveryCost = deliveryCost;
    }

    public BigDecimal getPaymentCost() {
        return paymentCost;
    }

    public void setPaymentCost(BigDecimal paymentCost) {
        this.paymentCost = paymentCost;
    }

    public Set<OrderEntryRAO> getEntries() {
        return entries;
    }

    public void setEntries(Set<OrderEntryRAO> entries) {
        this.entries = entries;
    }

    public UserRAO getUser() {
        return user;
    }

    public void setUser(UserRAO user) {
        this.user = user;
    }

    public PaymentModeRAO getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(PaymentModeRAO paymentMode) {
        this.paymentMode = paymentMode;
    }

    public List<CouponRAO> getCoupons() {
        return coupons;
    }

    public void setCoupons(List<CouponRAO> coupons) {
        this.coupons = coupons;
    }

    public Long getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Long warehouse) {
        this.warehouse = warehouse;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTotalCode() {
        return totalCode;
    }

    public void setTotalCode(String totalCode) {
        this.totalCode = totalCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCurrencyIsoCode() {
        return currencyIsoCode;
    }

    public void setCurrencyIsoCode(String currencyIsoCode) {
        this.currencyIsoCode = currencyIsoCode;
    }

    public BigDecimal getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(BigDecimal subTotal) {
        this.subTotal = subTotal;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getFixedOrderDiscount() {
        return fixedOrderDiscount;
    }

    public void setFixedOrderDiscount(BigDecimal fixedOrderDiscount) {
        this.fixedOrderDiscount = fixedOrderDiscount;
    }

    public String getPriceType() {
        return priceType;
    }

    public void setPriceType(String priceType) {
        this.priceType = priceType;
    }

    public Long getOrderSource() {
        return orderSource;
    }

    public void setOrderSource(Long orderSource) {
        this.orderSource = orderSource;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public boolean equals(final Object o)
    {

        if (o == null) return false;
        if (this.getClass() != o.getClass()) return false;
        if (o == this) return true;
        final AbstractOrderRAO other = (AbstractOrderRAO) o;
        return new org.apache.commons.lang.builder.EqualsBuilder()
                .append(getId(), other.getId())
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new org.apache.commons.lang.builder.HashCodeBuilder()
                .append(getId())
                .toHashCode();
    }
}
