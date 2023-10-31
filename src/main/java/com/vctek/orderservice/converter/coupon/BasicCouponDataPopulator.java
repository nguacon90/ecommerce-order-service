package com.vctek.orderservice.converter.coupon;

import com.vctek.converter.Populator;
import com.vctek.orderservice.couponservice.model.CouponModel;
import com.vctek.orderservice.dto.CouponData;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import org.springframework.stereotype.Component;

@Component("basicCouponDataPopulator")
public class BasicCouponDataPopulator implements Populator<CouponModel, CouponData> {

    @Override
    public void populate(CouponModel couponModel, CouponData couponData) {
        couponData.setId(couponModel.getId());
        couponData.setCompanyId(couponModel.getCompanyId());
        couponData.setActive(couponModel.isActive());
        couponData.setLength(couponModel.getLength());
        couponData.setMaxRedemptionPerCustomer(couponModel.getMaxRedemptionPerCustomer());
        couponData.setMaxTotalRedemption(couponModel.getMaxTotalRedemption());
        couponData.setName(couponModel.getName());
        couponData.setPrefix(couponModel.getPrefix());
        couponData.setSuffix(couponModel.getSuffix());
        couponData.setQuantity(couponModel.getQuantity());
        couponData.setAllowRedemptionMultipleCoupon(couponModel.isAllowRedemptionMultipleCoupon());
        PromotionSourceRuleModel promotionSourceRule = couponModel.getPromotionSourceRule();
        if(promotionSourceRule != null) {
            couponData.setUsedForPromotion(true);
            couponData.setSourceRuleId(promotionSourceRule.getId());
            couponData.setSourceRuleName(promotionSourceRule.getMessageFired());
        }
    }
}
