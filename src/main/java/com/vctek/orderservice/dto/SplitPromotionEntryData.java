package com.vctek.orderservice.dto;

public class SplitPromotionEntryData {
    private Long entryId;
    private Long toppingOptionId;
    private Long toppingItemId;
    private Double finalPrice;
    private Double discountToItem;

    public Long getEntryId() {
        return entryId;
    }

    public void setEntryId(Long entryId) {
        this.entryId = entryId;
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
        return finalPrice;
    }

    public void setFinalPrice(Double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public Double getDiscountToItem() {
        return discountToItem;
    }

    public void setDiscountToItem(Double discountToItem) {
        this.discountToItem = discountToItem;
    }
}
