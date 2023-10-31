package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.dto.request.CheckTotalSellingOfProductRequest;
import com.vctek.orderservice.repository.OrderEntryRepository;
import com.vctek.orderservice.service.CheckProductSellingService;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class CheckProductSellingServiceImplTest {

    private OrderEntryRepository orderEntryRepository;
    private CheckProductSellingService service;
    private CheckTotalSellingOfProductRequest request;

    @Before
    public void setUp() {
        orderEntryRepository = mock(OrderEntryRepository.class);
        request = mock(CheckTotalSellingOfProductRequest.class);
        service = new CheckProductSellingServiceImpl(orderEntryRepository);
    }

    @Test
    public void countTotalInWholeSaleAndRetail() {
        when(orderEntryRepository.getListOfProductSellingExcludeOnline(anyLong(), anyString(), anyLong(), any(Date.class))).thenReturn(20l);
        service.countTotalInWholeSaleAndRetail(request, 2l);
        verify(orderEntryRepository).getListOfProductSellingExcludeOnline(anyLong(), anyString(), anyLong(), any(Date.class));
    }

    @Test
    public void countTotalInOnline() {
        when(orderEntryRepository.getListOfProductSellingOnline(anyLong(), anyString(), anyLong(), any(Date.class), anyString())).thenReturn(20l);
        service.countTotalInOnline(request, 2l);
        verify(orderEntryRepository).getListOfProductSellingOnline(anyLong(), anyString(), anyLong(), any(Date.class), anyString());
    }
}