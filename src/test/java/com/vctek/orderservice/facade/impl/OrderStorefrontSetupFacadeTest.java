package com.vctek.orderservice.facade.impl;

import com.vctek.dto.redis.OrderStorefrontSetupData;
import com.vctek.orderservice.model.OrderStorefrontSetupModel;
import com.vctek.orderservice.service.OrderStorefrontSetupService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class OrderStorefrontSetupFacadeTest {
    private OrderStorefrontSetupFacadeImpl facade;
    private OrderStorefrontSetupData request;
    @Mock
    private ArgumentCaptor<OrderStorefrontSetupModel> captor;
    @Mock
    private OrderStorefrontSetupService service;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        captor = ArgumentCaptor.forClass(OrderStorefrontSetupModel.class);
        facade = new OrderStorefrontSetupFacadeImpl();
        facade.setOrderStorefrontSetupService(service);
        request = new OrderStorefrontSetupData();
        request.setCompanyId(2L);
        request.setWarehouseId(2L);
    }

    @Test
    public void create() {
        when(service.findByCompanyId(anyLong())).thenReturn(null);
        when(service.save(any(OrderStorefrontSetupModel.class))).thenReturn(new OrderStorefrontSetupModel());
        facade.createOrUpdate(request);
        verify(service).save(captor.capture());
        OrderStorefrontSetupModel model = captor.getValue();
        assertEquals(2L, model.getWarehouseId(), 0);
        assertEquals(2L, model.getCompanyId(), 0);
    }

    @Test
    public void update() {
        OrderStorefrontSetupModel model = new OrderStorefrontSetupModel();
        model.setId(1L);
        model.setCompanyId(2L);
        model.setWarehouseId(120L);
        when(service.findByCompanyId(anyLong())).thenReturn(model);
        when(service.save(any(OrderStorefrontSetupModel.class))).thenReturn(new OrderStorefrontSetupModel());
        facade.createOrUpdate(request);
        verify(service).save(captor.capture());
        assertEquals(2L, model.getWarehouseId(), 0);
        assertEquals(2L, model.getCompanyId(), 0);
    }

    @Test
    public void findByCompanyId_empty() {
        when(service.findByCompanyId(anyLong())).thenReturn(null);
        OrderStorefrontSetupData data = facade.findByCompanyId(2L);
        verify(service).findByCompanyId(anyLong());
        assertNull(data);
    }

    @Test
    public void findByCompanyId() {
        OrderStorefrontSetupModel model = new OrderStorefrontSetupModel();
        model.setCompanyId(2L);
        model.setWarehouseId(2L);
        when(service.findByCompanyId(anyLong())).thenReturn(model);
        OrderStorefrontSetupData data = facade.findByCompanyId(2L);
        assertEquals(2L, data.getCompanyId(), 0);
        assertEquals(2L, data.getWarehouseId(), 0);
    }
}
