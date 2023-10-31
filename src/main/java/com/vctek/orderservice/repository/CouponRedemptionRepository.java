package com.vctek.orderservice.repository;

import com.vctek.orderservice.couponservice.model.CouponCodeModel;
import com.vctek.orderservice.couponservice.model.CouponRedemptionModel;
import com.vctek.orderservice.model.AbstractOrderModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponRedemptionRepository extends JpaRepository<CouponRedemptionModel, Long> {
    Long countCouponRedemptionModelByCouponCodeModel(CouponCodeModel couponCodeModel);

    List<CouponRedemptionModel> findAllByOrder(AbstractOrderModel orderModel);
}
