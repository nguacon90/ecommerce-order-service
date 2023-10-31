package com.vctek.orderservice.facade.impl;

import com.vctek.converter.Populator;
import com.vctek.migration.dto.MigrateBillDto;
import com.vctek.migration.dto.SyncOrderNoteData;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.repository.dao.OrderDAO;
import com.vctek.orderservice.repository.dao.OrderNoteDAO;
import com.vctek.orderservice.service.CalculationService;
import com.vctek.orderservice.service.OrderNoteService;
import com.vctek.orderservice.service.OrderService;
import com.vctek.orderservice.strategy.CommerceCartCalculationStrategy;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Calendar;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class SyncOrderFacadeTest {
    private SyncOrderFacadeImpl syncOrderFacade;

    @Mock
    private OrderService orderService;
    private Populator<MigrateBillDto, OrderModel> syncOrderPopulator;
    @Mock
    private OrderDAO orderDAO;
    @Mock
    private MigrateBillDto orderMigrationMock;
    @Mock
    private CalculationService calculationService;
    @Mock
    private OrderNoteService orderNoteService;
    @Mock
    private OrderNoteDAO orderNoteDAO;
    @Mock
    private CommerceCartCalculationStrategy commerceCartCalculationStrategy;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        syncOrderPopulator = mock(Populator.class);
        syncOrderFacade = new SyncOrderFacadeImpl();
        syncOrderFacade.setCalculationService(calculationService);
        syncOrderFacade.setOrderDAO(orderDAO);
        syncOrderFacade.setOrderService(orderService);
        syncOrderFacade.setSyncOrderPopulator(syncOrderPopulator);
        syncOrderFacade.setOrderNoteDAO(orderNoteDAO);
        syncOrderFacade.setOrderNoteService(orderNoteService);
        syncOrderFacade.setCommerceCartCalculationStrategy(commerceCartCalculationStrategy);
    }

    @Test
    public void processSyncOrderMessage_Existed() {
        when(orderMigrationMock.getOrderCode()).thenReturn("123");
        when(orderMigrationMock.getCompanyId()).thenReturn(1l);
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(new OrderModel());
        syncOrderFacade.processSyncOrderMessage(orderMigrationMock);
        verify(orderService).findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean());
    }

    @Test
    public void processSyncOrderMessage() {
        when(orderMigrationMock.getOrderCode()).thenReturn("123");
        when(orderMigrationMock.getCompanyId()).thenReturn(1l);
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(null);
        syncOrderFacade.processSyncOrderMessage(orderMigrationMock);
        verify(calculationService).calculate(any(OrderModel.class));
        verify(orderService).save(any(OrderModel.class));
        verify(orderDAO).updateAuditing(any(), any());
    }

    @Test
    public void processSyncOrderMessage_create_retail() {
        SyncOrderNoteData syncOrderNoteData = new SyncOrderNoteData();
        syncOrderNoteData.setContent("note");
        syncOrderNoteData.setCreatedAt(Calendar.getInstance().getTime().toString());
        syncOrderNoteData.setCreatedBy(1l);
        when(orderMigrationMock.getOrderNotes()).thenReturn(Arrays.asList(syncOrderNoteData));
        when(orderMigrationMock.getOrderRetailCode()).thenReturn("123321");
        when(orderMigrationMock.getOrderCode()).thenReturn("123");
        when(orderMigrationMock.getId()).thenReturn(123l);
        when(orderMigrationMock.getCompanyId()).thenReturn(1l);
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(null);
        when(orderService.save(any())).thenReturn(new OrderModel());
        syncOrderFacade.processSyncOrderMessage(orderMigrationMock);
        verify(calculationService).calculate(any(OrderModel.class));
        verify(orderService, times(2)).save(any(OrderModel.class));
        verify(orderDAO, times(2)).updateAuditing(any(), any());
    }
}
