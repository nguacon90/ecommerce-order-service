package com.vctek.orderservice.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReturnOrderEntryRequest {
    private Long id;
    private Integer entryNumber;
    private Integer quantity;
    private Long companyId;
    private Long productId;
    private String productBarcode;
    private String productName;
    private String productSku;
    private Long warehouseId;
    private Double price;
    private Double discount;
    private String discountType;
    private Double finalDiscount;
    private Long orderEntryId;
    private boolean saleOff;
    private List<ToppingOptionRequest> toppingOptions;

    public Integer getEntryNumber() {
        return entryNumber;
    }

    public void setEntryNumber(Integer entryNumber) {
        this.entryNumber = entryNumber;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductBarcode() {
        return productBarcode;
    }

    public void setProductBarcode(String productBarcode) {
        this.productBarcode = productBarcode;
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

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
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

    public Double getFinalDiscount() {
        return finalDiscount;
    }

    public void setFinalDiscount(Double finalDiscount) {
        this.finalDiscount = finalDiscount;
    }

    public List<ToppingOptionRequest> getToppingOptions() {
        return toppingOptions;
    }

    public void setToppingOptions(List<ToppingOptionRequest> toppingOptions) {
        this.toppingOptions = toppingOptions;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderEntryId() {
        return orderEntryId;
    }

    public void setOrderEntryId(Long orderEntryId) {
        this.orderEntryId = orderEntryId;
    }

    public boolean isSaleOff() {
        return saleOff;
    }

    public void setSaleOff(boolean saleOff) {
        this.saleOff = saleOff;
    }
}
