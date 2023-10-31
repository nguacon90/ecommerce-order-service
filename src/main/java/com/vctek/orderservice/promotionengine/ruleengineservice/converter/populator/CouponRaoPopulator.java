package com.vctek.orderservice.promotionengine.ruleengineservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.couponservice.model.CouponCodeModel;
import com.vctek.orderservice.couponservice.model.CouponModel;
import com.vctek.orderservice.dto.RedeemableCouponCodeData;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.OrderHasCouponCodeModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.CartRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.CouponRAO;
import com.vctek.orderservice.service.ValidateCouponService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component("couponRaoPopulator")
public class CouponRaoPopulator implements Populator<AbstractOrderModel, CartRAO> {
    private ValidateCouponService validateCouponService;
    @Override
    public void populate(AbstractOrderModel cartModel, CartRAO cartRAO) {
        Set<OrderHasCouponCodeModel> orderHasCouponCodeModels = cartModel.getOrderHasCouponCodeModels();
        if (CollectionUtils.isEmpty(orderHasCouponCodeModels)) {
            return;
        }

        List<CouponRAO> coupons = new ArrayList<>();
        CouponRAO couponRAO;
        for (OrderHasCouponCodeModel appliedCouponCode : orderHasCouponCodeModels) {
            couponRAO = new CouponRAO();
            CouponCodeModel couponCode = appliedCouponCode.getCouponCode();
            RedeemableCouponCodeData redeemableCouponCodeData = validateCouponService.getValidateRedemptionQuantityCouponCode(couponCode, appliedCouponCode.getRedemptionQuantity());
            if (redeemableCouponCodeData.isCanRedeem()) {
                CouponModel coupon = couponCode.getCoupon();
                couponRAO.setCouponId(coupon.getId());
                couponRAO.setCouponCode(couponCode.getCode());
                couponRAO.setTotalRedemption(appliedCouponCode.getRedemptionQuantity());
                coupons.add(couponRAO);
            }
        }

        cartRAO.setCoupons(coupons);

    }

    @Autowired
    public void setValidateCouponService(ValidateCouponService validateCouponService) {
        this.validateCouponService = validateCouponService;
    }
}
