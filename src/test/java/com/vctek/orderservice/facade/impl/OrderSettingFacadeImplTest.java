package com.vctek.orderservice.facade.impl;

import com.vctek.converter.Converter;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.OrderSettingData;
import com.vctek.orderservice.dto.request.OrderSettingRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.OrderSettingModel;
import com.vctek.orderservice.service.ComboPriceSettingService;
import com.vctek.orderservice.service.OrderSettingService;
import com.vctek.orderservice.util.OrderSettingType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.cluster.lock.support.DelegatingDistributedLock;
import org.springframework.cloud.cluster.redis.lock.RedisLockService;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class OrderSettingFacadeImplTest {
    private OrderSettingFacadeImpl facade;

    @Mock
    private ComboPriceSettingService comboPriceSettingService;
    @Mock
    private Converter<OrderSettingModel, OrderSettingData> orderSettingDataConverter;
    @Mock
    private RedisLockService redisLockService;
    @Mock
    private DelegatingDistributedLock lockMock;
    @Mock
    private OrderSettingRequest requestMock;
    @Mock
    private OrderSettingService orderSettingService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        facade = new OrderSettingFacadeImpl();
        facade.setComboPriceSettingService(comboPriceSettingService);
        facade.setOrderSettingDataConverter(orderSettingDataConverter);
        facade.setRedisLockService(redisLockService);
        facade.setOrderSettingService(orderSettingService);
        when(redisLockService.obtain(anyString())).thenReturn(lockMock);
        when(lockMock.tryLock()).thenReturn(true);
    }

    @Test
    public void createOrUpdateComboPriceSetting() {
        when(comboPriceSettingService.save(requestMock)).thenReturn(new OrderSettingModel());
        facade.createOrUpdateComboPriceSetting(requestMock);
        verify(comboPriceSettingService).save(requestMock);
        verify(orderSettingDataConverter).convert(any(OrderSettingModel.class));
    }

    @Test
    public void getComboPriceSetting_null() {
        when(comboPriceSettingService.findByTypeAndCompanyId(anyString(), anyLong())).thenReturn(null);
        OrderSettingData data = facade.getComboPriceSetting(2l);
        assertNull(data);
        verify(comboPriceSettingService).findByTypeAndCompanyId(anyString(), anyLong());
    }

    @Test
    public void getComboPriceSetting() {
        when(comboPriceSettingService.findByTypeAndCompanyId(anyString(), anyLong())).thenReturn(new OrderSettingModel());
        facade.getComboPriceSetting(2l);
        verify(comboPriceSettingService).findByTypeAndCompanyId(anyString(), anyLong());
        verify(orderSettingDataConverter).convert(any(OrderSettingModel.class));
    }

    @Test
    public void getOrderMaximumDiscount_null() {
        when(orderSettingService.findByTypeAndCompanyId(anyString(), anyLong())).thenReturn(null);
        OrderSettingData data = facade.getOrderMaximumDiscount(2l);
        assertNull(data);
        verify(orderSettingService).findByTypeAndCompanyId(anyString(), anyLong());
    }

    @Test
    public void getOrderMaximumDiscount() {
        when(orderSettingService.findByTypeAndCompanyId(anyString(), anyLong())).thenReturn(new OrderSettingModel());
        facade.getOrderMaximumDiscount(2l);
        verify(orderSettingService).findByTypeAndCompanyId(anyString(), anyLong());
        verify(orderSettingDataConverter).convert(any(OrderSettingModel.class));
    }

    @Test
    public void createOrUpdateSettingNotificationChangeStatus_validNote() {
        try {
            when(requestMock.getNote()).thenReturn("Cách đây ít phút Sơn Tùng M-TP đã upload There's no one at all version 2 để kỉ niệm sinh nhật lần thứ 28." +
                    "Phiên bản lần này đang nhận được rất nhiều lời khen tích cực, cả về hình ảnh lẫn giai điệu của bài hát");
            facade.createOrUpdateSettingNotificationChangeStatus(requestMock);
            fail("Must throw new Exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.OVER_MAX_LENGTH_100.message(), e.getMessage());
        }
    }

    @Test
    public void createOrUpdateSettingNotificationChangeStatus() {
        when(requestMock.getNote()).thenReturn("message notification");
        when(requestMock.getCompanyId()).thenReturn(2L);
        when(orderSettingService.findByTypeAndCompanyId(eq(OrderSettingType.CREATE_NOTIFICATION_CHANGE_ORDER_STATUS.code()), anyLong())).thenReturn(null);
        when(orderSettingService.save(any(OrderSettingModel.class))).thenReturn(new OrderSettingModel());
        facade.createOrUpdateSettingNotificationChangeStatus(requestMock);
        verify(orderSettingService).save(any(OrderSettingModel.class));
        verify(orderSettingDataConverter).convert(any(OrderSettingModel.class));
    }

    @Test
    public void getSettingNotificationChangeStatus_empryModel() {
        when(orderSettingService.findByTypeAndCompanyId(eq(OrderSettingType.CREATE_NOTIFICATION_CHANGE_ORDER_STATUS.code()), anyLong())).thenReturn(null);
        facade.getSettingNotificationChangeStatus(2L);
        verify(orderSettingDataConverter, times(0)).convert(any(OrderSettingModel.class));
    }

    @Test
    public void getSettingNotificationChangeStatus() {
        when(orderSettingService.findByTypeAndCompanyId(eq(OrderSettingType.CREATE_NOTIFICATION_CHANGE_ORDER_STATUS.code()), anyLong())).thenReturn(new OrderSettingModel());
        facade.getSettingNotificationChangeStatus(2L);
        verify(orderSettingDataConverter, times(1)).convert(any(OrderSettingModel.class));
    }
}
