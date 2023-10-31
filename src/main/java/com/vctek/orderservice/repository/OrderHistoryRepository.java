package com.vctek.orderservice.repository;

import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.OrderHistoryModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderHistoryRepository extends JpaRepository<OrderHistoryModel, Long> {

    List<OrderHistoryModel> findAllByOrderOrderByModifiedTimeDesc(AbstractOrderModel model);

    @Query(value = "select e.* from order_history e where e.order_id = ?1 ORDER BY modified_time ASC", nativeQuery = true)
    List<OrderHistoryModel> findAllByOrderId(Long orderId);

    @Query(value = "select * from order_history where order_id = ?1 and current_status = ?2 ORDER BY id ASC LIMIT 1",
            nativeQuery = true)
    Optional<OrderHistoryModel> findFirstOldestBy(Long orderId, String status);

    @Query(value = "select * from order_history where order_id = ?1 ORDER BY id DESC LIMIT 1",
            nativeQuery = true)
    Optional<OrderHistoryModel> findLastBy(Long orderId);

    @Query(value = "select * from order_history where order_id = ?1 and current_status = ?2 ORDER BY id DESC LIMIT 1",
            nativeQuery = true)
    Optional<OrderHistoryModel> findLastHistoryByOrderIdAndStatus(Long orderId, String status);

    @Query(value = "select * from qtn_orders.order_history where id IN ( " +
            "SELECT MAX(oh.id) FROM qtn_orders.order_history as oh " +
            "join qtn_orders.orders as o on o.id = oh.order_id " +
            "join qtn_orders.order_entry as oe on o.id = oe.order_id " +
            "where o.order_type = 'ONLINE' and o.dtype = 'OrderModel' and o.deleted = 0 " +
            "and (oh.previous_status = 'SHIPPING' OR oh.previous_status = 'RETURNING') " +
            "and o.company_id = ?1 and o.order_status IN ('COMPLETED') " +
            "group by o.id )",
            countQuery = "SELECT COUNT(*) FROM (select * from qtn_orders.order_history where id IN ( " +
                    "SELECT MAX(oh.id) FROM qtn_orders.order_history as oh " +
                    "join qtn_orders.orders as o on o.id = oh.order_id " +
                    "join qtn_orders.order_entry as oe on o.id = oe.order_id " +
                    "where o.order_type = 'ONLINE' and o.dtype = 'OrderModel' and o.deleted = 0 " +
                    "and (oh.previous_status = 'SHIPPING' OR oh.previous_status = 'RETURNING') " +
                    "and o.company_id = ?1 and o.order_status IN ('COMPLETED') " +
                    "group by o.id )) A ",
            nativeQuery = true)
    Page<OrderHistoryModel> findAllByAndCompanyId(Long companyId, Pageable pageable);

    @Query(value = "select * from qtn_orders.order_history where id IN ( " +
            "SELECT MAX(oh.id) FROM qtn_orders.order_history as oh " +
            "join qtn_orders.orders as o on o.id = oh.order_id " +
            "join qtn_orders.order_entry as oe on o.id = oe.order_id " +
            "where o.order_type = 'ONLINE' and o.dtype = 'OrderModel' and o.deleted = 0 " +
            "and (oh.previous_status = 'SHIPPING' OR oh.previous_status = 'RETURNING') " +
            "and o.company_id = ?1 and o.order_status IN ('COMPLETED') " +
            "and oe.product_id = ?2 " +
            "group by o.id )",
            countQuery = "SELECT COUNT(*) FROM (select * from qtn_orders.order_history where id IN ( " +
                    "SELECT MAX(oh.id) FROM qtn_orders.order_history as oh " +
                    "join qtn_orders.orders as o on o.id = oh.order_id " +
                    "join qtn_orders.order_entry as oe on o.id = oe.order_id " +
                    "where o.order_type = 'ONLINE' and o.dtype = 'OrderModel' and o.deleted = 0 " +
                    "and (oh.previous_status = 'SHIPPING' OR oh.previous_status = 'RETURNING') " +
                    "and o.company_id = ?1 and o.order_status IN ('COMPLETED') " +
                    "and oe.product_id = ?2 " +
                    "group by o.id )) A",
            nativeQuery = true)
    Page<OrderHistoryModel> findAllByAndCompanyIdAndProductId(Long companyId, Long productId, Pageable pageable);


    @Query(value = "select * from qtn_orders.order_history where id IN ( " +
            "SELECT MAX(oh.id) FROM qtn_orders.order_history as oh " +
            "join qtn_orders.orders as o on o.id = oh.order_id " +
            "join qtn_orders.order_entry as oe on o.id = oe.order_id " +
            "where o.order_type = 'ONLINE' and o.dtype = 'OrderModel' and o.deleted = 0 " +
            "and (oh.previous_status = 'SHIPPING' OR oh.previous_status = 'RETURNING') " +
            "and o.company_id = ?1 and o.order_status IN ('COMPLETED') and o.created_time >= ?2 " +
            "group by o.id )",
            countQuery = "SELECT COUNT(*) FROM (select * from qtn_orders.order_history where id IN ( " +
                    "SELECT MAX(oh.id) FROM qtn_orders.order_history as oh " +
                    "join qtn_orders.orders as o on o.id = oh.order_id " +
                    "join qtn_orders.order_entry as oe on o.id = oe.order_id " +
                    "where o.order_type = 'ONLINE' and o.dtype = 'OrderModel' and o.deleted = 0 " +
                    "and (oh.previous_status = 'SHIPPING' OR oh.previous_status = 'RETURNING') " +
                    "and o.company_id = ?1 and o.order_status IN ('COMPLETED') and o.created_time >= ?2 " +
                    "group by o.id )) A ",
            nativeQuery = true)
    Page<OrderHistoryModel> findAllByAndCompanyIdAndFromDate(Long companyId, Date fromDate, Pageable pageable);
}
