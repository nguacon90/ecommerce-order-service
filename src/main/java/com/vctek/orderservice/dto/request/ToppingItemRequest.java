package com.vctek.orderservice.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Calendar;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ToppingItemRequest {
    private String orderCode;
    private Long toppingOptionId;
    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private Double price;
    private Long companyId;
    private Long entryId;
    private Double totalPrice;
    private Integer totalQuantity;
    private Double discountOrderToItem;
    private Double discount;
    private String discountType;
    private Long timeRequest;
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getToppingOptionId() {
        return toppingOptionId;
    }

    public void setToppingOptionId(Long toppingOptionId) {
        this.toppingOptionId = toppingOptionId;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Double getDiscountOrderToItem() {
        return discountOrderToItem;
    }

    public void setDiscountOrderToItem(Double discountOrderToItem) {
        this.discountOrderToItem = discountOrderToItem;
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
}
