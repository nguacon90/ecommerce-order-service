package com.vctek.orderservice.service;

import com.vctek.kafka.data.OrderProcessResultData;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.OrderStatusImportDetailModel;
import com.vctek.orderservice.model.OrderStatusImportModel;

import java.util.List;

public interface OrderStatusImportDetailService {

    void updateStatusAndUnlockOrder(OrderProcessResultData data);

    List<OrderStatusImportDetailModel> findAllByOrderStatusImportIdAndCompanyIdAndIdIn(Long orderStatusImportId, Long companyId, List<Long> detailId);

    List<OrderStatusImportDetailModel> saveAll(List<OrderStatusImportDetailModel> models);

    OrderStatusImportDetailModel save(OrderStatusImportDetailModel model);

    OrderStatusImportDetailModel findByIdAndCompanyId(Long id, Long companyId);

    void updateStatusCompletedOrderStatusImportModel(OrderStatusImportModel model);

    void updateLockOrder(OrderModel orderModel, boolean lockOrder);
}
