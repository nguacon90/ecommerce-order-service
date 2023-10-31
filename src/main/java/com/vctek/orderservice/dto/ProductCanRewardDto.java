package com.vctek.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductCanRewardDto implements Serializable {
    private Long productId;
    private Long orderEntryId;
    private Long toppingOptionId;
    private Long toppingItemId;
    private Double finalPrice;
    private Double rewardRate;
    private Double awardAmount;
    private Long subOrderEntryId;
    private List<ProductCanRewardDto> subOrderEntries;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getOrderEntryId() {
        return orderEntryId;
    }

    public void setOrderEntryId(Long orderEntryId) {
        this.orderEntryId = orderEntryId;
    }

    public Long getToppingOptionId() {
        return toppingOptionId;
    }

    public void setToppingOptionId(Long toppingOptionId) {
        this.toppingOptionId = toppingOptionId;
    }

    public Long getToppingItemId() {
        return toppingItemId;
    }

    public void setToppingItemId(Long toppingItemId) {
        this.toppingItemId = toppingItemId;
    }

    public Double getFinalPrice() {
        //Avoid reward less than zero
        return finalPrice != null && finalPrice > 0 ? finalPrice : 0;
    }

    public void setFinalPrice(Double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public Double getRewardRate() {
        return rewardRate;
    }

    public void setRewardRate(Double rewardRate) {
        this.rewardRate = rewardRate;
    }

    public Double getAwardAmount() {
        return awardAmount;
    }

    public void setAwardAmount(Double awardAmount) {
        this.awardAmount = awardAmount;
    }

    public List<ProductCanRewardDto> getSubOrderEntries() {
        return subOrderEntries;
    }

    public void setSubOrderEntries(List<ProductCanRewardDto> subOrderEntries) {
        this.subOrderEntries = subOrderEntries;
    }

    public Long getSubOrderEntryId() {
        return subOrderEntryId;
    }

    public void setSubOrderEntryId(Long subOrderEntryId) {
        this.subOrderEntryId = subOrderEntryId;
    }

    @Override
    public String toString() {
        return "ProductCanRewardDto{" +
                "productId=" + productId +
                ", orderEntryId=" + orderEntryId +
                ", toppingOptionId=" + toppingOptionId +
                ", toppingItemId=" + toppingItemId +
                ", finalPrice=" + finalPrice +
                ", rewardRate=" + rewardRate +
                ", awardAmount=" + awardAmount +
                ", subOrderEntryId=" + subOrderEntryId +
                ", subOrderEntries=" + subOrderEntries +
                '}';
    }
}
