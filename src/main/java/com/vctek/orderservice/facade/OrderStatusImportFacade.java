package com.vctek.orderservice.facade;

import com.vctek.orderservice.dto.OrderStatusImportData;
import com.vctek.orderservice.dto.request.OrderStatusImportRequest;
import com.vctek.orderservice.dto.request.OrderStatusImportSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderStatusImportFacade {
    OrderStatusImportData createStatusImport(OrderStatusImportRequest request);

    OrderStatusImportData findByIdAndCompanyId(Long id, Long companyId);

    Page<OrderStatusImportData> search(OrderStatusImportSearchRequest request, Pageable pageable);
}
