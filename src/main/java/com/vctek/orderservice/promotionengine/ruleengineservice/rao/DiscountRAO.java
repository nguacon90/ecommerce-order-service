package com.vctek.orderservice.promotionengine.ruleengineservice.rao;

import java.math.BigDecimal;

public class DiscountRAO extends AbstractRuleActionRAO {
    private BigDecimal value;
    private long appliedToQuantity;
    private String currencyIsoCode;
    private boolean perUnit;
    private boolean isTheSamePartnerProduct;

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public long getAppliedToQuantity() {
        return appliedToQuantity;
    }

    public void setAppliedToQuantity(long appliedToQuantity) {
        this.appliedToQuantity = appliedToQuantity;
    }

    public boolean isPerUnit() {
        return perUnit;
    }

    public void setPerUnit(boolean perUnit) {
        this.perUnit = perUnit;
    }

    public String getCurrencyIsoCode() {
        return currencyIsoCode;
    }

    public void setCurrencyIsoCode(String currencyIsoCode) {
        this.currencyIsoCode = currencyIsoCode;
    }

    public boolean isTheSamePartnerProduct() {
        return isTheSamePartnerProduct;
    }

    public void setTheSamePartnerProduct(boolean theSamePartnerProduct) {
        isTheSamePartnerProduct = theSamePartnerProduct;
    }
}
