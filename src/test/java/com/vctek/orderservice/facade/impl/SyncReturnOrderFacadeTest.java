package com.vctek.orderservice.facade.impl;

import com.vctek.migration.dto.MigrateBillDto;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.ReturnOrderModel;
import com.vctek.orderservice.repository.dao.ReturnOrderDAO;
import com.vctek.orderservice.service.OrderService;
import com.vctek.orderservice.service.ReturnOrderService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class SyncReturnOrderFacadeTest {
    private SyncReturnOrderFacadeImpl syncReturnOrderFacade;

    @Mock
    private ReturnOrderService returnOrderService;
    @Mock
    private OrderService orderService;
    @Mock
    private ReturnOrderDAO returnOrderDAO;
    @Mock
    private MigrateBillDto orderMigrationMock;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        syncReturnOrderFacade = new SyncReturnOrderFacadeImpl();
        syncReturnOrderFacade.setReturnOrderDAO(returnOrderDAO);
        syncReturnOrderFacade.setOrderService(orderService);
        syncReturnOrderFacade.setReturnOrderService(returnOrderService);
    }

    @Test
    public void processSyncReturnOrderMessage_Existed() {
        when(orderMigrationMock.getOrderCode()).thenReturn("123");
        when(orderMigrationMock.getCompanyId()).thenReturn(1l);
        when(orderMigrationMock.getReturnOrderId()).thenReturn(1l);
        when(returnOrderService.findByExportExternalIdAndCompanyId(anyLong(), anyLong())).thenReturn(new ReturnOrderModel());
        syncReturnOrderFacade.processSyncReturnOrderMessage(orderMigrationMock);
        verify(returnOrderService).findByExportExternalIdAndCompanyId(anyLong(), anyLong());
    }

    @Test
    public void processSyncReturnOrderMessage() {
        when(orderMigrationMock.getOrderCode()).thenReturn("123");
        when(orderMigrationMock.getCompanyId()).thenReturn(1l);
        when(orderMigrationMock.getReturnOrderId()).thenReturn(1l);
        when(orderMigrationMock.getBillExchangeId()).thenReturn(1l);
        when(orderMigrationMock.getBillReturnId()).thenReturn(1l);
        when(returnOrderService.findByExportExternalIdAndCompanyId(anyLong(), anyLong())).thenReturn(null);
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(new OrderModel());
        syncReturnOrderFacade.processSyncReturnOrderMessage(orderMigrationMock);
        verify(orderService, times(2)).findByCodeAndCompanyId(anyString(), anyLong());
        verify(returnOrderService).onlySave(any(ReturnOrderModel.class));
        verify(returnOrderDAO).updateAuditing(any(), any());
    }
}
