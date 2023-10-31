package com.vctek.orderservice.service;

import com.vctek.orderservice.couponservice.model.CouponCodeModel;
import com.vctek.orderservice.couponservice.model.CouponModel;
import com.vctek.orderservice.dto.CommerceRedeemCouponParameter;
import com.vctek.orderservice.dto.RedeemableCouponCodeData;
import com.vctek.orderservice.dto.ValidCouponCodeData;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CouponService {
    CouponModel save(CouponModel model);

    List<CouponModel> findAllForQualifyingByCompanyId(Long companyId);

    void updateUseForPromotion(List<Long> coupons, PromotionSourceRuleModel rule);

    Page<CouponModel> findAllBy(Long companyId, String name, Pageable pageable);

    CouponModel findById(Long couponId, Long companyId);

    void delete(CouponModel couponModel);

    List<CouponModel> findAllForQualifyingByCompanyIdOrSourceRule(Long companyId, Long sourceRuleId);

    void redeemCoupon(CommerceRedeemCouponParameter parameter);

    void createCouponRedemption(OrderModel savedOrder);

    void releaseCoupon(CommerceRedeemCouponParameter parameter);

    void removeCouponRedemption(OrderModel order);

    void removeCouponToSourceRule(PromotionSourceRuleModel rule);

    ValidCouponCodeData getValidatedCouponCode(AbstractOrderModel abstractOrderModel);

    List<CouponCodeModel> findAllCouponCodeBy(CouponModel source);

    Page<CouponModel> findAllByCompanyId(Long companyId, Pageable pageable);

    void revertAllCouponToOrder(OrderModel orderModel);

    CouponCodeModel findValidatedCouponCode(String clearedCouponCode, Long companyId);

}
