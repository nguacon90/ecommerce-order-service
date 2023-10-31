package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.OrderSettingModel;
import com.vctek.orderservice.service.OrderSettingService;
import com.vctek.service.UserService;
import com.vctek.util.OrderStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class WebSocketServiceImplTest {
    @Mock
    private OrderSettingService orderSettingService;
    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;
    @Mock
    private UserService userService;
    private WebSocketServiceImpl service;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new WebSocketServiceImpl();
        service.setUserService(userService);
        service.setOrderSettingService(orderSettingService);
        service.setSimpMessagingTemplate(simpMessagingTemplate);
    }

    @Test
    public void sendNotificationChangeOrderStatus_notSend() {
        OrderModel orderModel = new OrderModel();
        orderModel.setCompanyId(2L);
        orderModel.setOrderStatus(OrderStatus.COMPLETED.toString());
        OrderSettingModel orderSettingModel = new OrderSettingModel();
        orderModel.setOrderStatus(OrderStatus.SHIPPING.toString());
        when(orderSettingService.findCreateNotificationChangeStatus(anyLong())).thenReturn(orderSettingModel);
        service.sendNotificationChangeOrderStatus(orderModel);
        verify(simpMessagingTemplate, times(0)).convertAndSendToUser(anyString(), anyString(), anyString());
    }

    @Test
    public void sendNotificationChangeOrderStatus() {
        OrderModel orderModel = new OrderModel();
        orderModel.setCompanyId(2L);
        orderModel.setOrderStatus(OrderStatus.COMPLETED.toString());
        OrderSettingModel orderSettingModel = new OrderSettingModel();
        orderSettingModel.setOrderStatus(OrderStatus.COMPLETED.toString());
        when(userService.getCurrentUserId()).thenReturn(2L);
        when(orderSettingService.findCreateNotificationChangeStatus(anyLong())).thenReturn(orderSettingModel);
        service.sendNotificationChangeOrderStatus(orderModel);
        verify(simpMessagingTemplate, times(1)).convertAndSendToUser(anyString(), anyString(), anyString());
    }
}

