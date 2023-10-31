package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.model.OrderSourceModel;
import com.vctek.orderservice.repository.OrderSourceRepository;
import com.vctek.orderservice.service.OrderSourceService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class OrderSourceServiceImplTest {
    private OrderSourceRepository orderSourceRepository;
    private OrderSourceService orderSourceService;
    private ArgumentCaptor<OrderSourceModel> captor;

    @Before
    public void setUp() {
        orderSourceRepository = mock(OrderSourceRepository.class);
        captor = ArgumentCaptor.forClass(OrderSourceModel.class);
        orderSourceService = new OrderSourceServiceImpl(orderSourceRepository);
    }

    @Test
    public void save() {
        OrderSourceModel orderSourceModel = new OrderSourceModel();
        orderSourceModel.setName("name");
        orderSourceModel.setCompanyId(10l);
        orderSourceService.save(orderSourceModel);

        verify(orderSourceRepository).save(captor.capture());
        OrderSourceModel actual = captor.getValue();
        assertEquals("name", actual.getName());
        assertEquals(10l, actual.getCompanyId(), 0);
    }
    @Test
    public void findById() {
        orderSourceService.findById(12l);
        verify(orderSourceRepository).findById(anyLong());
    }

    @Test
    public void findAllByCompanyId() {
        orderSourceService.findAllByCompanyId(12l);
        verify(orderSourceRepository).findAllByCompanyIdOrderByOrderAsc(anyLong());
    }

    @Test
    public void findByIdAndCompanyId() {
        orderSourceService.findByIdAndCompanyId(12l,1l);
        verify(orderSourceRepository).findByIdAndCompanyId(anyLong(),anyLong());
    }

    @Test
    public void rearrangeOrder() {
        orderSourceService.rearrangeOrder(anyList());
        verify(orderSourceRepository).saveAll(anyList());
    }
}
