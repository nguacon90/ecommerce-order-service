package com.vctek.orderservice.repository.dao;

import com.vctek.migration.dto.MigrateBillDto;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.ReturnOrderModel;
import com.vctek.util.DateUtil;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Calendar;
import java.util.Date;

@Repository
public class ReturnOrderDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void updateAuditing(ReturnOrderModel model, MigrateBillDto dto) {
        StringBuilder sqlBuilder = new StringBuilder("UPDATE return_order SET ");
        sqlBuilder.append(" created_by = :createdBy, created_time = :creationTime");
        sqlBuilder.append(" WHERE id = :id");

        Query nativeQuery = entityManager.createNativeQuery(sqlBuilder.toString());
        nativeQuery.setParameter("id", model.getId());
        nativeQuery.setParameter("createdBy", dto.getCreatedBy());
        Date currentTime = Calendar.getInstance().getTime();
        Date creationTime = dto.getCreationTime() == null ? currentTime : DateUtil.parseDate(dto.getCreationTime(), DateUtil.ISO_DATE_TIME_PATTERN);
        nativeQuery.setParameter("creationTime", creationTime);
        nativeQuery.executeUpdate();

    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}
