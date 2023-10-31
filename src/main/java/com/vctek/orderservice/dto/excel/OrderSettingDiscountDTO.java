package com.vctek.orderservice.dto.excel;


import org.apache.commons.lang3.StringUtils;

public class OrderSettingDiscountDTO {
    private Long productId;
    private String productSku;
    private String discount;
    private String discountType;
    private String error;
    private Integer rowExcel;

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
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

    public boolean isNotEmpty() {
        if (StringUtils.isNotEmpty(productSku)) {
            return true;
        }
        if (StringUtils.isNotEmpty(discount)) {
            return true;
        }
        if (StringUtils.isNotBlank(discountType)) {
            return true;
        }
        return false;
    }
}