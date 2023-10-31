package com.vctek.orderservice.dto;

import java.util.ArrayList;
import java.util.List;

public class ValidCouponCodeData {
    private boolean isValid;
    private List<CouponCodeData> couponData = new ArrayList<>();

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public List<CouponCodeData> getCouponData() {
        return couponData;
    }

    public void setCouponData(List<CouponCodeData> couponData) {
        this.couponData = couponData;
    }
}
