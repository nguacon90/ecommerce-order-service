package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.dto.request.OrderSettingRequest;
import com.vctek.orderservice.model.OrderSettingModel;
import com.vctek.orderservice.repository.OrderSettingRepository;
import com.vctek.orderservice.util.CurrencyType;
import com.vctek.orderservice.util.OrderSettingType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ComboPriceSettingServiceImplTest {
    private ComboPriceSettingServiceImpl service;
    @Mock
    private OrderSettingRepository repositoryMock;
    private OrderSettingRequest request = new OrderSettingRequest();
    private ArgumentCaptor<OrderSettingModel> captor = ArgumentCaptor.forClass(OrderSettingModel.class);
    private OrderSettingModel existedModel =  new OrderSettingModel();

    @Before
    public void setup() {
        request.setCompanyId(1l);
        request.setAmount(10d);
        MockitoAnnotations.initMocks(this);
        service = new ComboPriceSettingServiceImpl(repositoryMock);
    }

    @Test
    public void save_newComboPriceSetting() {
        when(service.findByTypeAndCompanyId(OrderSettingType.COMBO_PRICE_SETTING.code(), 1l)).thenReturn(null);
        service.save(request);
        verify(repositoryMock).save(captor.capture());
        OrderSettingModel value = captor.getValue();
        assertEquals(1l, value.getCompanyId(), 0);
        assertEquals(OrderSettingType.COMBO_PRICE_SETTING.code(), value.getType());
        assertEquals(CurrencyType.PERCENT.toString(), value.getAmountType());
        assertEquals(10d, value.getAmount(), 0);
    }

    @Test
    public void save_existedComboPriceSetting() {
        existedModel.setAmount(30d);
        when(service.findByTypeAndCompanyId(OrderSettingType.COMBO_PRICE_SETTING.code(), 1l)).thenReturn(existedModel);
        service.save(request);
        verify(repositoryMock).save(captor.capture());
        OrderSettingModel value = captor.getValue();
        assertEquals(1l, value.getCompanyId(), 0);
        assertEquals(OrderSettingType.COMBO_PRICE_SETTING.code(), value.getType());
        assertEquals(CurrencyType.PERCENT.toString(), value.getAmountType());
        assertEquals(10d, value.getAmount(), 0);
    }
}
