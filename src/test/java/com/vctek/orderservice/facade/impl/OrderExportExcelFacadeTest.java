package com.vctek.orderservice.facade.impl;

import com.vctek.converter.Converter;
import com.vctek.dto.ExcelStatusData;
import com.vctek.orderservice.dto.request.OrderFileParameter;
import com.vctek.orderservice.dto.request.OrderSearchRequest;
import com.vctek.orderservice.service.OrderFileService;
import com.vctek.orderservice.util.ExportExcelType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class OrderExportExcelFacadeTest {
    private OrderExportExcelFacadeImpl facade;
    @Mock
    private OrderFileService biFileService;
    private OrderSearchRequest request;
    @Mock
    private Converter<OrderSearchRequest, OrderFileParameter> fileParameterConverter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        facade = new OrderExportExcelFacadeImpl();
        facade.setOrderFileService(biFileService);
        facade.setFileParameterConverter(fileParameterConverter);
        request = new OrderSearchRequest();
        request.setUserId(22l);
        when(fileParameterConverter.convert(request)).thenReturn(new OrderFileParameter());

    }

    @Test
    public void isExportExcel_NotDefineKey() {
        assertEquals(false, facade.isExportExcel(request));
    }

    @Test
    public void isExportExcel_ProductType_NotSetExportOnCache() {
        request.setExportType("ss");
        when(biFileService.isProcessingExportExcel(any(OrderFileParameter.class))).thenReturn(false);
        assertEquals(false, facade.isExportExcel(request));
    }

    @Test
    public void checkStatus() {
        request.setExportType(ExportExcelType.EXPORT_ORDER_WIDTH_DETAIL_COMBO.toString());
        when(biFileService.isProcessingExportExcel(any(OrderFileParameter.class))).thenReturn(true);
        when(biFileService.isExistedFile(any(OrderFileParameter.class))).thenReturn(false);
        ExcelStatusData excelStatusData = facade.checkStatus(request);
        assertEquals(true, excelStatusData.isProcessingExport());
        assertEquals(false, excelStatusData.isFinishExport());
    }
}
