package com.vctek.orderservice.promotionengine.promotionservice.model;

import com.vctek.orderservice.model.ItemModel;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "promotion_budget")
public class PromotionBudgetModel extends ItemModel {
    @Column(name = "budget_amount")
    private Double budgetAmount;
    @Column(name = "schedule_type")
    private String scheduleType;
    @Column(name = "customer_group_id")
    private Long customerGroupId;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "promotion_source_rule_id")
    private PromotionSourceRuleModel promotionSourceRuleModel;

    public Double getBudgetAmount() {
        return budgetAmount;
    }

    public void setBudgetAmount(Double budgetAmount) {
        this.budgetAmount = budgetAmount;
    }

    public String getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(String scheduleType) {
        this.scheduleType = scheduleType;
    }

    public Long getCustomerGroupId() {
        return customerGroupId;
    }

    public void setCustomerGroupId(Long customerGroupId) {
        this.customerGroupId = customerGroupId;
    }

    public PromotionSourceRuleModel getPromotionSourceRuleModel() {
        return promotionSourceRuleModel;
    }

    public void setPromotionSourceRuleModel(PromotionSourceRuleModel promotionSourceRuleModel) {
        this.promotionSourceRuleModel = promotionSourceRuleModel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PromotionBudgetModel)) return false;
        PromotionBudgetModel that = (PromotionBudgetModel) o;
        if(this.getId() == null && that.getId() == null) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
