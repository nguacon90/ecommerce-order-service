package com.vctek.orderservice.promotionengine.util;

import java.util.Objects;

public class FixedPriceRuleKey implements Comparable<FixedPriceRuleKey>{
    private Long orderEntryId;
    private String ruleCode;
    private Double fixedPrice;


    public FixedPriceRuleKey(String ruleCode, Double fixedPrice) {
        this.ruleCode = ruleCode;
        this.fixedPrice = fixedPrice;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public Double getFixedPrice() {
        return fixedPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FixedPriceRuleKey)) return false;
        FixedPriceRuleKey that = (FixedPriceRuleKey) o;
        return Objects.equals(getRuleCode(), that.getRuleCode());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getRuleCode());
    }

    @Override
    public int compareTo(FixedPriceRuleKey o) {
        return this.fixedPrice.compareTo(o.getFixedPrice());
    }

    public Long getOrderEntryId() {
        return orderEntryId;
    }

    public void setOrderEntryId(Long orderEntryId) {
        this.orderEntryId = orderEntryId;
    }
}


