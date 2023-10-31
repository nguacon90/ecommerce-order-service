package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.dto.request.CustomerRequest;
import com.vctek.orderservice.feignclient.CustomerClient;
import com.vctek.orderservice.feignclient.dto.CustomerData;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.service.ModelService;
import com.vctek.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class CustomerServiceImplTest {
    @Mock
    private CustomerClient customerClient;
    @Mock
    private ModelService modelService;
    @Mock
    private UserService userService;
    private CustomerServiceImpl customerService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        customerService = new CustomerServiceImpl(customerClient);
        customerService.setModelService(modelService);
        customerService.setUserService(userService);
    }

    @Test
    public void getCustomerById_null() {
        when(customerClient.searchCustomerByIds(anyLong(), anyString())).thenReturn(new ArrayList<>());
        customerService.getCustomerById(1l, 1l);
        verify(customerClient).searchCustomerByIds( anyLong(), anyString());
    }

    @Test
    public void getCustomerById() {
        when(customerClient.searchCustomerByIds(anyLong(), anyString())).thenReturn(Arrays.asList(new CustomerData()));
        customerService.getCustomerById(1l, 1l);
        verify(customerClient).searchCustomerByIds( anyLong(), anyString());
    }

    @Test
    public void getBasicCustomerInfo() {
        customerService.getBasicCustomerInfo( 1l, 2l);
        verify(customerClient).getBasicCustomerInfo(1l, 2L);
    }

    @Test
    public void update_null() {
        OrderModel order = new OrderModel();
        customerService.update( order, null);
        verify(customerClient, times(0)).createNew(any());
        verify(modelService, times(0)).save(any());
    }

    @Test
    public void update() {
        OrderModel order = new OrderModel();
        CustomerData customerData = new CustomerData();
        customerData.setId(2L);
        CustomerRequest request = new CustomerRequest();
        request.setName("name");
        request.setPhone("phone");
        when(customerClient.createNew(any())).thenReturn(customerData);
        customerService.update( order, request);
        verify(customerClient, times(1)).createNew(any());
        verify(modelService, times(1)).save(any());
    }

    @Test
    public void limitedApplyPromotionAndReward_customerId_null() {
        boolean isLimitedApplyPromotionAndReward = customerService.limitedApplyPromotionAndReward(null, 2L);
        assertFalse(isLimitedApplyPromotionAndReward);
        verify(customerClient, times(0)).getBasicCustomerInfo(anyLong(), anyLong());
    }

    @Test
    public void limitedApplyPromotionAndReward_customer_null() {
        when(customerClient.getBasicCustomerInfo(anyLong(), anyLong())).thenReturn(null);
        boolean isLimitedApplyPromotionAndReward = customerService.limitedApplyPromotionAndReward(2L, 2L);
        assertFalse(isLimitedApplyPromotionAndReward);
        verify(customerClient, times(1)).getBasicCustomerInfo(anyLong(), anyLong());
    }

    @Test
    public void limitedApplyPromotionAndReward() {
        CustomerData customerData = new CustomerData();
        customerData.setLimitedApplyPromotionAndReward(true);
        when(customerClient.getBasicCustomerInfo(anyLong(), anyLong())).thenReturn(customerData);
        boolean isLimitedApplyPromotionAndReward = customerService.limitedApplyPromotionAndReward(2L, 2L);
        assertTrue(isLimitedApplyPromotionAndReward);
        verify(customerClient, times(1)).getBasicCustomerInfo(anyLong(), anyLong());
    }
}
