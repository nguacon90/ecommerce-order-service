package com.vctek.orderservice.dto;

public class ProductLoyaltyRewardRateData {
    private Long productId;
    private Double rewardRate;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Double getRewardRate() {
        return rewardRate;
    }

    public void setRewardRate(Double rewardRate) {
        this.rewardRate = rewardRate;
    }
}
