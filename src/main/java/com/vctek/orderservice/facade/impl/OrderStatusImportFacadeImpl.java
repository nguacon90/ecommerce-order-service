package com.vctek.orderservice.facade.impl;

import com.vctek.converter.Converter;
import com.vctek.converter.Populator;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.OrderStatusImportData;
import com.vctek.orderservice.dto.request.OrderStatusImportRequest;
import com.vctek.orderservice.dto.request.OrderStatusImportSearchRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.OrderStatusImportFacade;
import com.vctek.orderservice.model.OrderStatusImportModel;
import com.vctek.orderservice.service.OrderStatusImportService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrderStatusImportFacadeImpl implements OrderStatusImportFacade {
    private OrderStatusImportService service;
    private Populator<OrderStatusImportRequest, OrderStatusImportModel> orderStatusImportModelPopulator;
    private Converter<OrderStatusImportModel, OrderStatusImportData> orderStatusImportDataConverter;

    @Override
    public OrderStatusImportData createStatusImport(OrderStatusImportRequest request) {
        OrderStatusImportModel model = new OrderStatusImportModel();
        orderStatusImportModelPopulator.populate(request, model);
        OrderStatusImportModel savedModel = service.save(model);
        return orderStatusImportDataConverter.convert(savedModel);
    }

    @Override
    public OrderStatusImportData findByIdAndCompanyId(Long id, Long companyId) {
        OrderStatusImportModel model = service.findByIdAndCompanyId(id, companyId);
        if (model == null) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_STATUS_IMPORT_DETAIL_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        return orderStatusImportDataConverter.convert(model);
    }

    @Override
    public Page<OrderStatusImportData> search(OrderStatusImportSearchRequest request, Pageable pageable) {
        Page<OrderStatusImportModel> models = service.search(request, pageable);
        List<OrderStatusImportData> data = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(models.getContent())) {
            data = orderStatusImportDataConverter.convertAll(models.getContent());
        }
        return new PageImpl<>(data, models.getPageable(), models.getTotalElements());
    }


    @Autowired
    public void setService(OrderStatusImportService service) { this.service = service; }

    @Autowired
    public void setModelPopulator(Populator<OrderStatusImportRequest, OrderStatusImportModel> orderStatusImportModelPopulator) {
        this.orderStatusImportModelPopulator = orderStatusImportModelPopulator;
    }

    @Autowired
    public void setConverter(Converter<OrderStatusImportModel, OrderStatusImportData> orderStatusImportDataConverter) {
        this.orderStatusImportDataConverter = orderStatusImportDataConverter;
    }

}
