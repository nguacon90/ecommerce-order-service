package com.vctek.orderservice.facade.impl;

import com.vctek.converter.Converter;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.OrderSourceData;
import com.vctek.orderservice.dto.request.OrderSourceRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.PermissionFacade;
import com.vctek.orderservice.model.OrderSourceModel;
import com.vctek.orderservice.service.OrderSourceService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class OrderSourceFacadeImplTest {
    private OrderSourceService service;
    private OrderSourceFacadeImpl facade;
    private OrderSourceRequest request;
    private OrderSourceModel model;
    private Converter<OrderSourceModel, OrderSourceData> converter;
    private Long companyId;
    @Mock
    private PermissionFacade permissionFacade;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        companyId = 1l;
        service = mock(OrderSourceService.class);
        converter = mock(Converter.class);
        request = mock(OrderSourceRequest.class);
        model = mock(OrderSourceModel.class);
        facade = new OrderSourceFacadeImpl(service,converter);
        facade.setPermissionFacade(permissionFacade);
    }

    @Test
    public void update () {
        when(model.getId()).thenReturn(11l);
        when(service.findById(anyLong())).thenReturn(model);
        facade.update(request);
        verify(service).save(any(OrderSourceModel.class));
    }


    @Test
    public void create() {
        when(model.getId()).thenReturn(11l);
        facade.create(request);
        verify(service).save(any(OrderSourceModel.class));
    }

    @Test
    public void findById_orderSourceIdNull() {
        try {
            facade.findByIdAndCompanyId(null, companyId);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.NOT_FOUND_DATA.code(), e.getCode());
        }
    }

    @Test
    public void findById_companyIdNull() {
        try {
            facade.findByIdAndCompanyId(2l, null);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.NOT_FOUND_DATA.code(), e.getCode());
        }
    }

    @Test
    public void findById_OrderSourceNotExisted() {
        try {
            when(service.findById(anyLong())).thenReturn(null);
            facade.findByIdAndCompanyId(2l, companyId);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.NOT_FOUND_DATA.code(), e.getCode());
        }
    }


    @Test
    public void findAllByCompanyId() {
        when(permissionFacade.userBelongTo(anyLong())).thenReturn(true);
        facade.findAllByCompanyId(1l);
        verify(service).findAllByCompanyId(anyLong());
    }

    @Test
    public void findAllByCompanyId_UserNotBelongToCompany() {
        when(permissionFacade.userBelongTo(anyLong())).thenReturn(false);
        facade.findAllByCompanyId(1l);
        verify(service, times(0)).findAllByCompanyId(anyLong());
    }

    @Test
    public void rearrangeOrder() {
        List<OrderSourceRequest> requests = new ArrayList<>();
        requests.add(request);
        requests.add(request);
        when(service.findById(anyLong())).thenReturn(model);
        facade.rearrangeOrder(requests);
        verify(service).rearrangeOrder(anyList());
        verify(service, times(2)).findById(anyLong());
        verify(converter).convertAll(any());
    }
}
