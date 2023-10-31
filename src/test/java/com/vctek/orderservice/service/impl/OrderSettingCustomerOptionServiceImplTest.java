package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.model.OrderSettingCustomerModel;
import com.vctek.orderservice.repository.OrderSettingCustomerOptionRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class OrderSettingCustomerOptionServiceImplTest {

    private OrderSettingCustomerOptionServiceImpl service;
    private OrderSettingCustomerModel model;
    @Mock
    private OrderSettingCustomerOptionRepository repository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new OrderSettingCustomerOptionServiceImpl(repository);
        model = new OrderSettingCustomerModel();
    }

    @Test
    public void findByOrderSettingCustomerModel() {
        service.findByOrderSettingCustomerModel(model);
        verify(repository).findByOrderSettingCustomerModelAndDeleted(any(OrderSettingCustomerModel.class), anyBoolean());
    }

    @Test
    public void findByIdAndCompanyId() {
        service.findByIdAndCompanyId(1l, 1l);
        verify(repository).findByIdAndCompanyIdAndDeleted(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    public void findAllByOrderId() {
        service.findAllByOrderId(1L);
        verify(repository).findAllByOrderId(anyLong());
    }

    @Test
    public void findAllByCompanyNotHasOrder() {
        service.findAllByCompanyNotHasOrder(1L);
        verify(repository).findAllByCompanyNotHasOrder(anyLong());
    }

    @Test
    public void findAllByCompanyIdAndIdIn() {
        service.findAllByCompanyIdAndIdIn(1L, Arrays.asList(2L));
        verify(repository).findAllByCompanyIdAndIdIn(anyLong(), anyList());
    }
}