package com.vctek.orderservice.converter.populator;

import com.vctek.orderservice.dto.request.OrderStatusImportRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.OrderStatusImportModel;
import com.vctek.orderservice.service.LogisticService;
import com.vctek.orderservice.service.OrderService;
import com.vctek.util.OrderStatus;
import com.vctek.util.OrderStatusImport;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class OrderStatusImportModelPopulatorTest {
    private OrderStatusImportRequest request;
    private OrderStatusImportModelPopulator populator;
    private OrderStatusImportModel model;
    private OrderModel orderModel1;
    private OrderModel orderModel2;

    @Mock
    private OrderService orderService;
    @Mock
    private LogisticService logisticService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        populator = new OrderStatusImportModelPopulator();
        populator.setOrderService(orderService);
        populator.setLogisticService(logisticService);
        request = new OrderStatusImportRequest();
        request.setCompanyId(2L);
        request.setOrderStatus(OrderStatus.NEW.code());
        List<String> orderCodes = new ArrayList<>();
        orderCodes.add("ORDER_CODE_1");
        orderCodes.add("ORDER_CODE_2");
        request.setOrderCodes(orderCodes);
        model = new OrderStatusImportModel();
        orderModel1 = new OrderModel();
        orderModel1.setCode("ORDER_CODE_1");
        orderModel1.setOrderStatus(OrderStatus.PRE_ORDER.code());
        orderModel2 = new OrderModel();
        orderModel2.setCode("ORDER_CODE_2");
        orderModel2.setOrderStatus(OrderStatus.PRE_ORDER.code());
    }

    @Test
    public void populate_DUPLICATE_ORDER_CODE() {
        request.getOrderCodes().add("ORDER_CODE_1");
        when(orderService.findByCompanyIdAndOrderCodeIn(anyLong(), anyList())).thenReturn(Arrays.asList(orderModel1, orderModel2));
        populator.populate(request, model);
        assertEquals(3, model.getOrderStatusImportDetailModels().size());
        assertEquals(OrderStatusImport.ERROR.toString(), model.getOrderStatusImportDetailModels().get(2).getStatus());
        assertEquals(ErrorCodes.DUPLICATE_ORDER_CODE_FOR_CHANGE_STATUS.code(), model.getOrderStatusImportDetailModels().get(2).getNote());
    }

    @Test
    public void populate_INVALID_ORDER() {
        request.getOrderCodes().add("ORDER_CODE_3");
        when(orderService.findByCompanyIdAndOrderCodeIn(anyLong(), anyList())).thenReturn(Arrays.asList(orderModel1, orderModel2));
        populator.populate(request, model);
        assertEquals(3, model.getOrderStatusImportDetailModels().size());
        assertEquals(OrderStatusImport.ERROR.toString(), model.getOrderStatusImportDetailModels().get(2).getStatus());
        assertEquals(ErrorCodes.INVALID_ORDER_ID.code(), model.getOrderStatusImportDetailModels().get(2).getNote());
    }

    @Test
    public void populate_INVALID_ORDER_STATUS_CHANGE() {
        orderModel1.setOrderStatus(OrderStatus.NEW.code());
        when(orderService.findByCompanyIdAndOrderCodeIn(anyLong(), anyList())).thenReturn(Arrays.asList(orderModel1, orderModel2));
        populator.populate(request, model);
        assertEquals(2, model.getOrderStatusImportDetailModels().size());
        assertEquals(OrderStatusImport.ERROR.toString(), model.getOrderStatusImportDetailModels().get(0).getStatus());
        assertEquals(ErrorCodes.INVALID_ORDER_STATUS_CHANGE.code(), model.getOrderStatusImportDetailModels().get(0).getNote());
    }

    @Test
    public void populate_INVALID_LOCK_ORDER_STATUS_CHANGE() {
        orderModel1.setOrderStatus(OrderStatus.CONFIRMED.code());
        orderModel1.setImportOrderProcessing(true);
        when(orderService.findByCompanyIdAndOrderCodeIn(anyLong(), anyList())).thenReturn(Arrays.asList(orderModel1, orderModel2));
        populator.populate(request, model);
        assertEquals(2, model.getOrderStatusImportDetailModels().size());
        assertEquals(OrderStatusImport.ERROR.toString(), model.getOrderStatusImportDetailModels().get(0).getStatus());
        assertEquals(ErrorCodes.CANNOT_CHANGE_ORDER_PROCESSING.code(), model.getOrderStatusImportDetailModels().get(0).getNote());
    }

    @Test
    public void populate() {
        populator.populate(request, model);

        assertEquals(model.getCompanyId(), request.getCompanyId());
        assertEquals(model.getOrderStatus(), request.getOrderStatus());
        assertEquals(model.getOrderStatusImportDetailModels().get(1).getOrderCode(), request.getOrderCodes().get(1));
    }
}