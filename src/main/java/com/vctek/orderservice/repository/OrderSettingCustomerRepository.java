package com.vctek.orderservice.repository;

import com.vctek.orderservice.model.OrderSettingCustomerModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderSettingCustomerRepository extends JpaRepository<OrderSettingCustomerModel, Long> {

    OrderSettingCustomerModel findByIdAndCompanyIdAndDeleted(Long id, Long companyId, boolean deleted);

    OrderSettingCustomerModel findByCompanyIdAndDeletedAndIsDefault(Long companyId, boolean deleted, boolean isDefault);

    List<OrderSettingCustomerModel> findAllByNameAndDeletedAndCompanyIdAndIsDefault(String name, boolean deleted, Long companyId, boolean isDefault);

    @Query(value = "select osc.* from order_setting_customer as osc join order_type_setting_customer as otsc " +
            "on osc.id = otsc.order_setting_customer_id where osc.company_id = ?1 and otsc.order_type =?2 and osc.deleted = false " +
            "and osc.is_default = false order by osc.priority desc", nativeQuery = true)
    List<OrderSettingCustomerModel> findAllByCompanyIdAndOrderType(Long companyId, String orderType);
}
