package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.model.OrderNoteModel;
import com.vctek.orderservice.repository.OrderNoteRepository;
import com.vctek.orderservice.service.OrderNoteService;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class OrderNoteServiceTest {
    private OrderNoteService service;
    private OrderNoteRepository repository;

    @Before
    public  void setUp() {
        repository = mock(OrderNoteRepository.class);
        service = new OrderNoteServiceImpl(repository);
    }

    @Test
    public void save() {
        service.save(new OrderNoteModel());
        verify(repository).save(any(OrderNoteModel.class));
    }

    @Test
    public void delete() {
        service.delete(new OrderNoteModel());
        verify(repository).delete(any(OrderNoteModel.class));
    }

    @Test
    public void findAllByOrderCode() {
        service.findAllByOrderCode("1234");
        verify(repository).findAllByOrderCode("1234");
    }

    @Test
    public void findById() {
        service.findById(anyLong());
        verify(repository).findById(anyLong());
    }
}
