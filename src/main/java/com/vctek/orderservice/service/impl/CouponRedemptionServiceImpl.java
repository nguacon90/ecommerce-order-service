package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.couponservice.model.CouponCodeModel;
import com.vctek.orderservice.couponservice.model.CouponRedemptionModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.repository.CouponRedemptionRepository;
import com.vctek.orderservice.service.CouponRedemptionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CouponRedemptionServiceImpl implements CouponRedemptionService {
    private CouponRedemptionRepository couponRedemptionRepository;

    public CouponRedemptionServiceImpl(CouponRedemptionRepository couponRedemptionRepository) {
        this.couponRedemptionRepository = couponRedemptionRepository;
    }

    @Override
    public Long countBy(CouponCodeModel codeModel) {
        return couponRedemptionRepository.countCouponRedemptionModelByCouponCodeModel(codeModel);
    }

    @Override
    public void saveAll(List<CouponRedemptionModel> redemptionModels) {
        couponRedemptionRepository.saveAll(redemptionModels);
    }

    @Override
    public List<CouponRedemptionModel> findAllBy(OrderModel orderModel) {
        return couponRedemptionRepository.findAllByOrder(orderModel);
    }

}
