package com.vctek.orderservice.dto.excel;

import com.vctek.orderservice.elasticsearch.model.ProductSearchModel;
import com.vctek.redis.elastic.ProductSearchData;
import org.apache.commons.lang3.StringUtils;

public class OrderItemDTO {
    private ProductSearchModel productData;
    private String sku;
    private String quantity;
    private String price;
    private String discount;
    private String discountType;
    private Double recommendedRetailPrice;
    private String error;
    private String errorValue;
    private Integer rowExcel;

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public boolean isNotEmpty() {
        if(StringUtils.isNotBlank(sku)) {
            return true;
        }
        if(StringUtils.isNotBlank(quantity)) {
            return true;
        }
        if(StringUtils.isNotBlank(discount)) {
            return true;
        }
        if(StringUtils.isNotBlank(price)) {
            return true;
        }
        return false;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Integer getRowExcel() {
        return rowExcel;
    }

    public void setRowExcel(Integer rowExcel) {
        this.rowExcel = rowExcel;
    }
    public ProductSearchModel getProductData() {
        return productData;
    }

    public void setProductData(ProductSearchModel productData) {
        this.productData = productData;
    }

    public String getErrorValue() {
        return errorValue;
    }

    public void setErrorValue(String errorValue) {
        this.errorValue = errorValue;
    }

    public Double getRecommendedRetailPrice() {
        return recommendedRetailPrice;
    }

    public void setRecommendedRetailPrice(Double recommendedRetailPrice) {
        this.recommendedRetailPrice = recommendedRetailPrice;
    }
}
