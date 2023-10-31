package com.vctek.orderservice.repository.dao.impl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class AbstractQueryDAO {
    protected EntityManager entityManager;
    public static final String ID = "id";
    public static final String COMPANY_ID = "companyId";
    public static final String CREATED_BY = "createdBy";
    public static final String CREATION_TIME = "creationTime";
    public static final String SELL_SIGNAL = "sellSignal";
    public static final String ORDER_TYPE = "orderType";

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}
