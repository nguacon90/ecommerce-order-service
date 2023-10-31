package com.vctek.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TrackingOrderDetailData {
    private Long entryId;
    private Long subOrderEntryId;
    private Long toppingOptionId;
    private Long productId;
    private Long comboId;
    private Integer quantity;
    private Double price;
    private Double discount;
    private String discountType;
    private Double weight;

    public Long getEntryId() {
        return entryId;
    }

    public void setEntryId(Long entryId) {
        this.entryId = entryId;
    }

    public Long getSubOrderEntryId() {
        return subOrderEntryId;
    }

    public void setSubOrderEntryId(Long subOrderEntryId) {
        this.subOrderEntryId = subOrderEntryId;
    }

    public Long getToppingOptionId() {
        return toppingOptionId;
    }

    public void setToppingOptionId(Long toppingOptionId) {
        this.toppingOptionId = toppingOptionId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
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

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Long getComboId() {
        return comboId;
    }

    public void setComboId(Long comboId) {
        this.comboId = comboId;
    }
}
