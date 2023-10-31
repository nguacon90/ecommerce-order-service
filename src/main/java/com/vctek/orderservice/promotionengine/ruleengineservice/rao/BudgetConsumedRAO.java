package com.vctek.orderservice.promotionengine.ruleengineservice.rao;

import java.io.Serializable;
import java.math.BigDecimal;

public class BudgetConsumedRAO implements Serializable {
    private static final long serialVersionUID = -2754290368266626771L;
    private String firedRuleCode;
    private Long promotionSourceRuleId;
    private BigDecimal discountAmount;
    private Long customerId;
    private String orderCode;
    private int month;
    private int year;

    public String getFiredRuleCode() {
        return firedRuleCode;
    }

    public void setFiredRuleCode(String firedRuleCode) {
        this.firedRuleCode = firedRuleCode;
    }

    public Long getPromotionSourceRuleId() {
        return promotionSourceRuleId;
    }

    public void setPromotionSourceRuleId(Long promotionSourceRuleId) {
        this.promotionSourceRuleId = promotionSourceRuleId;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }
}
