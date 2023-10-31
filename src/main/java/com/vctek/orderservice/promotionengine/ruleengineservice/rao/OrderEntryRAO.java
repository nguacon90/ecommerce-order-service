package com.vctek.orderservice.promotionengine.ruleengineservice.rao;

import java.math.BigDecimal;

public class OrderEntryRAO extends AbstractActionedRAO {
    private Long id;
    private Integer entryNumber;
    private int quantity;
    private BigDecimal basePrice;
    private BigDecimal totalPrice;
    private BigDecimal fixedDiscount;
    private AbstractOrderRAO order;
    private ProductRAO product;
    private String currencyIsoCode;
    private BigDecimal totalToppingPrice;
    private BigDecimal totalToppingFixedDiscount;

    public Integer getEntryNumber() {
        return entryNumber;
    }

    public void setEntryNumber(Integer entryNumber) {
        this.entryNumber = entryNumber;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public AbstractOrderRAO getOrder() {
        return order;
    }

    public void setOrder(AbstractOrderRAO order) {
        this.order = order;
    }

    public ProductRAO getProduct() {
        return product;
    }

    public void setProduct(ProductRAO product) {
        this.product = product;
    }

    public String getCurrencyIsoCode() {
        return currencyIsoCode;
    }

    public void setCurrencyIsoCode(String currencyIsoCode) {
        this.currencyIsoCode = currencyIsoCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(final Object o) {

        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof OrderEntryRAO)) return false;
        final OrderEntryRAO other = (OrderEntryRAO) o;
        return new org.apache.commons.lang.builder.EqualsBuilder()
                .append(getEntryNumber(), other.getEntryNumber())
                .append(getOrder(), other.getOrder())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new org.apache.commons.lang.builder.HashCodeBuilder()
                .append(getEntryNumber())
                .append(getOrder())
                .toHashCode();
    }

    public BigDecimal getFixedDiscount() {
        return fixedDiscount;
    }

    public void setFixedDiscount(BigDecimal fixedDiscount) {
        this.fixedDiscount = fixedDiscount;
    }

    public BigDecimal getTotalToppingPrice() {
        return totalToppingPrice;
    }

    public void setTotalToppingPrice(BigDecimal totalToppingPrice) {
        this.totalToppingPrice = totalToppingPrice;
    }

    public BigDecimal getTotalToppingFixedDiscount() {
        return totalToppingFixedDiscount;
    }

    public void setTotalToppingFixedDiscount(BigDecimal totalToppingFixedDiscount) {
        this.totalToppingFixedDiscount = totalToppingFixedDiscount;
    }
}
