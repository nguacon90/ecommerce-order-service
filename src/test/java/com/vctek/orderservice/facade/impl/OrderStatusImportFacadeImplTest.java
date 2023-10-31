package com.vctek.orderservice.facade.impl;

import com.vctek.converter.Converter;
import com.vctek.converter.Populator;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.OrderStatusImportData;
import com.vctek.orderservice.dto.request.OrderStatusImportRequest;
import com.vctek.orderservice.dto.request.OrderStatusImportSearchRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.OrderStatusImportModel;
import com.vctek.orderservice.service.OrderStatusImportDetailService;
import com.vctek.orderservice.service.OrderStatusImportService;
import com.vctek.util.OrderStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class OrderStatusImportFacadeImplTest {
    private OrderStatusImportFacadeImpl facade;
    private OrderStatusImportModel model;
    private OrderStatusImportRequest request;
    private OrderStatusImportSearchRequest searchRequest;

    @Mock
    private OrderStatusImportService service;
    @Mock
    Converter<OrderStatusImportModel, OrderStatusImportData> converter;
    @Mock
    private Populator<OrderStatusImportRequest, OrderStatusImportModel> modelPopulator;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        facade = new OrderStatusImportFacadeImpl();
        facade.setConverter(converter);
        facade.setService(service);
        facade.setModelPopulator(modelPopulator);
        request = new OrderStatusImportRequest();
        request.setCompanyId(1L);
        request.setOrderStatus(OrderStatus.NEW.code());
        List<String> orderCodes = new ArrayList<>();
        orderCodes.add("PENDING");
        request.setOrderCodes(orderCodes);
        model = new OrderStatusImportModel();
    }

    @Test
    public void createStatusImport() {
        when(service.save(any())).thenReturn(model);
        facade.createStatusImport(request);
        verify(modelPopulator).populate(any(OrderStatusImportRequest.class), any(OrderStatusImportModel.class));
        verify(converter).convert(any(OrderStatusImportModel.class));
        verify(service, times(1)).save(any(OrderStatusImportModel.class));
    }

    @Test
    public void findByIdAndCompanyId() {
        when(service.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(model);
        facade.findByIdAndCompanyId(1L, 1L);
        verify(service).findByIdAndCompanyId(anyLong(), anyLong());
        verify(converter).convert(any(OrderStatusImportModel.class));
    }

    @Test
    public void findByIdAndCompanyId_null() {
        try {
            when(service.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(null);
            facade.findByIdAndCompanyId(1L, 1L);
            fail("throw new Exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_STATUS_IMPORT_DETAIL_ID.message(), e.getMessage());
        }
    }

    @Test
    public void search() {
        Pageable pageable = PageRequest.of(0, 20);
        when(service.search(any(), any())).thenReturn(new PageImpl<>(Arrays.asList(model), pageable, 1));
        facade.search(searchRequest, pageable);
        verify(service).search(any(), any());
        verify(converter).convertAll(anyList());
    }
}