package com.vctek.orderservice.repository;

import com.vctek.orderservice.model.OrderSettingCustomerModel;
import com.vctek.orderservice.model.OrderSettingCustomerOptionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderSettingCustomerOptionRepository extends JpaRepository<OrderSettingCustomerOptionModel, Long> {

    List<OrderSettingCustomerOptionModel> findByOrderSettingCustomerModelAndDeleted(OrderSettingCustomerModel orderSettingCustomerModel, boolean deleted);

    @Query(value = "select osco.* from order_setting_customer_option osco join order_setting_customer osc on osco.order_setting_customer_id = osc.id " +
            "where osc.deleted = false and osco.id = ?1 and osc.company_id = ?2 and osco.deleted = ?3", nativeQuery = true)
    OrderSettingCustomerOptionModel findByIdAndCompanyIdAndDeleted(Long id, Long companyId, boolean deleted);

    @Query(value = "SELECT osco.* FROM order_has_setting_customer_option ohsco join order_setting_customer_option osco " +
            "on ohsco.order_setting_customer_option_id = osco.id " +
            "where osco.deleted = false and ohsco.order_id = ?1", nativeQuery = true)
    List<OrderSettingCustomerOptionModel> findAllByOrderId(Long orderId);

    @Query(value = "SELECT distinct osco.* FROM order_has_setting_customer_option ohsco join order_setting_customer_option osco " +
            "on ohsco.order_setting_customer_option_id = osco.id join order_setting_customer osc on osc.id = osco.order_setting_customer_id " +
            "where osc.company_id = ?1 and osco.deleted = false", nativeQuery = true)
    List<OrderSettingCustomerOptionModel> findAllByCompanyNotHasOrder(Long companyId);

    @Query(value = "select osco.* from order_setting_customer_option osco join order_setting_customer osc on osco.order_setting_customer_id = osc.id " +
            "where osc.deleted = false and osco.id in :ids and osc.company_id = :companyId and osco.deleted = false", nativeQuery = true)
    List<OrderSettingCustomerOptionModel> findAllByCompanyIdAndIdIn(@Param("companyId") Long companyId, @Param("ids") List<Long> ids);
}
