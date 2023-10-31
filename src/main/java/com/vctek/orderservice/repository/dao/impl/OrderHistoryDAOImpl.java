package com.vctek.orderservice.repository.dao.impl;

import com.vctek.migration.dto.MigrateOrderHistoryDto;
import com.vctek.orderservice.model.OrderHistoryModel;
import com.vctek.orderservice.repository.dao.OrderHistoryDAO;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Repository
public class OrderHistoryDAOImpl implements OrderHistoryDAO {

    private EntityManager entityManager;

    @Override
    @Transactional
    public void updateAuditing(OrderHistoryModel model, MigrateOrderHistoryDto dto) {
        StringBuilder sqlBuilder = new StringBuilder("UPDATE order_history ");
        sqlBuilder.append("SET modified_time = :modifiedTime ");
        sqlBuilder.append("WHERE id = :id");
        Query nativeQuery = entityManager.createNativeQuery(sqlBuilder.toString());
        nativeQuery.setParameter("id", model.getId());
        nativeQuery.setParameter("modifiedTime", dto.getModifiedTime());
        nativeQuery.executeUpdate();
    }

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}
