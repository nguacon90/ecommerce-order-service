package com.vctek.orderservice.dto.request.storefront;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShippingFeeData {
    private Long shippingFeeSettingId;
    private double shippingFee;
    private double shippingFeeDiscount;

    private Long shippingCompanyId;
    private String shippingCompanyName;
    private Double actualWeight;
    private boolean notFoundShippingExtraSetting;
    private Map<Long, Double> productWeight;

    public double getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(double shippingFee) {
        this.shippingFee = shippingFee;
    }

    public double getShippingFeeDiscount() {
        return shippingFeeDiscount;
    }

    public void setShippingFeeDiscount(double shippingFeeDiscount) {
        this.shippingFeeDiscount = shippingFeeDiscount;
    }

    public Long getShippingCompanyId() {
        return shippingCompanyId;
    }

    public void setShippingCompanyId(Long shippingCompanyId) {
        this.shippingCompanyId = shippingCompanyId;
    }

    public String getShippingCompanyName() {
        return shippingCompanyName;
    }

    public void setShippingCompanyName(String shippingCompanyName) {
        this.shippingCompanyName = shippingCompanyName;
    }

    public Double getActualWeight() {
        return actualWeight;
    }

    public void setActualWeight(Double actualWeight) {
        this.actualWeight = actualWeight;
    }

    public boolean isNotFoundShippingExtraSetting() {
        return notFoundShippingExtraSetting;
    }

    public void setNotFoundShippingExtraSetting(boolean notFoundShippingExtraSetting) {
        this.notFoundShippingExtraSetting = notFoundShippingExtraSetting;
    }

    public Long getShippingFeeSettingId() {
        return shippingFeeSettingId;
    }

    public void setShippingFeeSettingId(Long shippingFeeSettingId) {
        this.shippingFeeSettingId = shippingFeeSettingId;
    }

    public Map<Long, Double> getProductWeight() {
        return productWeight;
    }

    public void setProductWeight(Map<Long, Double> productWeight) {
        this.productWeight = productWeight;
    }
}
