package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.dto.request.OrderFileParameter;
import com.vctek.orderservice.util.ExportExcelType;
import com.vctek.util.OrderType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class OrderFileServiceTest {
    private OrderFileServiceImpl service;
    private OrderFileParameter fileParameter;
    @Mock
    private RedisTemplate redisTemplate;
    @Mock
    private ValueOperations valueOperations;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        fileParameter = new OrderFileParameter();
        fileParameter.setUserId(345l);
        fileParameter.setOrderType(OrderType.ONLINE.toString());
        service = new OrderFileServiceImpl();
        service.setRedisTemplate(redisTemplate);
        service.setFileRootPath("src/test/resources/files");
        service.setRedisCachedKeyTimeout(4);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    public void writeToFile() {
        fileParameter.setExportExcelType(ExportExcelType.EXPORT_ORDER_WIDTH_DETAIL_COMBO);
        service.writeToFile(new byte[0], fileParameter);
        assertEquals(true, service.isExistedFile(fileParameter));
    }


    @Test
    public void readFile() {
        fileParameter.setExportExcelType(ExportExcelType.EXPORT_ORDER_WIDTH_DETAIL_COMBO);
        byte[] bytes = service.readFile(fileParameter);
        assertNotNull(bytes);
    }

    @Test
    public void mergeFiles() {
        fileParameter.setExportExcelType(ExportExcelType.EXPORT_ORDER_WIDTH_DETAIL_COMBO);
        service.mergeFile(fileParameter, 1);
        byte[] bytes = service.readFile(fileParameter);
        assertNotNull(bytes);

    }

    @Test
    public void isExportExcel_ProductType_SetExportOnCache() {
        fileParameter.setExportExcelType(ExportExcelType.EXPORT_ORDER_WIDTH_DETAIL_COMBO);
        when(valueOperations.get(any())).thenReturn(true);
        assertEquals(true, service.isProcessingExportExcel(fileParameter));
    }

    @Test
    public void doFinishExportExcel_WidthDetailCombo() {
        fileParameter.setExportExcelType(ExportExcelType.EXPORT_ORDER_WIDTH_DETAIL_COMBO);
        service.setProcessExportExcel(fileParameter, true);
        verify(valueOperations, times(1)).set(any(), eq(true), eq(4l), eq(TimeUnit.HOURS));
    }
}
