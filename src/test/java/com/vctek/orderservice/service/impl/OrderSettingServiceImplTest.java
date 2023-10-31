package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.model.OrderSettingModel;
import com.vctek.orderservice.repository.OrderSettingRepository;
import com.vctek.orderservice.util.OrderSettingType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class OrderSettingServiceImplTest {
    private OrderSettingRepository repository;
    private OrderSettingServiceImpl service;
    private ArgumentCaptor<OrderSettingModel> captor;

    @Before
    public void setUp() {
        repository = mock(OrderSettingRepository.class);
        service = new OrderSettingServiceImpl(repository);
        captor = ArgumentCaptor.forClass(OrderSettingModel.class);
    }

    @Test
    public void save() {
        OrderSettingModel model = new OrderSettingModel();
        model.setCompanyId(2l);
        model.setType(OrderSettingType.MAXIMUM_DISCOUNT_SETTING.toString());
        service.save(model);

        verify(repository).save(captor.capture());
        OrderSettingModel actual = captor.getValue();
        assertEquals(2l, actual.getCompanyId(), 0);
    }

    @Test
    public void findByTypeAndCompanyId() {
        service.findByTypeAndCompanyId(OrderSettingType.MAXIMUM_DISCOUNT_SETTING.toString(), 1l);
        verify(repository).findByTypeAndCompanyId(anyString(), anyLong());
    }
}
