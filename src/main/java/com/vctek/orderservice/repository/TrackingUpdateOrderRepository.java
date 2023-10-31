package com.vctek.orderservice.repository;

import com.vctek.orderservice.model.TrackingUpdateOrderModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackingUpdateOrderRepository extends JpaRepository<TrackingUpdateOrderModel, Long> {
    TrackingUpdateOrderModel findDistinctTopByOrderCode(String orderCode);
}
