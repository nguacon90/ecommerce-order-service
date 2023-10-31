package com.vctek.orderservice.couponservice.service;

import com.vctek.orderservice.couponservice.couponcodegeneration.dto.CouponCodeConfiguration;

import java.util.Set;

public interface CouponCodeGenerationService {
    Set<String> generateCodes(CouponCodeConfiguration configuration);
}
