package com.vctek.orderservice.service;

import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.OrderHistoryModel;
import com.vctek.orderservice.model.OrderModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface OrderHistoryService {
    List<OrderHistoryModel> findAllByOrderId(Long orderId);

    List<OrderHistoryModel> findAllByOrder(AbstractOrderModel orderModel);

    OrderHistoryModel save(OrderHistoryModel model);

    Optional<OrderHistoryModel> findFirstSuccessStatusOf(AbstractOrderModel orderModel);

    Page<OrderHistoryModel> findAllByAndCompanyId(Long companyId, Pageable pageable);

    Page<OrderHistoryModel> findAllByCompanyIdAndProductId(Long companyId, Long productId, Pageable pageable);

    boolean hasChangeShippingToOtherStatus(OrderModel orderModel);

    Page<OrderHistoryModel> findAllByAndCompanyIdAndFromDate(Long companyId, Date fromDate, Pageable pageable);

    Date getLastCompletedDateOf(OrderModel orderModel);
}
