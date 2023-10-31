package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.model.OrderStorefrontSetupModel;
import com.vctek.orderservice.repository.OrderStorefrontSetupRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;

public class OrderStorefrontSetupServiceTest {
    @Mock
    private OrderStorefrontSetupRepository repository;
    private OrderStorefrontSetupServiceImpl service;
    private OrderStorefrontSetupModel model;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new OrderStorefrontSetupServiceImpl(repository);
        model = new OrderStorefrontSetupModel();
    }

    @Test
    public void save() {
        service.save(model);
        verify(repository).save(any(OrderStorefrontSetupModel.class));
    }

    @Test
    public void findByCompanyId() {
        service.findByCompanyId(2L);
        verify(repository).findByCompanyId(anyLong());
    }
}

