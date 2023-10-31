package com.vctek.orderservice.repository;

import com.vctek.orderservice.model.OrderHasCouponCodeModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderHasCouponRepository extends JpaRepository<OrderHasCouponCodeModel, Long> {

    @Query(value = "select * from orders_has_coupon_code where order_id = ?1", nativeQuery = true)
    List<OrderHasCouponCodeModel> findAllByOrderId(Long orderId);
}
