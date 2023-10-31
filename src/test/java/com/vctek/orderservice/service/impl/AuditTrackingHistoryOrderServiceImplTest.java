package com.vctek.orderservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vctek.converter.Converter;
import com.vctek.converter.Populator;
import com.vctek.kafka.data.OrderData;
import com.vctek.orderservice.dto.TrackingHistoryOrderData;
import com.vctek.orderservice.dto.TrackingOrderData;
import com.vctek.orderservice.model.TrackingUpdateOrderModel;
import com.vctek.orderservice.repository.TrackingUpdateOrderRepository;
import com.vctek.orderservice.service.OrderHistoryService;
import com.vctek.orderservice.service.OrderService;
import com.vctek.orderservice.service.TrackingUpdateOrderService;
import com.vctek.util.OrderStatus;
import com.vctek.util.OrderType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AuditTrackingHistoryOrderServiceImplTest {
    @Mock
    private TrackingUpdateOrderService trackingUpdateOrderService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private Populator<List<TrackingOrderData>, List<TrackingHistoryOrderData>> trackingUpdateOrderPopulator;
    @Mock
    private OrderHistoryService orderHistoryService;
    @Mock
    private OrderService orderService;
    @Mock
    private Converter<OrderData, TrackingOrderData> trackingOrderDataConverter;
    private AuditTrackingHistoryOrderServiceImpl service;
    private OrderData orderData;
    private TrackingUpdateOrderModel model;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new AuditTrackingHistoryOrderServiceImpl();
        service.setObjectMapper(objectMapper);
        service.setOrderHistoryService(orderHistoryService);
        service.setOrderService(orderService);
        service.setTrackingOrderDataConverter(trackingOrderDataConverter);
        service.setTrackingUpdateOrderPopulator(trackingUpdateOrderPopulator);
        service.setTrackingUpdateOrderService(trackingUpdateOrderService);
        orderData = new OrderData();
        orderData.setOrderCode("code");
        orderData.setCompanyId(2L);
        orderData.setCustomerNote("note");
        orderData.setOrderType(OrderType.ONLINE.toString());
        orderData.setOrderStatus(OrderStatus.CONFIRMED.toString());
        model = new TrackingUpdateOrderModel();
        model.setOrderCode("code");
        model.setContent("content");
    }

    @Test
    public void compareChangeFields_create() {
        when(trackingUpdateOrderService.findByOrderCode(anyString())).thenReturn(null);
        when(trackingOrderDataConverter.convert(any(OrderData.class))).thenReturn(new TrackingOrderData());
        service.compareChangeFields(orderData);
        verify(trackingUpdateOrderService).createNew(any(OrderData.class), any(TrackingOrderData.class));
    }

    @Test
    public void compareChangeFields_update() throws IOException {
        TrackingOrderData orderData1 = new TrackingOrderData();
        orderData1.setOrderCode("code");
        orderData1.setCompanyId(2L);
        orderData1.setCustomerNote("note123");
        when(trackingUpdateOrderService.findByOrderCode(anyString())).thenReturn(model);
        when(objectMapper.readValue(anyString(), eq(TrackingOrderData.class))).thenReturn(orderData1);
        when(trackingOrderDataConverter.convert(any(OrderData.class))).thenReturn(new TrackingOrderData());
        service.compareChangeFields(orderData);
        verify(trackingUpdateOrderService).updateModel(any(OrderData.class), any(TrackingOrderData.class));
    }
}

