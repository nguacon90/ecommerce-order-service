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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderNoteFacadeImpl implements OrderNoteFacade {
    private OrderNoteService orderNoteService;
    private Converter<OrderNoteModel, OrderNoteData> orderNoteDataConverter;
    private OrderService orderService;

    @Autowired
    public OrderNoteFacadeImpl(OrderNoteService orderNoteService, Converter<OrderNoteModel, OrderNoteData> orderNoteDataConverter, OrderService orderService) {
        this.orderNoteService = orderNoteService;
        this.orderNoteDataConverter = orderNoteDataConverter;
        this.orderService = orderService;
    }

    protected void populateModel(OrderNoteRequest orderNoteRequest, OrderNoteModel orderNoteModel) {
        orderNoteModel.setContent(orderNoteRequest.getContent());
        OrderModel orderModel = orderService.findByCodeAndCompanyId(orderNoteRequest.getOrderCode(), orderNoteRequest.getCompanyId());
        if(orderModel == null) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_CODE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        orderNoteModel.setOrder(orderModel);
        orderNoteModel.setOrderCode(orderModel.getCode());

    }
    @Override
    public OrderNoteData create(OrderNoteRequest orderNoteRequest) {
        OrderNoteModel orderNoteModel = new OrderNoteModel();
        populateModel(orderNoteRequest, orderNoteModel);
        OrderNoteModel saveOrderNote = orderNoteService.save(orderNoteModel);
        return orderNoteDataConverter.convert(saveOrderNote);
    }

    @Override
    public List<OrderNoteData> findAllByOrderCode(String orderCode) {
        List<OrderNoteModel> orderNoteModel = orderNoteService.findAllByOrderCode(orderCode);
        return orderNoteDataConverter.convertAll(orderNoteModel);
    }

    @Override
    public void remove(Long orderNoteId) {
        OrderNoteModel orderNoteModel = orderNoteService.findById(orderNoteId);
        if (orderNoteModel == null) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_NOTE_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        orderNoteService.delete(orderNoteModel);
    }
}
