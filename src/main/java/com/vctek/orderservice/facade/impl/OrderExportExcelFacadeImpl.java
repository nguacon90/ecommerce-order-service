package com.vctek.orderservice.facade.impl;

import com.vctek.converter.Converter;
import com.vctek.dto.ExcelStatusData;
import com.vctek.orderservice.dto.request.OrderFileParameter;
import com.vctek.orderservice.dto.request.OrderSearchRequest;
import com.vctek.orderservice.facade.OrderExportExcelFacade;
import com.vctek.orderservice.service.OrderFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderExportExcelFacadeImpl implements OrderExportExcelFacade {
    private OrderFileService orderFileService;
    private Converter<OrderSearchRequest, OrderFileParameter> fileParameterConverter;


    @Override
    public boolean isExportExcel(OrderSearchRequest request) {
        OrderFileParameter orderFileParameter = fileParameterConverter.convert(request);
        return orderFileService.isProcessingExportExcel(orderFileParameter);
    }

    @Override
    public void processExportExcel(OrderSearchRequest request, boolean isProcessing) {
        OrderFileParameter orderFileParameter = fileParameterConverter.convert(request);
        orderFileService.setProcessExportExcel(orderFileParameter, isProcessing);
    }

    @Override
    public ExcelStatusData checkStatus(OrderSearchRequest request) {
        ExcelStatusData statusData = new ExcelStatusData();
        statusData.setProcessingExport(isExportExcel(request));
        OrderFileParameter fileParameter = fileParameterConverter.convert(request);
        statusData.setFinishExport(orderFileService.isExistedFile(fileParameter));
        return statusData;
    }


    @Autowired
    public void setOrderFileService(OrderFileService orderFileService) {
        this.orderFileService = orderFileService;
    }

    @Autowired
    public void setFileParameterConverter(Converter<OrderSearchRequest, OrderFileParameter> fileParameterConverter) {
        this.fileParameterConverter = fileParameterConverter;
    }
}
