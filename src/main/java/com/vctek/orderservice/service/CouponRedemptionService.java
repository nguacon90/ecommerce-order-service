package com.vctek.orderservice.service;

import com.vctek.orderservice.couponservice.model.CouponCodeModel;
import com.vctek.orderservice.couponservice.model.CouponRedemptionModel;
import com.vctek.orderservice.model.OrderModel;

import java.util.List;

public interface CouponRedemptionService {

    Long countBy(CouponCodeModel codeModel);

    void saveAll(List<CouponRedemptionModel> redemptionModels);

    List<CouponRedemptionModel> findAllBy(OrderModel orderModel);
}
