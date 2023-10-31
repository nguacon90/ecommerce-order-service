package com.vctek.orderservice.couponservice.couponcodegeneration;


import com.vctek.orderservice.couponservice.couponcodegeneration.dto.CouponCodeConfiguration;

public interface CouponCodesGenerator {
    String generateNextCouponCode(CouponCodeConfiguration configuration);
}
