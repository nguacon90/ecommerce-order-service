package com.vctek.orderservice.dto;

public class CategoryLoyaltyRewardRateData {
    private Long categoryId;
    private Double rewardRate;

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Double getRewardRate() {
        return rewardRate;
    }

    public void setRewardRate(Double rewardRate) {
        this.rewardRate = rewardRate;
    }
}
