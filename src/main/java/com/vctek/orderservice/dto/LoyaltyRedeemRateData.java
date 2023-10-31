package com.vctek.orderservice.dto;

import java.util.ArrayList;
import java.util.List;

public class LoyaltyRedeemRateData {
    private Long companyId;
    private List<Long> productList = new ArrayList<>();
    private List<Long> categoryList = new ArrayList<>();

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public List<Long> getProductList() {
        return productList;
    }

    public void setProductList(List<Long> productList) {
        this.productList = productList;
    }

    public List<Long> getCategoryList() {
        return categoryList;
    }

    public void setCategoryList(List<Long> categoryList) {
        this.categoryList = categoryList;
    }
}
