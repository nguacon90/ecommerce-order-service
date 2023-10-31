package com.vctek.orderservice.repository;

import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.ReturnOrderModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface ReturnOrderRepository extends JpaRepository<ReturnOrderModel, Long>, JpaSpecificationExecutor<ReturnOrderModel> {
    ReturnOrderModel findByIdAndCompanyId(Long id, Long companyId);

    List<ReturnOrderModel> findAllByOriginOrder(OrderModel originOrder);

    ReturnOrderModel findByExportExternalIdAndCompanyId(Long exportExternalId, Long companyId);

    List<ReturnOrderModel> findAllByCompanyId(Long companyId);

    List<ReturnOrderModel> findAllByCompanyIdAndCreatedTimeGreaterThanEqual(Long companyId, Date fromDate);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "update return_order " +
            "set revert_amount = ?1 where id = ?2 and company_id = ?3")
    void updateRevertAmount(double amount, Long id, Long companyId);

    Page<ReturnOrderModel> findAllByCompanyId(Long companyId, Pageable pageable);

    List<ReturnOrderModel> findAllByCompanyIdAndExternalIdIsNotNull(Long companyId);
}
