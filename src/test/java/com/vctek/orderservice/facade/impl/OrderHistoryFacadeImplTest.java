package com.vctek.orderservice.facade.impl;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.UserData;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.OrderHistoryFacade;
import com.vctek.orderservice.model.OrderHistoryModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.service.AuthService;
import com.vctek.orderservice.service.OrderHistoryService;
import com.vctek.orderservice.service.OrderService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OrderHistoryFacadeImplTest {

    @Mock
    private OrderService orderService;

    @Mock
    private OrderHistoryService orderHistoryService;

    @Mock
    private AuthService authService;

    private OrderHistoryFacade facade;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        facade = new OrderHistoryFacadeImpl(orderHistoryService, orderService, authService);
    }

    @Test
    public void getStatusHistory_case1() {
        try {
            facade.getStatusHistory(null, 1l);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_CODE.code(), e.getCode());
        }
    }

    @Test
    public void getStatusHistory_case2() {
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(new OrderModel());
        when(orderHistoryService.findAllByOrder(any(OrderModel.class))).thenReturn(Collections.emptyList());
        facade.getStatusHistory("code", 1l);
        verify(orderHistoryService).findAllByOrder(any(OrderModel.class));
    }

    @Test
    public void getStatusHistory_case3() {
        OrderHistoryModel model = new OrderHistoryModel();
        model.setModifiedBy(1l);
        model.setModifiedTime(Calendar.getInstance().getTime());
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(new OrderModel());
        when(orderHistoryService.findAllByOrder(any(OrderModel.class))).thenReturn(Arrays.asList(model));
        when(authService.getUserById(anyLong())).thenReturn(new UserData());
        facade.getStatusHistory("code", 1l);
        verify(orderHistoryService).findAllByOrder(any(OrderModel.class));
    }
}