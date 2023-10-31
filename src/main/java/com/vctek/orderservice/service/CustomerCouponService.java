package com.vctek.orderservice.service;

import com.vctek.kafka.data.CustomerCouponDto;
import com.vctek.orderservice.dto.storefront.UserCouponCodeData;

import java.util.List;

public interface CustomerCouponService {
    void saveCustomerCoupon(CustomerCouponDto dto);

    List<UserCouponCodeData> getCouponByUser(Long companyId);
}
