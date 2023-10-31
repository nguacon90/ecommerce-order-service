package com.vctek.orderservice.repository;

import com.vctek.orderservice.model.OrderModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends AbstractOrderRepository<OrderModel>, JpaRepository<OrderModel, Long> {

    OrderModel findByCode(String code);

    Page<OrderModel> findAllByCreatedTimeGreaterThanEqual(Date fromDate, Pageable pageable);

    Page<OrderModel> findAllByCompanyIdAndCreatedTimeGreaterThanEqual(Long companyId, Date fromDate, Pageable pageable);

    Page<OrderModel> findAllByCompanyIdAndTypeIn(Long companyId, List<String> orderTypes, Pageable pageable);

    List<OrderModel> findAllByCompanyIdAndCodeIn(Long companyId, List<String> orderCodes);

    Optional<OrderModel> findByCodeAndCompanyIdAndDeleted(String code, Long companyId, boolean deleted);

    Optional<OrderModel> findByCodeAndCompanyIdAndTypeAndDeleted(String code, Long companyId, String orderType, boolean deleted);

    Page<OrderModel> findAllByDeleted(boolean deleted, Pageable pageable);

    @Query(value = "SELECT o.* FROM orders AS o JOIN return_order as ro ON " +
            " o.id = ro.origin_order_id where  ro.id = ?1 LIMIT 1", nativeQuery = true)
    OrderModel findOriginOrderOf(Long returnOrderId);

    Page<OrderModel> findAllByCompanyIdAndCreatedTimeBetween(Long companyId, Date fromDate, Date toDate, Pageable pageable);

    Page<OrderModel> findAllByCompanyIdAndTypeAndCreatedTimeGreaterThanEqualAndDeleted(Long companyId, String type, Date fromDate, boolean deleted, Pageable pageable);

    Page<OrderModel> findAllByCompanyIdAndTypeAndDeleted(Long companyId, String type, boolean deleted, Pageable pageable);

    @Query(value = "SELECT DISTINCT o.* FROM orders AS o INNER JOIN order_entry AS oe ON o.id = oe.order_id " +
            "INNER JOIN sub_order_entry AS sub ON oe.id = sub.order_entry_id WHERE o.company_id = ?1", nativeQuery = true)
    List<OrderModel> findOrderCombo(Long companyId);

    Page<OrderModel> findAllByCompanyIdOrderByIdAsc(Long companyId, Pageable pageable);

    OrderModel findByCodeAndCompanyId(String orderCode, Long companyId);

    List<OrderModel> findByCompanyIdAndExternalIdAndSellSignal(Long companyId, Long externalId, String sellSignal);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "update orders set paid_amount = :paidAmount where code = :orderCode and company_id = :companyId")
    void updatePaidAmount(@Param("paidAmount") double paidAmount, @Param("orderCode") String orderCode, @Param("companyId") Long companyId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "update orders set bill_id = :billId, import_order_processing = false where code = :orderCode and company_id = :companyId")
    void updateBillToOrder(@Param("billId") long billId, @Param("orderCode") String orderCode, @Param("companyId") Long companyId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "update orders set import_order_processing = :lockOrder where code = :orderCode")
    void updateLockOrder(@Param("orderCode") String orderCode, @Param("lockOrder") boolean lockOrder);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "update orders set import_order_processing = :lockOrder where company_id = :companyId and code in :orderCodes")
    void updateLockOrders(@Param("companyId") Long companyId, @Param("orderCodes") List<String> orderCodes,  @Param("lockOrder") boolean lockOrder);

    Page<OrderModel> findAllByCompanyIdAndOrderStatus(Long companyId, String orderStatus, Pageable pageable);
}
