package com.vctek.orderservice.couponservice.model;


import com.vctek.orderservice.model.ItemModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "coupon")
public class CouponModel extends ItemModel {

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "name")
    private String name;

    @Column(name = "max_redemption_per_customer")
    private int maxRedemptionPerCustomer;

    @Column(name = "max_total_redemption")
    private int maxTotalRedemption;

    @Column(name = "prefix")
    private String prefix;

    @Column(name = "suffix")
    private String suffix;

    @Column(name = "length")
    private int length;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "active")
    private boolean active;

    @Column(name = "allow_redemption_multiple_coupon")
    private boolean allowRedemptionMultipleCoupon;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "promotion_source_rule_id")
    private PromotionSourceRuleModel promotionSourceRule;

    @OneToMany(mappedBy = "coupon", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CouponCodeModel> couponCodes = new HashSet<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxRedemptionPerCustomer() {
        return maxRedemptionPerCustomer;
    }

    public void setMaxRedemptionPerCustomer(int maxRedemptionPerCustomer) {
        this.maxRedemptionPerCustomer = maxRedemptionPerCustomer;
    }

    public int getMaxTotalRedemption() {
        return maxTotalRedemption;
    }

    public void setMaxTotalRedemption(int maxTotalRedemption) {
        this.maxTotalRedemption = maxTotalRedemption;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Set<CouponCodeModel> getCouponCodes() {
        return couponCodes;
    }

    public void setCouponCodes(Set<CouponCodeModel> couponCodes) {
        this.couponCodes = couponCodes;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public PromotionSourceRuleModel getPromotionSourceRule() {
        return promotionSourceRule;
    }

    public void setPromotionSourceRule(PromotionSourceRuleModel promotionSourceRule) {
        this.promotionSourceRule = promotionSourceRule;
    }

    public boolean isAllowRedemptionMultipleCoupon() {
        return allowRedemptionMultipleCoupon;
    }

    public void setAllowRedemptionMultipleCoupon(boolean allowRedemptionMultipleCoupon) {
        this.allowRedemptionMultipleCoupon = allowRedemptionMultipleCoupon;
    }
}
