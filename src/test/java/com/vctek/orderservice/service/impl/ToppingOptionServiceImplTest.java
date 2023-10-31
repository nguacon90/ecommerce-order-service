package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.model.ToppingOptionModel;
import com.vctek.orderservice.repository.ToppingOptionRepository;
import com.vctek.orderservice.service.ToppingOptionService;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ToppingOptionServiceImplTest {

    private ToppingOptionRepository repository;
    private ToppingOptionService service;

    @Before
    public void setUp() {
        repository = mock(ToppingOptionRepository.class);
        service = new ToppingOptionServiceImpl(repository);
    }

    @Test
    public void findById() {
        service.findById(1l);
        verify(repository).findById(anyLong());
    }

    @Test
    public void save() {
        service.save(new ToppingOptionModel());
        verify(repository).save(any(ToppingOptionModel.class));
    }

    @Test
    public void delete() {
        service.delete(new ToppingOptionModel());
        verify(repository).delete(any(ToppingOptionModel.class));
    }
}
