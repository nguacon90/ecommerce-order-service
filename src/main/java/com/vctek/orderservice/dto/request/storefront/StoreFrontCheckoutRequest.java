package com.vctek.orderservice.dto.request.storefront;

import com.vctek.orderservice.dto.request.OrderRequest;

public class StoreFrontCheckoutRequest extends OrderRequest {
    private Long shippingFeeSettingId;
    private Double finalPrice;

    public Long getShippingFeeSettingId() {
        return shippingFeeSettingId;
    }

    public void setShippingFeeSettingId(Long shippingFeeSettingId) {
        this.shippingFeeSettingId = shippingFeeSettingId;
    }

    public Double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(Double finalPrice) {
        this.finalPrice = finalPrice;
    }
}
