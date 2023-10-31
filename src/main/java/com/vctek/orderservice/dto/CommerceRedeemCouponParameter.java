package com.vctek.orderservice.dto;

import com.vctek.orderservice.model.AbstractOrderModel;

public class CommerceRedeemCouponParameter {
    private AbstractOrderModel abstractOrderModel;
    private String couponCode;
    private Integer redemptionQuantity;


    public CommerceRedeemCouponParameter(AbstractOrderModel abstractOrderModel, String couponCode) {
        this.abstractOrderModel = abstractOrderModel;
        this.couponCode = couponCode;
    }

    public AbstractOrderModel getAbstractOrderModel() {
        return abstractOrderModel;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public Integer getRedemptionQuantity() {
        return redemptionQuantity;
    }

    public void setRedemptionQuantity(Integer redemptionQuantity) {
        this.redemptionQuantity = redemptionQuantity;
    }
}
