package com.vctek.orderservice.promotionengine.promotionservice.model;

import com.vctek.orderservice.couponservice.model.CouponModel;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.SourceRuleModel;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "promotion_source_rule")
@EntityListeners({AuditingEntityListener.class})
public class PromotionSourceRuleModel extends SourceRuleModel {

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "applied_warehouse_ids")
    private String appliedWarehouseIds;

    @Column(name = "applied_order_types")
    private String appliedOrderTypes;

    @Column(name = "applied_price_types")
    private String appliedPriceTypes;

    @Column(name = "applied_customer_types")
    private String appliedCustomerTypes;

    @Column(name = "exclude_order_sources")
    private String excludeOrderSources;

    @Column(name = "applied_only_one")
    private boolean appliedOnlyOne;

    @OneToMany(mappedBy = "promotionSourceRule")
    private Set<DroolsRuleModel> droolsRules;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(
            name = "campaign_has_promotion",
            joinColumns = { @JoinColumn(name = "promotion_source_rule_id") },
            inverseJoinColumns = { @JoinColumn(name = "promotion_campaign_id") }
    )
    private Set<CampaignModel> campaigns = new HashSet<>();

    @Column(name = "created_date")
    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Column(name = "created_by")
    @CreatedBy
    private Long createdBy;

    @Column(name = "modified_by")
    @LastModifiedBy
    private Long modifiedBy;

    @Column(name = "modified_date")
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedDate;

    @OneToMany(mappedBy = "promotionSourceRule", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<CouponModel> coupons;

    @ManyToMany(mappedBy = "couldFirePromotions")
    private Set<AbstractOrderModel> orderModels;

    @Column(name = "allow_reward")
    private boolean allowReward;

    @OneToMany(mappedBy = "promotionSourceRuleModel")
    private Set<PromotionBudgetModel> promotionBudgetModels;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getAppliedWarehouseIds() {
        return appliedWarehouseIds;
    }

    public void setAppliedWarehouseIds(String appliedWarehouseIds) {
        this.appliedWarehouseIds = appliedWarehouseIds;
    }

    public String getAppliedOrderTypes() {
        return appliedOrderTypes;
    }

    public void setAppliedOrderTypes(String appliedOrderTypes) {
        this.appliedOrderTypes = appliedOrderTypes;
    }

    public String getAppliedCustomerTypes() {
        return appliedCustomerTypes;
    }

    public void setAppliedCustomerTypes(String appliedCustomerTypes) {
        this.appliedCustomerTypes = appliedCustomerTypes;
    }

    public Set<DroolsRuleModel> getDroolsRules() {
        return droolsRules;
    }

    public void setDroolsRules(Set<DroolsRuleModel> droolsRules) {
        this.droolsRules = droolsRules;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public Set<CampaignModel> getCampaigns() {
        return campaigns;
    }

    public void setCampaigns(Set<CampaignModel> campaigns) {
        this.campaigns = campaigns;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(Long modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Set<CouponModel> getCoupons() {
        return coupons;
    }

    public void setCoupons(Set<CouponModel> coupons) {
        this.coupons = coupons;
    }

    public boolean isAllowReward() {
        return allowReward;
    }

    public void setAllowReward(boolean allowReward) {
        this.allowReward = allowReward;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PromotionSourceRuleModel)) return false;
        PromotionSourceRuleModel that = (PromotionSourceRuleModel) o;
        if(this.getId() == null && that.getId() == null) return false;
        return Objects.equals(this.getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }

    public String getAppliedPriceTypes() {
        return appliedPriceTypes;
    }

    public void setAppliedPriceTypes(String appliedPriceTypes) {
        this.appliedPriceTypes = appliedPriceTypes;
    }

    public boolean isAppliedOnlyOne() {
        return appliedOnlyOne;
    }

    public void setAppliedOnlyOne(boolean appliedOnlyOne) {
        this.appliedOnlyOne = appliedOnlyOne;
    }

    public Set<AbstractOrderModel> getOrderModels() {
        return orderModels;
    }

    public void setOrderModels(Set<AbstractOrderModel> orderModels) {
        this.orderModels = orderModels;
    }

    public String getExcludeOrderSources() {
        return excludeOrderSources;
    }

    public void setExcludeOrderSources(String excludeOrderSources) {
        this.excludeOrderSources = excludeOrderSources;
    }

    public Set<PromotionBudgetModel> getPromotionBudgetModels() {
        return promotionBudgetModels;
    }

    public void setPromotionBudgetModels(Set<PromotionBudgetModel> promotionBudgetModels) {
        this.promotionBudgetModels = promotionBudgetModels;
    }
}
