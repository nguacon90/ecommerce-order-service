package com.vctek.orderservice.facade.impl;

import com.vctek.converter.Converter;
import com.vctek.converter.Populator;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.OrderSettingCustomerData;
import com.vctek.orderservice.dto.request.OrderSettingCustomerRequest;
import com.vctek.orderservice.dto.request.OrderSettingCustomerSearchRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.OrderSettingCustomerModel;
import com.vctek.orderservice.model.OrderSettingCustomerOptionModel;
import com.vctek.orderservice.model.OrderTypeSettingCustomerModel;
import com.vctek.orderservice.service.OrderSettingCustomerService;
import com.vctek.orderservice.service.OrderTypeSettingCustomerService;
import com.vctek.util.OrderType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class OrderSettingCustomerFacadeImplTest {
    private OrderSettingCustomerFacadeImpl facade;
    private OrderSettingCustomerRequest request;
    private OrderSettingCustomerModel model;
    private ArgumentCaptor<OrderSettingCustomerModel> captor;

    @Mock
    private OrderSettingCustomerService service;
    @Mock
    private OrderTypeSettingCustomerService orderTypeSettingCustomerService;
    @Mock
    private Converter<OrderSettingCustomerModel, OrderSettingCustomerData> converter;
    @Mock
    private Populator<OrderSettingCustomerRequest, OrderSettingCustomerModel> modelPopulator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        captor = ArgumentCaptor.forClass(OrderSettingCustomerModel.class);
        facade = new OrderSettingCustomerFacadeImpl();
        facade.setConverter(converter);
        facade.setModelPopulator(modelPopulator);
        facade.setService(service);
        facade.setOrderTypeSettingCustomerService(orderTypeSettingCustomerService);
        request = new OrderSettingCustomerRequest();
        request.setName("name");
        request.setCompanyId(2L);
        model = new OrderSettingCustomerModel();
        model.setName("name");
        model.setCompanyId(2L);
    }

    @Test
    public void create() {
        when(service.save(any())).thenReturn(model);
        facade.createSetting(request);
        verify(modelPopulator).populate(any(), any());
        verify(converter).convert(any(OrderSettingCustomerModel.class));
        verify(service, times(1)).save(any());
    }

    @Test
    public void update() {
        model.setId(2L);
        request.setId(2L);
        when(service.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(model);
        when(service.save(any())).thenReturn(model);
        facade.updateSetting(request);
        verify(modelPopulator).populate(any(), any());
        verify(converter).convert(any(OrderSettingCustomerModel.class));
        verify(service, times(1)).save(any());
    }

    @Test
    public void findOneBy_invalid() {
        try {
            when(service.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(null);
            facade.findOneBy(2L, 2L);
            fail("new throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_SETTING_CUSTOMER.message(), e.getMessage());
        }
    }

    @Test
    public void findOneBy() {
        when(service.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(model);
        facade.findOneBy(2L, 2L);
        verify(service, times(1)).findByIdAndCompanyId(anyLong(), anyLong());
        converter.convert(any(OrderSettingCustomerModel.class));
    }

    @Test
    public void findAllBy() {
        OrderSettingCustomerSearchRequest request = new OrderSettingCustomerSearchRequest();
        when(service.findAllBy(request)).thenReturn(Arrays.asList(new OrderSettingCustomerModel()));
        facade.findAllBy(request);
        verify(service).findAllBy(any(OrderSettingCustomerSearchRequest.class));
        verify(converter).convertAll(anyList());
    }

    @Test
    public void validOrderType_createSettingDefault() {
        try {
            request.setOrderTypes(Arrays.asList("abc"));
            when(service.findByCompanyIdAndDefault(anyLong())).thenReturn(null);
            facade.createOrUpdateDefault(request);
            fail("new throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_TYPE.message(), e.getMessage());
        }
    }

    @Test
    public void createSettingDefault() {
        request.setOrderTypes(Arrays.asList(OrderType.RETAIL.toString()));
        when(service.findByCompanyIdAndDefault(anyLong())).thenReturn(null);
        facade.createOrUpdateDefault(request);
        verify(service).save(captor.capture());
        OrderSettingCustomerModel saveModel = captor.getValue();
        assertEquals(1, saveModel.getOrderTypeSettingCustomerModels().size());
        verify(service).findByCompanyIdAndDefault(anyLong());
        verify(converter).convert(any());
    }

    @Test
    public void updateSettingDefault() {
        request.setOrderTypes(Arrays.asList(OrderType.ONLINE.toString(), OrderType.RETAIL.toString()));
        OrderTypeSettingCustomerModel orderTypeSettingCustomerModel = new OrderTypeSettingCustomerModel();
        orderTypeSettingCustomerModel.setId(1L);
        orderTypeSettingCustomerModel.setOrderType(OrderType.ONLINE.toString());
        List<OrderTypeSettingCustomerModel> orderTypeModels = new ArrayList<>();
        orderTypeModels.add(orderTypeSettingCustomerModel);
        model.setOrderTypeSettingCustomerModels(orderTypeModels);
        when(service.findByCompanyIdAndDefault(anyLong())).thenReturn(model);
        facade.createOrUpdateDefault(request);
        verify(service).save(captor.capture());
        OrderSettingCustomerModel saveModel = captor.getValue();
        assertEquals(2, saveModel.getOrderTypeSettingCustomerModels().size());
        verify(service).findByCompanyIdAndDefault(anyLong());
        verify(converter).convert(any());
    }

    @Test
    public void getSettingDefault_null() {
        when(service.findByCompanyIdAndDefault(anyLong())).thenReturn(null);
        facade.getSettingDefault(2L);
        verify(service, times(1)).findByCompanyIdAndDefault(anyLong());
        verify(converter, times(0)).convert(any());
    }

    @Test
    public void getSettingDefault() {
        when(service.findByCompanyIdAndDefault(anyLong())).thenReturn(new OrderSettingCustomerModel());
        facade.getSettingDefault(2L);
        verify(service, times(1)).findByCompanyIdAndDefault(anyLong());
        verify(converter, times(1)).convert(any());
    }

    @Test
    public void deletedSetting() {
        OrderTypeSettingCustomerModel orderTypeSettingCustomerModel = new OrderTypeSettingCustomerModel();
        orderTypeSettingCustomerModel.setId(1L);
        orderTypeSettingCustomerModel.setOrderType(OrderType.ONLINE.toString());
        List<OrderTypeSettingCustomerModel> orderTypeModels = new ArrayList<>();
        orderTypeModels.add(orderTypeSettingCustomerModel);
        model.setOrderTypeSettingCustomerModels(orderTypeModels);
        when(service.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(model);
        facade.deletedSetting(2L, 2L);
        verify(service, times(1)).findByIdAndCompanyId(anyLong(), anyLong());
        verify(orderTypeSettingCustomerService, times(1)).deleteAll(anyList());
        verify(service, times(1)).save(any());
    }

    @Test
    public void deletedSettingOption_invalid() {
        try{
            when(service.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(model);
            facade.deletedSettingOption(2L, 2L, 2L);
            fail("new throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_SETTING_CUSTOMER_OPTIONS.message(), e.getMessage());
        }
    }

    @Test
    public void deletedSettingOption() {
        List<OrderSettingCustomerOptionModel> optionModels = new ArrayList<>();
        OrderSettingCustomerOptionModel optionModel = new OrderSettingCustomerOptionModel();
        optionModel.setId(2L);
        optionModels.add(optionModel);
        model.setOptionModels(optionModels);
        when(service.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(model);
        facade.deletedSettingOption(2L, 2L, 2L);
        verify(service).findByIdAndCompanyId(anyLong(), anyLong());
        verify(service).save(any());
    }

    @Test
    public void findSettingByOrder_empty() {
        when(service.findAllByCompanyIdAndOrderType(anyLong(), anyString())).thenReturn(new ArrayList<>());
        when(service.findByCompanyIdAndDefault(anyLong())).thenReturn(null);
        facade.findSettingByOrder(2L, "orderType");
        verify(service).findAllByCompanyIdAndOrderType(anyLong(), anyString());
        verify(converter, times(0)).convertAll(any());
    }

    @Test
    public void findSettingByOrder_hasDefault_emptyList() {
        OrderSettingCustomerModel model = new OrderSettingCustomerModel();
        OrderTypeSettingCustomerModel typeModel = new OrderTypeSettingCustomerModel();
        typeModel.setOrderType("orderType");
        model.setOrderTypeSettingCustomerModels(Arrays.asList(typeModel));
        when(service.findAllByCompanyIdAndOrderType(anyLong(), anyString())).thenReturn(new ArrayList<>());
        when(service.findByCompanyIdAndDefault(anyLong())).thenReturn(model);
        facade.findSettingByOrder(2L, "orderType");
        verify(service).findAllByCompanyIdAndOrderType(anyLong(), anyString());
        verify(converter, times(0)).convertAll(any());
    }

    @Test
    public void findSettingByOrder_notDefault_hasNotEmptyList() {
        OrderSettingCustomerModel model = new OrderSettingCustomerModel();
        model.setDefault(false);
        when(service.findAllByCompanyIdAndOrderType(anyLong(), anyString())).thenReturn(Arrays.asList(model));
        when(service.findByCompanyIdAndDefault(anyLong())).thenReturn(null);
        facade.findSettingByOrder(2L, "orderType");
        verify(service).findAllByCompanyIdAndOrderType(anyLong(), anyString());
        verify(converter, times(1)).convertAll(any());
    }

    @Test
    public void findSettingByOrder_hasDefault_hasNotEmptyList() {
        OrderSettingCustomerModel model = new OrderSettingCustomerModel();
        model.setDefault(true);
        OrderSettingCustomerModel model2 = new OrderSettingCustomerModel();
        model2.setDefault(false);
        when(service.findAllByCompanyIdAndOrderType(anyLong(), anyString())).thenReturn(Arrays.asList(model2));
        when(service.findByCompanyIdAndDefault(anyLong())).thenReturn(model);
        facade.findSettingByOrder(2L, "orderType");
        verify(service).findAllByCompanyIdAndOrderType(anyLong(), anyString());
        verify(converter, times(1)).convertAll(any());
    }
}
