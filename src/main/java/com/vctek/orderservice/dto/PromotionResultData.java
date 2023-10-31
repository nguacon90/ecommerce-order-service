package com.vctek.orderservice.dto;

public class PromotionResultData {
    private String code;
    private Long promotionId;
    private String messageFired;
    private String campaignName;
    private Double minValue;
    private boolean appliedOnlyOne;
    private Long productId;
    private String productName;
    private String productSku;
    private String productImage;
    private Double productPrice;

    public Long getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(Long promotionId) {
        this.promotionId = promotionId;
    }

    public String getMessageFired() {
        return messageFired;
    }

    public void setMessageFired(String messageFired) {
        this.messageFired = messageFired;
    }

    public String getCampaignName() {
        return campaignName;
    }

    public void setCampaignName(String campaignName) {
        this.campaignName = campaignName;
    }

    public Double getMinValue() {
        return minValue;
    }

    public void setMinValue(Double minValue) {
        this.minValue = minValue;
    }

    public boolean isAppliedOnlyOne() {
        return appliedOnlyOne;
    }

    public void setAppliedOnlyOne(boolean appliedOnlyOne) {
        this.appliedOnlyOne = appliedOnlyOne;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public Double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(Double productPrice) {
        this.productPrice = productPrice;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
