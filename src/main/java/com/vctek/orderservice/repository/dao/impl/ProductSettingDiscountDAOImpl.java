package com.vctek.orderservice.repository.dao.impl;

import com.vctek.orderservice.dto.OrderDiscountSettingMapper;
import com.vctek.orderservice.model.OrderSettingDiscountModel;
import com.vctek.orderservice.repository.dao.OrderSettingDiscountDAO;
import org.apache.commons.lang.text.StrBuilder;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.hibernate.type.DoubleType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ProductSettingDiscountDAOImpl implements OrderSettingDiscountDAO {
    protected EntityManager entityManager;
    public static final String PRODUCT_ID = "productId";
    public static final String COMPANY_ID = "companyId";
    public static final String DISCOUNT = "discount";
    public static final String DISCOUNT_TYPE = "discountType";

    @Override
    public List<OrderDiscountSettingMapper> findAllBy(Long companyId, Pageable pageable) {
        StringBuilder sql = new StringBuilder("SELECT product_id as productId, discount, ")
                .append("case discount_type when 'PERCENT' then '%' else 'VND' end as discountType ")
                .append("FROM order_setting_discount ")
                .append("WHERE company_id = :" + COMPANY_ID + " ")
                .append("AND deleted = false AND product_id IS NOT NULL ")
                .append("ORDER BY id DESC");
        Session session = entityManager.unwrap(Session.class);
        NativeQuery query = session.createNativeQuery(sql.toString());
        query.addScalar(PRODUCT_ID, LongType.INSTANCE)
                .addScalar(DISCOUNT, DoubleType.INSTANCE)
                .addScalar(DISCOUNT_TYPE, StringType.INSTANCE)
                .setResultTransformer(Transformers.aliasToBean(OrderDiscountSettingMapper.class));
        query.setParameter(COMPANY_ID, companyId);
        int offset = pageable.getPageNumber() * pageable.getPageSize();
        query.setFirstResult(offset);
        query.setMaxResults(pageable.getPageSize());

        return query.getResultList();
    }

    @Override
    public Page<OrderSettingDiscountModel> findAllProductSetting(Long companyId, String product, Pageable pageRequest) {
        try {
            StrBuilder sqlBuilder = new StrBuilder();
            String sql = "SELECT * FROM order_setting_discount as osd ";
            sqlBuilder.append(sql);
            sqlBuilder.append(" WHERE osd.company_id = :companyId" )
                    .append(" AND osd.deleted = false AND osd.product_id IS NOT NULL ");
            if (StringUtils.isNotEmpty(product)) {
                sqlBuilder.append(" AND osd.product_id = :productId");
            }
            sqlBuilder.append(" ORDER BY osd.id DESC");

            Query query = entityManager.createNativeQuery(sqlBuilder.toString(), OrderSettingDiscountModel.class);
            int offset = pageRequest.getPageNumber() * pageRequest.getPageSize();
            query.setFirstResult(offset);
            query.setMaxResults(pageRequest.getPageSize());
            setQueryParam(query, companyId, product);

            List<OrderSettingDiscountModel> productStorageModels = query.getResultList();

            sqlBuilder.replaceAll(sql, "SELECT COUNT(osd.id) FROM order_setting_discount as osd ");
            Query countQuery = entityManager.createNativeQuery(sqlBuilder.toString());

            setQueryParam(countQuery, companyId, product);
            Number totalRecord = (Number) countQuery.getSingleResult();
            return new PageImpl<>(productStorageModels, pageRequest, totalRecord.longValue());
        } catch (NoResultException | ClassCastException e) {
            return new PageImpl<>(new ArrayList<>());
        }
    }


    private void setQueryParam(Query query, Long companyId, String product) {
        query.setParameter(COMPANY_ID, companyId);

        if (StringUtils.isNotBlank(product)) {
            query.setParameter(PRODUCT_ID, product);
        }
    }


    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}
