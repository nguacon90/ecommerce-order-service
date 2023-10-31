package com.vctek.orderservice.promotionengine.promotionservice.model;

import com.vctek.orderservice.model.ItemModel;

import javax.persistence.*;

@Entity
@Table(name = "promotion_budget_consume")
public class PromotionBudgetConsumeModel extends ItemModel {
    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "discount_amount")
    private Double discountAmount;

    @Column(name = "promotion_source_rule_id")
    private Long promotionSourceRuleId;
    @Column(name = "month")
    private Integer month;

    @Column(name = "year")
    private Integer year;

    @Column(name = "order_code")
    private String orderCode;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "promotion_result_id")
    private PromotionResultModel promotionResult;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public PromotionResultModel getPromotionResult() {
        return promotionResult;
    }

    public void setPromotionResult(PromotionResultModel promotionResult) {
        this.promotionResult = promotionResult;
    }

    public Long getPromotionSourceRuleId() {
        return promotionSourceRuleId;
    }

    public void setPromotionSourceRuleId(Long promotionSourceRuleId) {
        this.promotionSourceRuleId = promotionSourceRuleId;
    }
}
