package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.dto.request.OrderSettingCustomerSearchRequest;
import com.vctek.orderservice.kafka.producer.OrderSettingCustomerProducerService;
import com.vctek.orderservice.model.OrderSettingCustomerModel;
import com.vctek.orderservice.repository.OrderSettingCustomerRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class OrderSettingCustomerServiceImplTest {

    private OrderSettingCustomerServiceImpl service;
    private OrderSettingCustomerModel model;
    @Mock
    private Root<OrderSettingCustomerModel> root;
    @Mock
    private CriteriaQuery<OrderSettingCustomerModel> cq;
    @Mock
    private CriteriaBuilder cb;
    @Mock
    private Path path;
    @Mock
    private Path<Object> objectPath;
    @Mock
    private Predicate where;
    @Mock
    private TypedQuery typedQuery;
    @Mock
    private Join<Object, Object> modelJoin;
    @Mock
    private OrderSettingCustomerRepository repository;
    @Mock
    private EntityManager entityManager;
    @Mock
    private OrderSettingCustomerProducerService orderSettingCustomerProducer;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new OrderSettingCustomerServiceImpl(repository);
        service.setEntityManager(entityManager);
        service.setOrderSettingCustomerProducer(orderSettingCustomerProducer);
        model = new OrderSettingCustomerModel();
    }

    @Test
    public void save() {
        service.save(model);
        verify(repository).save(any(OrderSettingCustomerModel.class));
    }

    @Test
    public void findByIdAndCompanyId() {
        service.findByIdAndCompanyId(2L, 2L);
        verify(repository).findByIdAndCompanyIdAndDeleted(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    public void findByNameAndCompanyIdOrGlobal() {
        service.findAllByNameAndCompanyId("name", 2L);
        verify(repository).findAllByNameAndDeletedAndCompanyIdAndIsDefault(anyString(), anyBoolean(), anyLong(), anyBoolean());
    }

    @Test
    public void findAllBy() {
        OrderSettingCustomerSearchRequest request = new OrderSettingCustomerSearchRequest();
        request.setCompanyId(2L);
        request.setId(2L);
        request.setOrderTypes(Arrays.asList("orderType"));
        request.setName("name");

        when(entityManager.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(OrderSettingCustomerModel.class)).thenReturn(cq);
        when(cq.from(OrderSettingCustomerModel.class)).thenReturn(root);
        when(root.join(anyString())).thenReturn(modelJoin);
        when(modelJoin.get(any(String.class))).thenReturn(objectPath);
        when(root.get(any(String.class))).thenReturn(path);
        when(cb.and(any())).thenReturn(where);
        when(cq.where(where)).thenReturn(cq);
        when(cq.groupBy(anyList())).thenReturn(cq);
        when(entityManager.createQuery(cq)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList(new OrderSettingCustomerModel()));
        List<OrderSettingCustomerModel> list = service.findAllBy(request);
        assertEquals(1, list.size());
    }

    @Test
    public void findByIdAndCompanyIdAndDefault() {
        service.findByCompanyIdAndDefault(2L);
        verify(repository).findByCompanyIdAndDeletedAndIsDefault(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    public void findAllByCompanyIdAndOrderType() {
        service.findAllByCompanyIdAndOrderType(2L, "orderType");
        verify(repository).findAllByCompanyIdAndOrderType(anyLong(), anyString());
    }
}