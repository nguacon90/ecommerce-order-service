package com.vctek.orderservice.service;

import com.vctek.orderservice.couponservice.model.CouponCodeModel;
import com.vctek.orderservice.dto.RedeemableCouponCodeData;

public interface ValidateCouponService {
    RedeemableCouponCodeData getValidateRedemptionQuantityCouponCode(CouponCodeModel couponCode, Integer redemptionQuantity);
}
