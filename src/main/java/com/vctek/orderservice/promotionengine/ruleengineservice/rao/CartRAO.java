package com.vctek.orderservice.promotionengine.ruleengineservice.rao;

import java.math.BigDecimal;
import java.util.List;

public class CartRAO extends AbstractOrderRAO {
    protected BigDecimal originalTotal;

    private List<PromotionBudgetRAO> promotionBudgetList;

    public BigDecimal getOriginalTotal() {
        return originalTotal;
    }

    public void setOriginalTotal(BigDecimal originalTotal) {
        this.originalTotal = originalTotal;
    }

    public List<PromotionBudgetRAO> getPromotionBudgetList() {
        return promotionBudgetList;
    }

    public void setPromotionBudgetList(List<PromotionBudgetRAO> promotionBudgetList) {
        this.promotionBudgetList = promotionBudgetList;
    }
}
