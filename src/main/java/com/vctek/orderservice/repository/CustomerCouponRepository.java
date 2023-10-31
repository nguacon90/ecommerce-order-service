package com.vctek.orderservice.repository;

import com.vctek.orderservice.couponservice.model.CouponCodeModel;
import com.vctek.orderservice.model.CustomerCouponModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerCouponRepository extends JpaRepository<CustomerCouponModel, Long> {
    List<CustomerCouponModel> findAllByUserId(Long userId);
}
