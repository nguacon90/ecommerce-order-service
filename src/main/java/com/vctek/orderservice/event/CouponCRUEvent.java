package com.vctek.orderservice.event;

import com.vctek.orderservice.couponservice.model.CouponModel;
import org.springframework.context.ApplicationEvent;

public class CouponCRUEvent{
    private CouponModel couponModel;
    public CouponCRUEvent(CouponModel model) {
        this.couponModel = model;
    }

    public CouponModel getCouponModel() {
        return couponModel;
    }

    public void setCouponModel(CouponModel couponModel) {
        this.couponModel = couponModel;
    }
}
