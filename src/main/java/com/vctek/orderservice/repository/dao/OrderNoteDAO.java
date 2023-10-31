package com.vctek.orderservice.repository.dao;

import com.vctek.migration.dto.SyncOrderNoteData;
import com.vctek.orderservice.model.OrderNoteModel;
import com.vctek.util.DateUtil;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Calendar;
import java.util.Date;

@Repository
public class OrderNoteDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void updateAuditing(OrderNoteModel model, SyncOrderNoteData dto) {
        StringBuilder sqlBuilder = new StringBuilder("UPDATE order_note SET ");
        sqlBuilder.append(" created_by = :createdBy, created_time = :creationTime");
        sqlBuilder.append(" WHERE id = :id");

        Query nativeQuery = entityManager.createNativeQuery(sqlBuilder.toString());
        nativeQuery.setParameter("id", model.getId());
        nativeQuery.setParameter("createdBy", dto.getCreatedBy());
        Date currentTime = Calendar.getInstance().getTime();
        Date creationTime = dto.getCreatedAt() == null ? currentTime : DateUtil.parseDate(dto.getCreatedAt(), DateUtil.ISO_DATE_TIME_PATTERN);
        nativeQuery.setParameter("creationTime", creationTime);
        nativeQuery.executeUpdate();

    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}
