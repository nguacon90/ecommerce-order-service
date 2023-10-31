package com.vctek.orderservice.dto.storefront;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vctek.orderservice.dto.CouponCodeData;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserCouponCodeData extends CouponCodeData {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
