package com.vctek.orderservice.repository;

import com.vctek.orderservice.model.OrderSettingModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderSettingRepository extends JpaRepository<OrderSettingModel, Long> {

    @Query(value = "SELECT * FROM order_setting WHERE type = ?1 and company_id = ?2 ORDER BY id DESC LIMIT 1",
            nativeQuery = true)
    OrderSettingModel findByTypeAndCompanyId(String type, Long companyId);
}
