package com.vctek.orderservice.dto.request.storefront;

import java.util.List;

public class ShippingFeeRequest {
    private Long companyId;
    private Long provinceId;
    private Long districtId;
    private Double orderAmount;
    private Long productId;
    private int quantity;

    private List<ProductInfoRequest> products;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(Long provinceId) {
        this.provinceId = provinceId;
    }

    public Long getDistrictId() {
        return districtId;
    }

    public void setDistrictId(Long districtId) {
        this.districtId = districtId;
    }

    public Double getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(Double orderAmount) {
        this.orderAmount = orderAmount;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public List<ProductInfoRequest> getProducts() {
        return products;
    }

    public void setProducts(List<ProductInfoRequest> products) {
        this.products = products;
    }
}
