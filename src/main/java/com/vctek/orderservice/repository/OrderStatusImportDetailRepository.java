package com.vctek.orderservice.repository;

import com.vctek.orderservice.model.OrderStatusImportDetailModel;
import com.vctek.orderservice.model.OrderStatusImportModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderStatusImportDetailRepository extends JpaRepository<OrderStatusImportDetailModel, Long> {
    @Query(value = "select DISTINCT osid.* from order_status_import_detail osid join order_status_import osi " +
            "on osid.order_status_import_id = osi.id where osid.id = ?1 and osi.company_id = ?2", nativeQuery = true)
    Optional<OrderStatusImportDetailModel> findByIdAndCompanyId(Long id, Long companyId);

    @Query(value = "select DISTINCT osid.* from order_status_import_detail osid join order_status_import osi " +
            "on osid.order_status_import_id = osi.id where osi.id = ?1 and osi.company_id = ?2 and osid.id in ?3", nativeQuery = true)
    List<OrderStatusImportDetailModel> findAllByOrderStatusImportIdAndCompanyIdAndIdIn(Long orderStatusImportId, Long companyId, List<Long> ids);

    Optional<OrderStatusImportDetailModel>findDistinctTopByOrderStatusImportModelAndStatus(OrderStatusImportModel importModel, String status);

}
