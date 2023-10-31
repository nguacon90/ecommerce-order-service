package com.vctek.orderservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vctek.kafka.data.OrderData;
import com.vctek.orderservice.dto.TrackingOrderData;
import com.vctek.orderservice.model.TrackingUpdateOrderModel;
import com.vctek.orderservice.repository.TrackingUpdateOrderRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TrackingUpdateOrderServiceImplTest {
    @Mock
    private TrackingUpdateOrderRepository repository;
    @Mock
    private ObjectMapper objectMapper;
    private TrackingUpdateOrderServiceImpl service;
    private TrackingOrderData trackingOrderData;
    private OrderData orderData;
    private TrackingUpdateOrderModel model;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new TrackingUpdateOrderServiceImpl(repository);
        service.setObjectMapper(objectMapper);
        model = new TrackingUpdateOrderModel();
        trackingOrderData = new TrackingOrderData();
        orderData = new OrderData();
        orderData.setOrderCode("code");
    }

    @Test
    public void createNew() {
        service.createNew(orderData, trackingOrderData);
        verify(repository).save(any(TrackingUpdateOrderModel.class));
    }

    @Test
    public void updateModel() {
        when(repository.findDistinctTopByOrderCode(anyString())).thenReturn(model);
        service.updateModel(orderData, trackingOrderData);
        verify(repository).save(any(TrackingUpdateOrderModel.class));
    }

    @Test
    public void findByOrderCode() {
        when(repository.findDistinctTopByOrderCode(anyString())).thenReturn(model);
        service.findByOrderCode("code");
        verify(repository).findDistinctTopByOrderCode(anyString());
    }
}

