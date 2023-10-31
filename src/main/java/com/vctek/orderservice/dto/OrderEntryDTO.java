package com.vctek.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Calendar;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderEntryDTO {
    private Long entryId;
    private String orderCode;
    private String orderType;
    private Long companyId;
    private Long quantity;
    private Long productId;
    private Double discount;
    private String discountType;
    private Double price;
    private Double recommendedRetailPrice;
    private Double weight;
    private Boolean isCombo;
    private Long timeRequest;
    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Boolean getCombo() {
        return isCombo;
    }

    public void setCombo(Boolean combo) {
        isCombo = combo;
    }

    public Long getTimeRequest() {
        return timeRequest;
    }

    public void setTimeRequest(Long timeRequest) {
        if(timeRequest == null) {
            this.timeRequest = Calendar.getInstance().getTimeInMillis();
            return;
        }
        this.timeRequest = timeRequest;
    }

    public Long getEntryId() {
        return entryId;
    }

    public void setEntryId(Long entryId) {
        this.entryId = entryId;
    }

    public Double getRecommendedRetailPrice() {
        return recommendedRetailPrice;
    }

    public void setRecommendedRetailPrice(Double recommendedRetailPrice) {
        this.recommendedRetailPrice = recommendedRetailPrice;
    }
}