package com.vctek.orderservice.repository;

import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.OrderEntryModel;
import com.vctek.orderservice.model.OrderModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface OrderEntryRepository extends AbstractOrderEntryRepository<OrderEntryModel>, JpaRepository<OrderEntryModel, Long>, JpaSpecificationExecutor {

    List<OrderEntryModel> findAllByOrderCode(String orderCode);

    List<OrderEntryModel> findAllByOrder(OrderModel order);

    @Query(value = "select distinct sum(oe.quantity) from order_entry oe " +
            "join orders o on oe.order_id = o.id " +
            "where oe.product_id = :productId and o.order_type != :orderType " +
            "and o.company_id = :companyId and o.created_time >= :fromDate", nativeQuery = true)
    Long getListOfProductSellingExcludeOnline(@Param("productId") Long productId, @Param("orderType") String orderType, @Param("companyId") Long companyId, @Param("fromDate") Date fromDate);

    @Query(value = "select sum(distinct(oe.quantity)) from order_entry oe " +
            "left join orders o on oe.order_id = o.id " +
            "left join order_history oh on o.id = oh.order_id " +
            "where oe.product_id = :productId and o.order_type = :orderType " +
            "and o.company_id = :companyId and oh.modified_time >= :fromDate " +
            "and o.order_status = :orderStatus", nativeQuery = true)
    Long getListOfProductSellingOnline(@Param("productId") Long productId,
                                                        @Param("orderType") String orderType,
                                                        @Param("companyId") Long companyId,
                                                        @Param("fromDate") Date fromDate,
                                                        @Param("orderStatus") String orderStatus);

    OrderEntryModel findByOrderAndEntryNumber(OrderModel order, Integer entryNumber);

    OrderEntryModel findByIdAndOrder(Long id, OrderModel order);

    OrderEntryModel findByOrderAndId(OrderModel order, Long entryId);
}
