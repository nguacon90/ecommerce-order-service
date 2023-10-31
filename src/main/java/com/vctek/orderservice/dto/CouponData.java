package com.vctek.orderservice.dto;

import java.util.ArrayList;
import java.util.List;

public class CouponData {
    private Long id;
    private Long companyId;
    private String name;
    private int maxRedemptionPerCustomer;
    private int maxTotalRedemption;
    private String prefix;
    private String suffix;
    private int length;
    private int quantity;
    private boolean active;
    private boolean usedForPromotion;
    private List<CouponCodeData> codes = new ArrayList<>();
    private Long sourceRuleId;
    private String sourceRuleName;
    private boolean redemptCoupon;
    private boolean allowRedemptionMultipleCoupon;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

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

    public List<CouponCodeData> getCodes() {
        return codes;
    }

    public void setCodes(List<CouponCodeData> codes) {
        this.codes = codes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isUsedForPromotion() {
        return usedForPromotion;
    }

    public void setUsedForPromotion(boolean usedForPromotion) {
        this.usedForPromotion = usedForPromotion;
    }

    public Long getSourceRuleId() {
        return sourceRuleId;
    }

    public void setSourceRuleId(Long sourceRuleId) {
        this.sourceRuleId = sourceRuleId;
    }

    public String getSourceRuleName() {
        return sourceRuleName;
    }

    public void setSourceRuleName(String sourceRuleName) {
        this.sourceRuleName = sourceRuleName;
    }

    public boolean isRedemptCoupon() {
        return redemptCoupon;
    }

    public void setRedemptCoupon(boolean redemptCoupon) {
        this.redemptCoupon = redemptCoupon;
    }

    public boolean isAllowRedemptionMultipleCoupon() {
        return allowRedemptionMultipleCoupon;
    }

    public void setAllowRedemptionMultipleCoupon(boolean allowRedemptionMultipleCoupon) {
        this.allowRedemptionMultipleCoupon = allowRedemptionMultipleCoupon;
    }
}
