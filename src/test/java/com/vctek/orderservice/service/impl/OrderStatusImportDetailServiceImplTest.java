package com.vctek.orderservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vctek.dto.health.ServiceName;
import com.vctek.kafka.data.OrderProcessResultData;
import com.vctek.orderservice.dto.IntegrationServiceStatusData;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.OrderStatusImportDetailModel;
import com.vctek.orderservice.repository.OrderStatusImportDetailRepository;
import com.vctek.orderservice.repository.OrderStatusImportRepository;
import com.vctek.orderservice.service.OrderService;
import com.vctek.util.OrderProcessResultStatus;
import com.vctek.util.OrderStatusImport;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OrderStatusImportDetailServiceImplTest {
    private OrderStatusImportDetailServiceImpl service;
    private OrderStatusImportDetailModel detail;

    @Mock
    private OrderService orderService;
    @Mock
    private OrderStatusImportDetailRepository repository;
    @Mock
    private OrderStatusImportRepository orderStatusImportRepository;

    private ObjectMapper objectMapper;
    private OrderProcessResultData data;
    private ArgumentCaptor<OrderStatusImportDetailModel> captor;
    @Mock
    private OrderModel orderModelMock;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        objectMapper = new ObjectMapper();
        service = new OrderStatusImportDetailServiceImpl();
        captor = ArgumentCaptor.forClass(OrderStatusImportDetailModel.class);
        service.setRepository(repository);
        service.setOrderService(orderService);
        service.setObjectMapper(objectMapper);
        service.setOrderStatusImportRepository(orderStatusImportRepository);

        detail = new OrderStatusImportDetailModel();
        detail.setOrderCode("code2");
        detail.setStatus(OrderStatusImport.PROCESSING.toString());
        data = new OrderProcessResultData();
        data.setCompanyId(2L);
        data.setOrderCode("code");
        data.setImportDetailId(2L);
        data.setServiceName("service");
        when(orderModelMock.isImportOrderProcessing()).thenReturn(true);
    }

    private IntegrationServiceStatusData generateIntegrationServiceData(String name, String status) {
        IntegrationServiceStatusData baseObjectData = new IntegrationServiceStatusData();
        baseObjectData.setName(name);
        baseObjectData.setStatus(status);
        return baseObjectData;
    }

    private void setIntegrationServiceStatus(List<IntegrationServiceStatusData> integrationServiceStatusList) {
        try {
            detail.setIntegrationServiceStatus(objectMapper.writeValueAsString(integrationServiceStatusList));
        } catch (IOException e) {

        }
    }

    @Test
    public void updateStatusAndUnlockOrder_serviceError() {
        data.setStatus(OrderProcessResultStatus.ERROR.toString());
        data.setErrorCode("errorCode");
        when(repository.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(Optional.of(detail));
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(orderModelMock);
        service.updateStatusAndUnlockOrder(data);
        verify(repository).save(captor.capture());
        verify(orderModelMock, times(0)).setImportOrderProcessing(anyBoolean());
        verify(orderService, times(0)).save(any(OrderModel.class));
        verify(orderService, times(1)).findByCodeAndCompanyId(anyString(), anyLong());
        OrderStatusImportDetailModel savedModel = captor.getValue();
        assertNotNull(savedModel.getNote());
    }

    @Test
    public void updateStatusAndUnlockOrder_NoContainLogisticServiceOK() {
        data.setStatus(OrderProcessResultStatus.OK.toString());
        data.setErrorCode("");
        data.setServiceName(ServiceName.FINANCE.toString());
        setIntegrationServiceStatus(Arrays.asList(generateIntegrationServiceData(ServiceName.LOYALTY.toString(), OrderProcessResultStatus.OK.toString())));
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(orderModelMock);
        when(repository.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(Optional.of(detail));

        service.updateStatusAndUnlockOrder(data);
        verify(repository).save(captor.capture());
        verify(orderService, times(0)).save(any(OrderModel.class));
        verify(orderModelMock, times(0)).setImportOrderProcessing(false);
        OrderStatusImportDetailModel savedModel = captor.getValue();
        assertNull(savedModel.getNote());
        assertNotNull(savedModel.getIntegrationServiceStatus());
        assertEquals(OrderStatusImport.PROCESSING.toString(), savedModel.getStatus());
    }

    @Test
    public void updateStatusAndUnlockOrder_ContainLogisticServiceOK() {
        data.setStatus(OrderProcessResultStatus.OK.toString());
        data.setErrorCode("");
        data.setServiceName(ServiceName.FINANCE.toString());
        when(orderModelMock.isImportOrderProcessing()).thenReturn(false);
        setIntegrationServiceStatus(Arrays.asList(
                generateIntegrationServiceData(ServiceName.LOGISTIC.toString(), OrderProcessResultStatus.OK.toString()),
                generateIntegrationServiceData(ServiceName.LOYALTY.toString(), OrderProcessResultStatus.OK.toString())));
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(orderModelMock);
        when(repository.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(Optional.of(detail));
        service.updateStatusAndUnlockOrder(data);
        verify(repository).save(captor.capture());
        verify(orderService, times(0)).save(any(OrderModel.class));
        verify(orderModelMock, times(0)).setImportOrderProcessing(false);
        OrderStatusImportDetailModel savedModel = captor.getValue();
        assertNull(savedModel.getNote());
        assertNotNull(savedModel.getIntegrationServiceStatus());
    }

    @Test
    public void unLockOrderModel_WithOnlyFinanceStatusOK_NotUnlock() {
        data.setStatus(OrderProcessResultStatus.OK.toString());
        data.setErrorCode("");
        data.setServiceName(ServiceName.FINANCE.toString());
        IntegrationServiceStatusData financeStatus = generateIntegrationServiceData(ServiceName.FINANCE.toString(), OrderProcessResultStatus.OK.toString());
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(orderModelMock);
        boolean unlock = service.unLockOrderModel(Arrays.asList(financeStatus), data);
        assertEquals(false, unlock);
        verify(orderModelMock, times(0)).setImportOrderProcessing(false);
        verify(orderService, times(0)).save(any());
    }

    @Test
    public void unLockOrderModel_WithLogisticSuccessStatus() {
        data.setStatus(OrderProcessResultStatus.OK.toString());
        data.setErrorCode("");
        data.setServiceName(ServiceName.LOGISTIC.toString());
        IntegrationServiceStatusData logisticStatus = generateIntegrationServiceData(ServiceName.LOGISTIC.toString(), OrderProcessResultStatus.OK.toString());
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(orderModelMock);
        boolean unlock = service.unLockOrderModel(Arrays.asList(logisticStatus), data);
        assertEquals(true, unlock);
        verify(orderService, times(1)).save(any(OrderModel.class));
    }

    @Test
    public void unLockOrderModel_WithLogisticSuccessStatus_OtherServiceError() {
        data.setStatus(OrderProcessResultStatus.ERROR.toString());
        data.setErrorCode("ERROR");
        data.setServiceName(ServiceName.FINANCE.toString());
        when(orderModelMock.isImportOrderProcessing()).thenReturn(false);
        IntegrationServiceStatusData logisticStatus = generateIntegrationServiceData(ServiceName.LOGISTIC.toString(), OrderProcessResultStatus.OK.toString());
        IntegrationServiceStatusData financeStatus = generateIntegrationServiceData(ServiceName.FINANCE.toString(), OrderProcessResultStatus.ERROR.toString());
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(orderModelMock);
        boolean unlock = service.unLockOrderModel(Arrays.asList(logisticStatus, financeStatus), data);
        assertEquals(false, unlock);
        verify(orderService, times(1)).save(any(OrderModel.class));
    }

    @Test
    public void unLockOrderModel_WithLogisticSuccessStatus_OtherServiceSuccess() {
        data.setStatus(OrderProcessResultStatus.OK.toString());
        data.setServiceName(ServiceName.FINANCE.toString());
        when(orderModelMock.isImportOrderProcessing()).thenReturn(false);
        IntegrationServiceStatusData logisticStatus = generateIntegrationServiceData(ServiceName.LOGISTIC.toString(), OrderProcessResultStatus.OK.toString());
        IntegrationServiceStatusData financeStatus = generateIntegrationServiceData(ServiceName.FINANCE.toString(), OrderProcessResultStatus.OK.toString());
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(orderModelMock);
        boolean unlock = service.unLockOrderModel(Arrays.asList(logisticStatus, financeStatus), data);
        assertEquals(true, unlock);
        verify(orderModelMock, times(0)).setImportOrderProcessing(false);
        verify(orderService, times(0)).save(any());
    }
}