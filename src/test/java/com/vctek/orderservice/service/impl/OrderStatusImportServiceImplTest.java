package com.vctek.orderservice.service.impl;

import com.vctek.kafka.data.order.OrderProcessData;
import com.vctek.orderservice.dto.OrderStatusImportDetailData;
import com.vctek.orderservice.dto.request.OrderStatusImportSearchRequest;
import com.vctek.orderservice.event.OrderStatusImportEvent;
import com.vctek.orderservice.kafka.producer.OrderProcessProducerService;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.OrderStatusImportDetailModel;
import com.vctek.orderservice.model.OrderStatusImportModel;
import com.vctek.orderservice.repository.OrderStatusImportRepository;
import com.vctek.orderservice.service.OrderService;
import com.vctek.orderservice.service.OrderStatusImportDetailService;
import com.vctek.orderservice.strategy.CommerceChangeOrderStatusStrategy;
import com.vctek.util.OrderStatus;
import com.vctek.util.OrderStatusImport;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class OrderStatusImportServiceImplTest {
    private OrderStatusImportServiceImpl service;
    private OrderStatusImportModel model;
    private OrderProcessData data;
    private OrderStatusImportDetailModel detail1;
    private OrderStatusImportDetailModel detail2;
    private List<OrderStatusImportDetailModel> detailModels;

    @Mock
    private OrderStatusImportRepository repository;
    @Mock
    private EntityManager entityManager;
    @Mock
    private Root<OrderStatusImportModel> root;
    @Mock
    private CriteriaQuery<OrderStatusImportModel> cq;
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
    private OrderService orderService;
    @Mock
    private CommerceChangeOrderStatusStrategy commerceChangeOrderStatusStrategy;
    private ArgumentCaptor<OrderStatusImportModel> captor;
    @Mock
    private OrderProcessProducerService producerService;
    @Mock
    private OrderStatusImportDetailService detailService;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new OrderStatusImportServiceImpl();
        captor = ArgumentCaptor.forClass(OrderStatusImportModel.class);
        service.setRepository(repository);
        service.setEntityManager(entityManager);
        service.setOrderService(orderService);
        service.setCommerceChangeOrderStatusStrategy(commerceChangeOrderStatusStrategy);
        service.setProducerService(producerService);
        service.setDetailService(detailService);
        service.setApplicationEventPublisher(applicationEventPublisher);
        model = new OrderStatusImportModel();
        model.setId(2L);
        model.setCompanyId(2L);
        model.setOrderStatus(OrderStatus.CONFIRMING.toString());
        detail1 = new OrderStatusImportDetailModel();
        detail1.setOrderCode("code1");
        detail1.setOrderStatusImportModel(model);
        detail2 = new OrderStatusImportDetailModel();
        detail2.setOrderCode("code2");
        detail2.setOrderStatusImportModel(model);
        detailModels = new ArrayList<>();
        detailModels.add(detail1);
        detailModels.add(detail2);
        model.setOrderStatusImportDetailModels(detailModels);
        data = new OrderProcessData();
        data.setImportOrderStatusDetailId(2L);
        data.setCompanyId(2L);
        OrderStatusImportDetailData detailData1 = new OrderStatusImportDetailData();
        detailData1.setId(1L);
        detailData1.setOrderCode("code1");
        OrderStatusImportDetailData detailData2 = new OrderStatusImportDetailData();
        detailData2.setId(2L);
        detailData2.setOrderCode("code2");
        List<OrderStatusImportDetailData> detailData = new ArrayList<>();
        detailData.add(detailData1);
        detailData.add(detailData2);
    }

    @Test
    public void save() {
        when(repository.save(any())).thenReturn(model);
        service.save(model);
        verify(repository).save(captor.capture());
        verify(orderService).updateLockOrders(anyLong(), anyList(), anyBoolean());
        verify(applicationEventPublisher).publishEvent(any(OrderStatusImportEvent.class));
    }

    @Test
    public void findByIdAndCompanyId() {
        service.findByIdAndCompanyId(anyLong(), anyLong());
        verify(repository).findByIdAndCompanyId(anyLong(), anyLong());
    }

    @Test
    public void search() {
        OrderStatusImportSearchRequest request = new OrderStatusImportSearchRequest();
        request.setCompanyId(2L);
        List<String> listStatus = new ArrayList<>();
        listStatus.add("status_1");
        listStatus.add("status_2");
        request.setStatus(listStatus);
        request.setOrderCode("20");
        request.setFromCreatedDate(new Date());
        request.setToCreatedDate(new Date());
        Pageable pageable = PageRequest.of(0, 20);

        when(entityManager.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(OrderStatusImportModel.class)).thenReturn(cq);
        when(cq.from(OrderStatusImportModel.class)).thenReturn(root);
        when(root.join(anyString())).thenReturn(modelJoin);
        when(modelJoin.get(any(String.class))).thenReturn(objectPath);
        when(root.get(any(String.class))).thenReturn(path);
        when(cb.and(any())).thenReturn(where);
        when(cq.where(where)).thenReturn(cq);
        when(entityManager.createQuery(cq)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList(model));

        Page<OrderStatusImportModel> listModels = service.search(request, pageable);

        assertEquals(1, listModels.getContent().size());
    }

    @Test
    public void changeStatusMultipleOrder_detailImport_NULL() {
        OrderModel orderModel = new OrderModel();
        orderModel.setCode("code1");
        data.setOrderCode("code1");
        orderModel.setOrderStatus(OrderStatus.PRE_ORDER.toString());
        when(detailService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(null);
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(orderModel);

        service.changeStatusMultipleOrder(data);
        verify(detailService, times(0)).save(any());
        verify(orderService, times(0)).updateLockOrder(any(), anyBoolean());
        verify(commerceChangeOrderStatusStrategy, times(0)).importChangeStatusOrder(any(), any());
        verify(detailService, times(0)).updateStatusCompletedOrderStatusImportModel(any(OrderStatusImportModel.class));
    }

    @Test
    public void changeStatusMultipleOrder() {
        OrderModel orderModel = new OrderModel();
        orderModel.setCode("code1");
        data.setOrderCode("code1");
        orderModel.setOrderStatus(OrderStatus.PRE_ORDER.toString());
        when(detailService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(detail2);
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(orderModel);
        detail2.setNewOrderStatus(OrderStatus.SHIPPING.code());

        service.changeStatusMultipleOrder(data);
        assertEquals(OrderStatusImport.PROCESSING.toString(), detail2.getStatus());
        assertNull(detail2.getNote());
        verify(detailService, times(1)).save(any());
        verify(orderService, times(0)).updateLockOrder(any(), anyBoolean());
        verify(commerceChangeOrderStatusStrategy, times(1)).importChangeStatusOrder(any(), any());
        verify(detailService, times(0)).updateStatusCompletedOrderStatusImportModel(any(OrderStatusImportModel.class));
    }

    @Test
    public void handleSendKafkaChangeOrderStatus() {
        service.handleSendKafkaChangeOrderStatus(model);
        verify(producerService, times(2)).sendOrderStatusImportKafka(any(OrderProcessData.class));
    }
}