package com.vctek.orderservice.repository;

import com.vctek.orderservice.model.OrderStatusImportModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderStatusImportRepository extends JpaRepository<OrderStatusImportModel, Long> {
    OrderStatusImportModel findByIdAndCompanyId(Long id, Long companyId);
}
