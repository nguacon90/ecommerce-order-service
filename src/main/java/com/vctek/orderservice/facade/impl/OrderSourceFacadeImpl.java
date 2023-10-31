package com.vctek.orderservice.facade.impl;

import com.vctek.converter.Converter;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.OrderSourceData;
import com.vctek.orderservice.dto.request.OrderSourceRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.OrderSourceFacade;
import com.vctek.orderservice.facade.PermissionFacade;
import com.vctek.orderservice.model.OrderSourceModel;
import com.vctek.orderservice.service.OrderSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrderSourceFacadeImpl implements OrderSourceFacade {
    private OrderSourceService orderSourceService;
    private PermissionFacade permissionFacade;
    private Converter<OrderSourceModel, OrderSourceData> orderSourceDataConverter;

    @Autowired
    public OrderSourceFacadeImpl(OrderSourceService orderSourceService, Converter<OrderSourceModel, OrderSourceData> orderSourceDataConverter) {
        this.orderSourceService = orderSourceService;
        this.orderSourceDataConverter = orderSourceDataConverter;
    }


    protected void populateModel(OrderSourceRequest orderSourceRequest, OrderSourceModel orderSourceModel) {
        orderSourceModel.setName(orderSourceRequest.getName());
        orderSourceModel.setCompanyId(orderSourceRequest.getCompanyId());
        orderSourceModel.setDescription(orderSourceRequest.getDescription());
        orderSourceModel.setTransactionName(orderSourceRequest.getTransactionName());
    }
    @Override
    public OrderSourceData create(OrderSourceRequest orderSourceRequest) {
        OrderSourceModel orderSourceModel = new OrderSourceModel();
        populateModel(orderSourceRequest, orderSourceModel);
        orderSourceModel = orderSourceService.save(orderSourceModel);
        return orderSourceDataConverter.convert(orderSourceModel);
    }

    @Override
    public OrderSourceData update(OrderSourceRequest orderSourceRequest) {
        OrderSourceModel orderSourceModel = orderSourceService.findById(orderSourceRequest.getId());
        populateModel(orderSourceRequest, orderSourceModel);
        orderSourceModel = orderSourceService.save(orderSourceModel);
        return orderSourceDataConverter.convert(orderSourceModel);
    }


    @Override
    public OrderSourceData findByIdAndCompanyId(Long orderSourceId, Long companyId) {
        if (orderSourceId == null || companyId == null) {
            ErrorCodes err = ErrorCodes.NOT_FOUND_DATA;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        OrderSourceModel orderSourceModel = orderSourceService.findByIdAndCompanyId(orderSourceId, companyId);
        if (orderSourceModel == null) {
            ErrorCodes err = ErrorCodes.NOT_FOUND_DATA;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        return orderSourceDataConverter.convert(orderSourceModel);
    }

    @Override
    public List<OrderSourceData> findAllByCompanyId(Long companyId) {
        boolean companyHasUser = permissionFacade.userBelongTo(companyId);
        if(!companyHasUser) {
            return new ArrayList<>();
        }
        List<OrderSourceModel> orderSourceModels = orderSourceService.findAllByCompanyId(companyId);
        return orderSourceDataConverter.convertAll(orderSourceModels);
    }

    @Override
    public List<OrderSourceData> rearrangeOrder(List<OrderSourceRequest> requests) {
        List<OrderSourceModel> orderSourceModels = new ArrayList<>();
        for (OrderSourceRequest orderSourceRequest: requests) {
            OrderSourceModel orderSourceModel = orderSourceService.findById(orderSourceRequest.getId());
            orderSourceModel.setOrder(orderSourceRequest.getOrder());
            orderSourceModels.add(orderSourceModel);
        }
        List<OrderSourceModel> savedModel = orderSourceService.rearrangeOrder(orderSourceModels);
        return orderSourceDataConverter.convertAll(savedModel);
    }

    @Autowired
    public void setPermissionFacade(PermissionFacade permissionFacade) {
        this.permissionFacade = permissionFacade;
    }
}
