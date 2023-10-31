package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.couponservice.model.CouponCodeModel;
import com.vctek.orderservice.couponservice.model.CouponModel;
import com.vctek.orderservice.dto.RedeemableCouponCodeData;
import com.vctek.orderservice.service.CouponRedemptionService;
import com.vctek.orderservice.service.ValidateCouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ValidateCouponServiceImpl implements ValidateCouponService {
    private CouponRedemptionService couponRedemptionService;
    @Override
    public RedeemableCouponCodeData getValidateRedemptionQuantityCouponCode(CouponCodeModel couponCodeModel, Integer redemptionQty) {
        RedeemableCouponCodeData data = new RedeemableCouponCodeData();
        if(couponCodeModel == null || couponCodeModel.getCoupon() == null) {
            return data;
        }

        CouponModel coupon = couponCodeModel.getCoupon();
        int maxTotalRedemption = coupon.getMaxTotalRedemption();
        Long totalRedemption = couponRedemptionService.countBy(couponCodeModel);
        int redemptionAmount = totalRedemption == null ? 0 : totalRedemption.intValue();
        int redeemableRemain = maxTotalRedemption - redemptionAmount;
        data.setCanRedeem(redemptionQty != null &&  redemptionQty <= redeemableRemain);
        data.setRemainRedeemQuantity(redeemableRemain);
        return data;
    }

    @Autowired
    public void setCouponRedemptionService(CouponRedemptionService couponRedemptionService) {
        this.couponRedemptionService = couponRedemptionService;
    }
}
