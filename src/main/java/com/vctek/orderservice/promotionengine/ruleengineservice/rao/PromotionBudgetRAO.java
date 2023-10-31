package com.vctek.orderservice.promotionengine.ruleengineservice.rao;

import java.io.Serializable;
import java.util.Objects;

public class PromotionBudgetRAO implements Serializable {
    private static final long serialVersionUID = -2298660733343913320L;
    private Long sourceRuleId;
    private Double remainDiscount;

    public Double getRemainDiscount() {
        return remainDiscount;
    }

    public void setRemainDiscount(Double remainDiscount) {
        this.remainDiscount = remainDiscount;
    }

    public Long getSourceRuleId() {
        return sourceRuleId;
    }

    public void setSourceRuleId(Long sourceRuleId) {
        this.sourceRuleId = sourceRuleId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PromotionBudgetRAO)) return false;
        PromotionBudgetRAO that = (PromotionBudgetRAO) o;
        if(this.getSourceRuleId() == null && that.getSourceRuleId() == null) return false;
        return Objects.equals(getSourceRuleId(), that.getSourceRuleId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSourceRuleId());
    }

}
