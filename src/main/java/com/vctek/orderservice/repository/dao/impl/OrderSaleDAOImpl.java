package com.vctek.orderservice.repository.dao.impl;

import com.vctek.orderservice.dto.SaleQuantity;
import com.vctek.orderservice.dto.request.SaleQuantityRequest;
import com.vctek.orderservice.repository.dao.OrderSaleDAO;
import com.vctek.orderservice.util.DateUtil;
import com.vctek.util.CommonUtils;
import com.vctek.util.OrderStatus;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Repository
public class OrderSaleDAOImpl implements OrderSaleDAO {

    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public List<SaleQuantity> findComboEntrySaleQuantity(SaleQuantityRequest request) {

        StringBuilder sqlBuilder = new StringBuilder("SELECT DISTINCT ");
        sqlBuilder.append("o.code as orderCode, o.order_type as orderType, soe.product_id as productId, soe.quantity as quantity")
                .append(" FROM sub_order_entry as soe JOIN order_entry as oe ON soe.order_entry_id = oe.id")
                .append(" JOIN orders as o ON oe.order_id = o.id LEFT JOIN order_history as oh ON o.id = oh.order_id")
                .append(" WHERE soe.product_id IN (:productIds) AND o.company_id = :companyId AND o.dtype = 'OrderModel' AND ");
        sqlBuilder.append(buildSearchDate(request));
        Session session = entityManager.unwrap(Session.class);
        NativeQuery query = session.createNativeQuery(sqlBuilder.toString());
        query.addScalar("productId", LongType.INSTANCE)
                .addScalar("quantity", LongType.INSTANCE)
                .addScalar("orderCode", StringType.INSTANCE)
                .addScalar("orderType", StringType.INSTANCE)
                .setResultTransformer(Transformers.aliasToBean(SaleQuantity.class));
        query.setParameter("companyId", request.getCompanyId());
        query.setParameter("productIds", CommonUtils.parseLongStringByComma(request.getProductIds()));
        Date fromDate = DateUtil.getEndDay(request.getFromDate());
        Date toDate = DateUtil.getEndDay(request.getToDate());
        if (request.getFromDate() != null) {
            query.setParameter("fromDate", fromDate);
        }
        query.setParameter("toDate", toDate);
        query.setParameter("status", OrderStatus.COMPLETED.code());
        query.setParameter("retailStatus", Arrays.asList(OrderStatus.COMPLETED.code(), OrderStatus.CHANGE_TO_RETAIL.code()));
        return query.getResultList();
    }

    @Override
    public List<SaleQuantity> findEntrySaleQuantity(SaleQuantityRequest request) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT DISTINCT ");
        sqlBuilder.append("o.code as orderCode, o.order_type as orderType, oe.product_id as productId, oe.quantity as quantity")
                .append(" FROM order_entry as oe")
                .append(" JOIN orders as o ON oe.order_id = o.id LEFT JOIN order_history as oh ON o.id = oh.order_id")
                .append(" WHERE oe.product_id IN (:productIds) AND o.company_id = :companyId AND o.dtype = 'OrderModel' AND ");
        sqlBuilder.append(buildSearchDate(request));
        Session session = entityManager.unwrap(Session.class);
        NativeQuery query = session.createNativeQuery(sqlBuilder.toString());
        query.addScalar("productId", LongType.INSTANCE)
                .addScalar("quantity", LongType.INSTANCE)
                .addScalar("orderCode", StringType.INSTANCE)
                .addScalar("orderType", StringType.INSTANCE)
                .setResultTransformer(Transformers.aliasToBean(SaleQuantity.class));
        query.setParameter("companyId", request.getCompanyId());
        query.setParameter("productIds", CommonUtils.parseLongStringByComma(request.getProductIds()));
        Date fromDate = DateUtil.getEndDay(request.getFromDate());
        Date toDate = DateUtil.getEndDay(request.getToDate());
        if (request.getFromDate() != null) {
            query.setParameter("fromDate", fromDate);
        }
        query.setParameter("toDate", toDate);
        query.setParameter("status", OrderStatus.COMPLETED.code());
        query.setParameter("retailStatus", Arrays.asList(OrderStatus.COMPLETED.code(), OrderStatus.CHANGE_TO_RETAIL.code()));
        return query.getResultList();
    }

    private String buildSearchDate(SaleQuantityRequest request) {
        StringBuilder searchDateBuilder = new StringBuilder();
        if (request.getFromDate() != null) {
            searchDateBuilder
                    .append(" (CASE WHEN o.order_type = 'ONLINE' THEN (oh.current_status = :status AND o.order_status = :status ")
                    .append("            AND oh.modified_time BETWEEN :fromDate AND :toDate)")
                    .append("                 OR (o.is_exchange = 1 AND o.created_time BETWEEN :fromDate AND :toDate)")
                    .append("       WHEN o.order_type = 'WHOLESALE' ")
                    .append("       THEN o.order_status = :status ")
                    .append("       AND (o.created_time BETWEEN :fromDate  AND :toDate)")
                    .append("       WHEN o.order_type = 'RETAIL' ")
                    .append("       THEN o.order_status in (:retailStatus)")
                    .append("       AND (o.created_time BETWEEN :fromDate  AND :toDate) ")
                    .append("  END)");
        } else {
            searchDateBuilder
                    .append(" (CASE WHEN o.order_type = 'ONLINE' THEN (oh.current_status = :status AND o.order_status = :status ")
                    .append("            AND oh.modified_time <= :toDate) ")
                    .append("                 OR (o.is_exchange = 1 AND o.created_time <= :toDate)")
                    .append("       WHEN o.order_type = 'WHOLESALE' ")
                    .append("       THEN o.order_status = :status AND o.created_time <= :toDate")
                    .append("       WHEN o.order_type = 'RETAIL' ")
                    .append("       THEN o.order_status in (:retailStatus) AND o.created_time <= :toDate ")
                    .append("  END)");
        }
        return searchDateBuilder.toString();
    }
}
