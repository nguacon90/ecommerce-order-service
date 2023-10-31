package com.vctek.orderservice.validator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.converter.populator.UpdateOrderParameterPopulator;
import com.vctek.orderservice.dto.UpdateOrderParameter;
import com.vctek.orderservice.dto.request.OrderRequest;
import com.vctek.orderservice.dto.request.PaymentTransactionRequest;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.OrderSettingCustomerOptionModel;
import com.vctek.orderservice.service.OrderService;
import com.vctek.orderservice.service.OrderSettingCustomerOptionService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class UpdateOrderParameterPopulatorTest {
    @Mock
    private OrderService orderService;

    @Mock
    private OrderSettingCustomerOptionService orderSettingCustomerOptionService;

    @Mock
    private Populator<List<PaymentTransactionRequest>, OrderModel> orderPaymentTransactionRequestPopulator;

    private UpdateOrderParameterPopulator populator;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        populator = new UpdateOrderParameterPopulator(orderService, orderSettingCustomerOptionService);
        populator.setOrderPaymentTransactionRequestPopulator(orderPaymentTransactionRequestPopulator);
    }

    @Test
    public void populate() {
        OrderRequest orderRequest = new OrderRequest();
        UpdateOrderParameter paramter = new UpdateOrderParameter();
        orderRequest.setCode("code");
        orderRequest.setCompanyId(1l);
        orderRequest.setOrderSourceId(1l);
        orderRequest.setCardNumber("card");
        orderRequest.setDeliveryCost(20d);
        List<Long> settingCustomerOptionIds = new ArrayList<>();
        settingCustomerOptionIds.add(1l);
        settingCustomerOptionIds.add(2l);
        OrderModel orderModel = new OrderModel();
        orderModel.setId(1l);
        orderRequest.setSettingCustomerOptionIds(settingCustomerOptionIds);
        OrderSettingCustomerOptionModel orderSettingCustomerOptionModel = new OrderSettingCustomerOptionModel();
        orderSettingCustomerOptionModel.setId(1l);
        OrderSettingCustomerOptionModel orderSettingCustomerOptionModel2 = new OrderSettingCustomerOptionModel();
        orderSettingCustomerOptionModel2.setId(2l);
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(orderModel);
        when(orderSettingCustomerOptionService.findByIdAndCompanyId(1l, 1l)).thenReturn(orderSettingCustomerOptionModel);
        when(orderSettingCustomerOptionService.findByIdAndCompanyId(2l, 1l)).thenReturn(orderSettingCustomerOptionModel2);
        populator.populate(orderRequest, paramter);
        assertEquals(orderRequest.getCardNumber(), paramter.getCardNumber());
        assertEquals(orderRequest.getOrderSourceId(), paramter.getOrderSourceId());
        assertEquals(orderRequest.getDeliveryCost(), paramter.getDeliveryCost());
        assertEquals(orderRequest.getSettingCustomerOptionIds(), paramter.getOrder().getOrderSettingCustomerOptionModels().stream().map(o -> o.getId()).collect(Collectors.toList()));
    }
}
