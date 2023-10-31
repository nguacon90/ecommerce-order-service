package com.vctek.orderservice.converter.populator;

import com.vctek.orderservice.dto.request.OrderSettingCustomerRequest;
import com.vctek.orderservice.model.OrderSettingCustomerModel;
import com.vctek.orderservice.model.OrderSettingCustomerOptionModel;
import com.vctek.orderservice.model.OrderTypeSettingCustomerModel;
import com.vctek.orderservice.service.OrderSettingCustomerOptionService;
import com.vctek.orderservice.service.OrderTypeSettingCustomerService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

public class OrderSettingCustomerModelPopulatorTest {
    private OrderSettingCustomerModelPopulator populator;
    @Mock
    private OrderSettingCustomerOptionService optionService;
    @Mock
    private OrderTypeSettingCustomerService orderTypeSettingCustomerService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        populator = new OrderSettingCustomerModelPopulator();
        populator.setOptionService(optionService);
        populator.setOrderTypeSettingCustomerService(orderTypeSettingCustomerService);
    }

    @Test
    public void populate_create() {
        OrderSettingCustomerRequest source = new OrderSettingCustomerRequest();
        source.setName("name");
        source.setCompanyId(1l);
        source.setOrderTypes(Arrays.asList("orderType"));

        List<OrderSettingCustomerRequest> optionRequest = new ArrayList<>();
        OrderSettingCustomerRequest option = new OrderSettingCustomerRequest();
        option.setName("option name");
        optionRequest.add(option);
        source.setOptions(optionRequest);

        OrderSettingCustomerModel target = new OrderSettingCustomerModel();
        populator.populate(source, target);

        assertEquals(source.getName(), target.getName());
        assertEquals(source.getCompanyId(), target.getCompanyId(), 0);
        verify(orderTypeSettingCustomerService, times(0)).deleteAll(anyList());
        verify(optionService, times(0)).findByOrderSettingCustomerModel(any());
    }

    @Test
    public void populate_update() {
        OrderSettingCustomerRequest source = new OrderSettingCustomerRequest();
        source.setId(2L);
        source.setName("name");
        source.setCompanyId(1l);
        source.setOrderTypes(Arrays.asList("orderType"));

        List<OrderSettingCustomerRequest> optionRequest = new ArrayList<>();
        OrderSettingCustomerRequest option = new OrderSettingCustomerRequest();
        option.setName("option name");
        optionRequest.add(option);
        source.setOptions(optionRequest);
        OrderSettingCustomerRequest option2 = new OrderSettingCustomerRequest();
        option2.setId(1L);
        option2.setName("option name");
        optionRequest.add(option2);
        source.setOptions(optionRequest);

        List<OrderSettingCustomerOptionModel> optionModels = new ArrayList<>();
        OrderSettingCustomerOptionModel optionModel = new OrderSettingCustomerOptionModel();
        optionModel.setId(1L);
        optionModel.setName("optionName");
        optionModels.add(optionModel);

        List<OrderTypeSettingCustomerModel> orderTypeSettingCustomerModels = new ArrayList<>();
        OrderTypeSettingCustomerModel orderTypeSettingCustomerModel = new OrderTypeSettingCustomerModel();
        orderTypeSettingCustomerModel.setId(1L);
        orderTypeSettingCustomerModel.setOrderType("order Type");
        orderTypeSettingCustomerModels.add(orderTypeSettingCustomerModel);
        OrderTypeSettingCustomerModel orderTypeSettingCustomerModel2 = new OrderTypeSettingCustomerModel();
        orderTypeSettingCustomerModel2.setId(2L);
        orderTypeSettingCustomerModel2.setOrderType("orderType");
        orderTypeSettingCustomerModels.add(orderTypeSettingCustomerModel2);

        OrderSettingCustomerModel target = new OrderSettingCustomerModel();
        target.setId(2L);
        target.setOrderTypeSettingCustomerModels(orderTypeSettingCustomerModels);
        when(optionService.findByOrderSettingCustomerModel(any())).thenReturn(optionModels);
        populator.populate(source, target);

        assertEquals(source.getName(), target.getName());
        assertEquals(source.getCompanyId(), target.getCompanyId(), 0);
        verify(optionService, times(1)).findByOrderSettingCustomerModel(any());
        verify(orderTypeSettingCustomerService, times(1)).deleteAll(anyList());
    }
}
