package com.vctek.orderservice.service;

import com.vctek.kafka.data.order.OrderProcessData;
import com.vctek.orderservice.dto.request.OrderStatusImportSearchRequest;
import com.vctek.orderservice.model.OrderStatusImportModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderStatusImportService {
    OrderStatusImportModel save(OrderStatusImportModel model);

    OrderStatusImportModel findByIdAndCompanyId(Long id, Long companyId);

    Page<OrderStatusImportModel> search(OrderStatusImportSearchRequest request, Pageable pageable);

    void changeStatusMultipleOrder(OrderProcessData data);

    void handleSendKafkaChangeOrderStatus(OrderStatusImportModel model);
}
