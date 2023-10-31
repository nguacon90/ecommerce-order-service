package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.dto.request.OrderSettingCustomerSearchRequest;
import com.vctek.orderservice.kafka.producer.OrderSettingCustomerProducerService;
import com.vctek.orderservice.model.OrderSettingCustomerModel;
import com.vctek.orderservice.model.OrderTypeSettingCustomerModel;
import com.vctek.orderservice.repository.OrderSettingCustomerRepository;
import com.vctek.orderservice.service.OrderSettingCustomerService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderSettingCustomerServiceImpl implements OrderSettingCustomerService {
    private static final String COMPANY_ID = "companyId";
    private static final String DELETED = "deleted";
    private static final String NAME = "name";
    private static final String ID = "id";
    private static final String IS_DEFAULT = "isDefault";
    private static final String ORDER_TYPE = "orderType";
    private static final String LIKE_PATTERN = "%{0}%";
    private OrderSettingCustomerRepository repository;
    private EntityManager entityManager;
    private OrderSettingCustomerProducerService orderSettingCustomerProducer;

    public OrderSettingCustomerServiceImpl(OrderSettingCustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    public OrderSettingCustomerModel save(OrderSettingCustomerModel model) {
        OrderSettingCustomerModel savedModel = repository.save(model);
        orderSettingCustomerProducer.createOrUpdateOrderSettingCustomer(savedModel);
        return savedModel;
    }

    @Override
    public OrderSettingCustomerModel findByIdAndCompanyId(Long id, Long companyId) {
        return repository.findByIdAndCompanyIdAndDeleted(id, companyId, false);
    }

    @Override
    public List<OrderSettingCustomerModel> findAllByNameAndCompanyId(String name, Long companyId) {
        return repository.findAllByNameAndDeletedAndCompanyIdAndIsDefault(name, false, companyId, false);
    }

    @Override
    public List<OrderSettingCustomerModel> findAllBy(OrderSettingCustomerSearchRequest request) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<OrderSettingCustomerModel> cq = cb.createQuery(OrderSettingCustomerModel.class);
        Root<OrderSettingCustomerModel> root = cq.from(OrderSettingCustomerModel.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get(DELETED), false));
        if (Boolean.FALSE.equals(request.isDefault())) {
            predicates.add(cb.equal(root.get(COMPANY_ID), request.getCompanyId()));
            predicates.add(cb.equal(root.get(IS_DEFAULT), request.isDefault()));
        } else {
            predicates.add(cb.or(cb.equal(root.get(COMPANY_ID), request.getCompanyId()),
                    cb.equal(root.get(IS_DEFAULT), request.isDefault())));
        }

        if (StringUtils.isNotBlank(request.getName())) {
            predicates.add(cb.like(root.get(NAME), MessageFormat.format(LIKE_PATTERN, request.getName())));
        }

        if (CollectionUtils.isNotEmpty(request.getOrderTypes())) {
            Join<OrderSettingCustomerModel, OrderTypeSettingCustomerModel> modelJoin = root.join("orderTypeSettingCustomerModels");
            predicates.add(cb.or(modelJoin.get(ORDER_TYPE).in(request.getOrderTypes())));
        }

        if (request.getId() != null) {
            predicates.add(cb.equal(root.get(ID), request.getId()));
        }

        cq.orderBy(cb.desc(root.get(ID)));
        cq.where(cb.and(predicates.toArray(new Predicate[0])));
        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public OrderSettingCustomerModel findByCompanyIdAndDefault(Long companyId) {
        return repository.findByCompanyIdAndDeletedAndIsDefault(companyId, false, true);
    }

    @Override
    public List<OrderSettingCustomerModel> findAllByCompanyIdAndOrderType(Long companyId, String orderType) {
        return repository.findAllByCompanyIdAndOrderType(companyId, orderType);
    }

    @Autowired
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Autowired
    public void setOrderSettingCustomerProducer(OrderSettingCustomerProducerService orderSettingCustomerProducer) {
        this.orderSettingCustomerProducer = orderSettingCustomerProducer;
    }
}
