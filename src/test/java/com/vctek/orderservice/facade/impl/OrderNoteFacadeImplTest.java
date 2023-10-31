package com.vctek.orderservice.facade.impl;

import com.vctek.converter.Converter;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.OrderNoteData;
import com.vctek.orderservice.dto.request.OrderNoteRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.OrderNoteFacade;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.OrderNoteModel;
import com.vctek.orderservice.service.OrderNoteService;
import com.vctek.orderservice.service.OrderService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class OrderNoteFacadeImplTest {
    private OrderNoteFacade orderNoteFacade;
    private OrderNoteService orderNoteService;
    private Converter<OrderNoteModel, OrderNoteData> converter;
    private ArgumentCaptor<OrderNoteModel> captor;
    private OrderNoteModel model;
    private OrderNoteRequest request;
    private OrderService orderService;

    @Before
    public void setUp() {
        model = new OrderNoteModel();
        model.setId(1l);
        orderService = mock(OrderService.class);
        captor = ArgumentCaptor.forClass(OrderNoteModel.class);
        request = new OrderNoteRequest();
        orderNoteService = mock(OrderNoteService.class);
        converter = mock(Converter.class);
        orderNoteFacade = new OrderNoteFacadeImpl(orderNoteService, converter, orderService);
        request.setCompanyId(1l);
    }

    @Test
    public void create_success() {
        request.setContent("name");
        request.setOrderCode("124432");

        OrderModel orderModel = new OrderModel();
        orderModel.setId(1l);
        orderModel.setCode("13412");
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(orderModel);
        orderNoteFacade.create(request);
        verify(orderNoteService).save(captor.capture());
    }

    @Test
    public void create_InvalidId() {
        try {
            request.setContent("name");
            request.setOrderCode("234531");
            OrderModel orderModel = new OrderModel();
            orderModel.setId(1l);
            when(orderService.findById(anyLong())).thenReturn(null);
            orderNoteFacade.create(request);
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_CODE.code(), e.getCode());
        }
    }


    @Test
    public void findAllByOrderCode() {
        orderNoteFacade.findAllByOrderCode("12351");
        verify(orderNoteService).findAllByOrderCode("12351");
    }

    @Test
    public void delete() {
        when(orderNoteService.findById(anyLong())).thenReturn(model);
        orderNoteFacade.remove(1l);
        verify(orderNoteService).delete(model);
    }

    @Test
    public void delete_InValidId() {
        try {
            when(orderNoteService.findById(anyLong())).thenReturn(null);
            orderNoteFacade.remove(1l);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_NOTE_ID.code(), e.getCode());
        }
    }
}
