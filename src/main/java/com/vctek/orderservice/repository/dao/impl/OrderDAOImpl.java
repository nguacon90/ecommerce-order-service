package com.vctek.orderservice.repository.dao.impl;

import com.vctek.migration.dto.MigrateBillDto;
import com.vctek.orderservice.dto.request.OrderSearchRequest;
import com.vctek.orderservice.dto.request.storefront.CountOrderData;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.repository.dao.OrderDAO;
import com.vctek.util.DateUtil;
import com.vctek.util.OrderType;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.hibernate.type.IntegerType;
import org.hibernate.type.StringType;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Repository
public class OrderDAOImpl extends AbstractQueryDAO implements OrderDAO {

    @Override
    @Transactional
    public void updateAuditing(OrderModel model, MigrateBillDto dto) {
        StringBuilder sqlBuilder = new StringBuilder("UPDATE orders SET ");
        sqlBuilder.append(" created_by = :"+CREATED_BY+", created_time = :"+CREATION_TIME+"");
        sqlBuilder.append(" WHERE id = :"+ID);

        Query nativeQuery = entityManager.createNativeQuery(sqlBuilder.toString());
        nativeQuery.setParameter(ID, model.getId());
        nativeQuery.setParameter(CREATED_BY, dto.getCreatedBy());
        Date currentTime = Calendar.getInstance().getTime();
        Date creationTime = dto.getCreationTime() == null ? currentTime : DateUtil.parseDate(dto.getCreationTime(), DateUtil.ISO_DATE_TIME_PATTERN);
        nativeQuery.setParameter(CREATION_TIME, creationTime);
        nativeQuery.executeUpdate();

    }

    @Override
    public List<CountOrderData> storefrontCountOrderByUser(OrderSearchRequest request) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT COUNT(o.id) AS quantity, o.order_status AS orderStatus")
                .append(" FROM orders as o")
                .append(" WHERE o.company_id = :"+COMPANY_ID+" AND o.dtype = 'OrderModel' AND o.deleted = false AND o.sell_signal = :"+SELL_SIGNAL)
                .append(" AND o.created_by = :"+CREATED_BY+" AND o.order_type = :"+ORDER_TYPE)
                .append(" AND o.order_status != 'CHANGE_TO_RETAIL'")
                .append(" GROUP BY o.order_status");
        Session session = entityManager.unwrap(Session.class);
        NativeQuery query = session.createNativeQuery(sqlBuilder.toString());
        query.addScalar("quantity", IntegerType.INSTANCE)
                .addScalar("orderStatus", StringType.INSTANCE)
                .setResultTransformer(Transformers.aliasToBean(CountOrderData.class));
        query.setParameter(COMPANY_ID, request.getCompanyId());
        query.setParameter(SELL_SIGNAL, request.getSellSignal());
        query.setParameter(CREATED_BY, request.getCreatedBy());
        query.setParameter(ORDER_TYPE, OrderType.ONLINE.name());
        return query.getResultList();
    }
}
