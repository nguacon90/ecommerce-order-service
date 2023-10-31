package com.vctek.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vctek.orderservice.promotionengine.promotionservice.util.DiscountValue;

import java.util.ArrayList;
import java.util.List;

public class OrderEntryData {
    private Long id;
    private Integer entryNumber;
    private Long quantity;
    private Double price;
    private Double recommendedRetailPrice;
    private Double totalPriceWithoutDiscount;
    private Double totalPrice;
    private Double finalPrice;
    private Long productId;
    private String productName;
    private String productSku;
    private String productImage;
    private Long parentProductId;
    private String parentProductName;
    private Double discount;
    private String discountType;
    private Double fixedDiscount;
    private Double weight;
    private Double totalDiscount;
    private Double discountOrderToItem;
    private Double finalDiscount;
    private boolean isHolding;
    private boolean isPreOrder;
    private Long holdingStock;
    private String comboType;
    private boolean giveAway;
    private boolean fixedPrice;
    private boolean saleOff;
    private Double rewardAmount;
    private Double awardPoint;
    private Double originBasePrice;
    private Long returnQuantity;
    private Double vat;
    private String vatType;
    private List<SubOrderEntryData> subOrderEntries;
    private List<ToppingOptionData> toppingOptions = new ArrayList<>();
    private CommerceEntryError commerceEntryError;
    private List<Long> promotionSourceRuleIds;

    @JsonIgnore
    private List<DiscountValue> discountValues;

    private boolean appliedPartnerDiscount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getEntryNumber() {
        return entryNumber;
    }

    public void setEntryNumber(Integer entryNumber) {
        this.entryNumber = entryNumber;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public Double getFixedDiscount() {
        return fixedDiscount;
    }

    public void setFixedDiscount(Double fixedDiscount) {
        this.fixedDiscount = fixedDiscount;
    }

    public Double getTotalPriceWithoutDiscount() {
        return totalPriceWithoutDiscount;
    }

    public void setTotalPriceWithoutDiscount(Double totalPriceWithoutDiscount) {
        this.totalPriceWithoutDiscount = totalPriceWithoutDiscount;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Double getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(Double totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public Double getDiscountOrderToItem() {
        return discountOrderToItem;
    }

    public void setDiscountOrderToItem(Double discountOrderToItem) {
        this.discountOrderToItem = discountOrderToItem;
    }

    public Double getFinalDiscount() {
        return finalDiscount;
    }

    public void setFinalDiscount(Double finalDiscount) {
        this.finalDiscount = finalDiscount;
    }

    public boolean isHolding() {
        return isHolding;
    }

    public void setHolding(boolean holding) {
        isHolding = holding;
    }

    public boolean isPreOrder() {
        return isPreOrder;
    }

    public void setPreOrder(boolean preOrder) {
        isPreOrder = preOrder;
    }

    public Long getHoldingStock() {
        return holdingStock;
    }

    public void setHoldingStock(Long holdingStock) {
        this.holdingStock = holdingStock;
    }

    public String getComboType() {
        return comboType;
    }

    public void setComboType(String comboType) {
        this.comboType = comboType;
    }

    public List<SubOrderEntryData> getSubOrderEntries() {
        return subOrderEntries;
    }

    public void setSubOrderEntries(List<SubOrderEntryData> subOrderEntries) {
        this.subOrderEntries = subOrderEntries;
    }

    public boolean isGiveAway() {
        return giveAway;
    }

    public void setGiveAway(boolean giveAway) {
        this.giveAway = giveAway;
    }

    public List<ToppingOptionData> getToppingOptions() {
        return toppingOptions;
    }

    public void setToppingOptions(List<ToppingOptionData> toppingOptions) {
        this.toppingOptions = toppingOptions;
    }

    public Double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(Double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public boolean isFixedPrice() {
        return fixedPrice;
    }

    public void setFixedPrice(boolean fixedPrice) {
        this.fixedPrice = fixedPrice;
    }

    public Double getRewardAmount() {
        return rewardAmount;
    }

    public void setRewardAmount(Double rewardAmount) {
        this.rewardAmount = rewardAmount;
    }

    public Long getReturnQuantity() {
        return returnQuantity;
    }

    public void setReturnQuantity(Long returnQuantity) {
        this.returnQuantity = returnQuantity;
    }

    public Double getAwardPoint() {
        return awardPoint;
    }

    public void setAwardPoint(Double awardPoint) {
        this.awardPoint = awardPoint;
    }

    public Double getOriginBasePrice() {
        return originBasePrice;
    }

    public void setOriginBasePrice(Double originBasePrice) {
        this.originBasePrice = originBasePrice;
    }

    public Double getRecommendedRetailPrice() {
        return recommendedRetailPrice;
    }

    public void setRecommendedRetailPrice(Double recommendedRetailPrice) {
        this.recommendedRetailPrice = recommendedRetailPrice;
    }

    public boolean isSaleOff() {
        return saleOff;
    }

    public void setSaleOff(boolean saleOff) {
        this.saleOff = saleOff;
    }

    public Double getVat() {
        return vat;
    }

    public void setVat(Double vat) {
        this.vat = vat;
    }

    public String getVatType() {
        return vatType;
    }

    public void setVatType(String vatType) {
        this.vatType = vatType;
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

    public Long getParentProductId() {
        return parentProductId;
    }

    public void setParentProductId(Long parentProductId) {
        this.parentProductId = parentProductId;
    }

    public String getParentProductName() {
        return parentProductName;
    }

    public void setParentProductName(String parentProductName) {
        this.parentProductName = parentProductName;
    }

    public CommerceEntryError getCommerceEntryError() {
        return commerceEntryError;
    }

    public void setCommerceEntryError(CommerceEntryError commerceEntryError) {
        this.commerceEntryError = commerceEntryError;
    }

    public List<Long> getPromotionSourceRuleIds() {
        return promotionSourceRuleIds;
    }

    public void setPromotionSourceRuleIds(List<Long> promotionSourceRuleIds) {
        this.promotionSourceRuleIds = promotionSourceRuleIds;
    }

    public boolean isAppliedPartnerDiscount() {
        return appliedPartnerDiscount;
    }

    public void setAppliedPartnerDiscount(boolean appliedPartnerDiscount) {
        this.appliedPartnerDiscount = appliedPartnerDiscount;
    }

    public List<DiscountValue> getDiscountValues() {
        return discountValues;
    }

    public void setDiscountValues(List<DiscountValue> discountValues) {
        this.discountValues = discountValues;
    }
}
