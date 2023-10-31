package com.vctek.orderservice.dto;

import java.util.List;

public class AllLoyaltyRewardRateData {
    private Long companyId;
    private DefaultLoyaltyRewardRateData defaultLoyaltyRewardRate;
    private List<CategoryLoyaltyRewardRateData> categoryRateList;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public DefaultLoyaltyRewardRateData getDefaultLoyaltyRewardRate() {
        return defaultLoyaltyRewardRate;
    }

    public void setDefaultLoyaltyRewardRate(DefaultLoyaltyRewardRateData defaultLoyaltyRewardRate) {
        this.defaultLoyaltyRewardRate = defaultLoyaltyRewardRate;
    }

    public List<CategoryLoyaltyRewardRateData> getCategoryRateList() {
        return categoryRateList;
    }

    public void setCategoryRateList(List<CategoryLoyaltyRewardRateData> categoryRateList) {
        this.categoryRateList = categoryRateList;
    }
}
