package com.vctek.orderservice.aop;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.WarehouseData;
import com.vctek.orderservice.dto.request.OrderRequest;
import com.vctek.orderservice.dto.request.PaymentTransactionRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.PermissionFacade;
import com.vctek.orderservice.model.OrderHistoryModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.ReturnOrderModel;
import com.vctek.orderservice.service.AuthService;
import com.vctek.orderservice.service.LogisticService;
import com.vctek.orderservice.service.OrderHistoryService;
import com.vctek.orderservice.service.OrderService;
import com.vctek.orderservice.util.DateUtil;
import com.vctek.util.OrderStatus;
import com.vctek.util.OrderType;
import com.vctek.util.WarehouseStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ValidateModifiedOrderAspectTest {
    @Mock
    private OrderService orderService;
    @Mock
    private OrderHistoryService orderHistoryService;
    private Date checkingTime;
    @Mock
    private OrderModel orderMock;
    @Mock
    private OrderHistoryModel orderHistoryMock;
    @Mock
    private PermissionFacade permissionFacade;
    @Mock
    private AuthService authService;
    @Mock
    private LogisticService logisticService;
    @Mock
    private WarehouseData warehouseMock;

    private ValidateModifiedOrderAspect init(Date currentTime) {
        ValidateModifiedOrderAspect validateModifiedOrderAspect = new ValidateModifiedOrderAspect(){
            @Override
            protected Calendar getCurrentCal() {
                Calendar instance = Calendar.getInstance();
                instance.setTime(currentTime);
                return instance;
            }
        };
        validateModifiedOrderAspect.setOrderHistoryService(orderHistoryService);
        validateModifiedOrderAspect.setOrderService(orderService);
        validateModifiedOrderAspect.setHourOfDayForModified(12);
        validateModifiedOrderAspect.setAuthService(authService);
        validateModifiedOrderAspect.setPermissionFacade(permissionFacade);
        validateModifiedOrderAspect.setLogisticService(logisticService);
        return validateModifiedOrderAspect;
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(orderHistoryService.findFirstSuccessStatusOf(orderMock)).thenReturn(Optional.of(orderHistoryMock));
        when(logisticService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(warehouseMock);
        when(warehouseMock.getStatus()).thenReturn(WarehouseStatus.ACTIVE.code());
    }

    @Test
    public void isValidTimeForModify_Valid_CheckingTimeIsNull() {
        ValidateModifiedOrderAspect validateModifiedOrderAspect = init(DateUtil.parseDate("2019-08-03 15:00:00",
                DateUtil.ISO_DATE_TIME_PATTERN));
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderMock);
        boolean validTimeForModify = validateModifiedOrderAspect.isValidTimeForModify(null);
        assertTrue(validTimeForModify);
    }

    @Test
    public void isValidTimeForModify_Valid_TheSameDay() {
        ValidateModifiedOrderAspect validateModifiedOrderAspect = init(DateUtil.parseDate("2019-08-03 15:00:00",
                DateUtil.ISO_DATE_TIME_PATTERN));
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderMock);
        checkingTime = DateUtil.parseDate("2019-08-03 14:00:00", DateUtil.ISO_DATE_TIME_PATTERN);
        boolean validTimeForModify = validateModifiedOrderAspect.isValidTimeForModify(checkingTime);
        assertTrue(validTimeForModify);
    }

    @Test
    public void isValidTimeForModify_ValidAtTheEndOfDay() {
        ValidateModifiedOrderAspect validateModifiedOrderAspect = init(DateUtil.parseDate("2019-08-03 23:59:59",
                DateUtil.ISO_DATE_TIME_PATTERN));
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderMock);
        checkingTime = DateUtil.parseDate("2019-08-03 12:00:00", DateUtil.ISO_DATE_TIME_PATTERN);
        boolean validTimeForModify = validateModifiedOrderAspect.isValidTimeForModify(checkingTime);
        assertTrue(validTimeForModify);
    }

    @Test
    public void isValidTimeForModify_ValidBefore12OfNextDay() {
        ValidateModifiedOrderAspect validateModifiedOrderAspect = init(DateUtil.parseDate("2019-08-04 08:59:59",
                DateUtil.ISO_DATE_TIME_PATTERN));
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderMock);
        checkingTime = DateUtil.parseDate("2019-08-03 12:00:00", DateUtil.ISO_DATE_TIME_PATTERN);
        boolean validTimeForModify = validateModifiedOrderAspect.isValidTimeForModify(checkingTime);
        assertTrue(validTimeForModify);
    }

    @Test
    public void isValidTimeForModify_ValidEqual12OfNextDay() {
        ValidateModifiedOrderAspect validateModifiedOrderAspect = init(DateUtil.parseDate("2019-08-04 12:00:00",
                DateUtil.ISO_DATE_TIME_PATTERN));
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderMock);
        checkingTime = DateUtil.parseDate("2019-08-03 12:00:00", DateUtil.ISO_DATE_TIME_PATTERN);
        boolean validTimeForModify = validateModifiedOrderAspect.isValidTimeForModify(checkingTime);
        assertTrue(validTimeForModify);
    }

    @Test
    public void isValidTimeForModify_InvalidAfter12OfNextDay() {
        ValidateModifiedOrderAspect validateModifiedOrderAspect = init(DateUtil.parseDate("2019-08-04 12:00:01",
                DateUtil.ISO_DATE_TIME_PATTERN));
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderMock);
        checkingTime = DateUtil.parseDate("2019-08-03 12:00:00", DateUtil.ISO_DATE_TIME_PATTERN);
        boolean validTimeForModify = validateModifiedOrderAspect.isValidTimeForModify(checkingTime);
        assertFalse(validTimeForModify);
    }

    @Test
    public void isValidTimeForModify_InvalidAfter12OfNextDay_LargerThan2days() {
        ValidateModifiedOrderAspect validateModifiedOrderAspect = init(DateUtil.parseDate("2019-08-05 12:00:01",
                DateUtil.ISO_DATE_TIME_PATTERN));
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderMock);
        checkingTime = DateUtil.parseDate("2019-08-03 12:00:00", DateUtil.ISO_DATE_TIME_PATTERN);
        boolean validTimeForModify = validateModifiedOrderAspect.isValidTimeForModify(checkingTime);
        assertFalse(validTimeForModify);
    }

    @Test
    public void validateTimeToModified_Retail_ValidToModify_orderNotExisted() {
        ValidateModifiedOrderAspect validateModifiedOrderAspect = init(DateUtil.parseDate("2019-08-09 09:50:01",
                DateUtil.ISO_DATE_TIME_PATTERN));
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderMock);
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(null);

        validateModifiedOrderAspect.validateValidToModified("orderCode", 1l);
        verify(orderHistoryService, times(0)).findFirstSuccessStatusOf(orderMock);
    }

    @Test
    public void validateTimeToModified_WholeSale_ValidToModify() {
        ValidateModifiedOrderAspect validateModifiedOrderAspect = init(DateUtil.parseDate("2019-08-09 09:50:01",
                DateUtil.ISO_DATE_TIME_PATTERN));
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderMock);
        when(orderMock.getType()).thenReturn(OrderType.WHOLESALE.toString());
        when(orderMock.getCreatedTime()).thenReturn(DateUtil.parseDate("2019-08-09 08:00:00",
                DateUtil.ISO_DATE_TIME_PATTERN));

        validateModifiedOrderAspect.validateValidToModified("orderCode", 1l);
        verify(orderHistoryService, times(0)).findFirstSuccessStatusOf(orderMock);
    }

    @Test
    public void validateTimeToModified_WholeSale_InValidToModify() {
        try {
            ValidateModifiedOrderAspect validateModifiedOrderAspect = init(DateUtil.parseDate("2019-08-09 09:50:01",
                    DateUtil.ISO_DATE_TIME_PATTERN));
            when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderMock);
            when(orderMock.getType()).thenReturn(OrderType.WHOLESALE.toString());
            when(orderMock.getCreatedTime()).thenReturn(DateUtil.parseDate("2019-07-09 08:00:00",
                    DateUtil.ISO_DATE_TIME_PATTERN));

            validateModifiedOrderAspect.validateValidToModified("orderCode", 1l);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.OVER_TIME_TO_MODIFY_ORDER.code(), e.getCode());
        }
    }

    @Test
    public void validateTimeToModified_Online_ValidToModify_OrderStatusNotCompleted() {
        ValidateModifiedOrderAspect validateModifiedOrderAspect = init(DateUtil.parseDate("2019-08-09 13:50:01",
                DateUtil.ISO_DATE_TIME_PATTERN));
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderMock);
        when(orderMock.getType()).thenReturn(OrderType.ONLINE.toString());
        when(orderMock.getOrderStatus()).thenReturn(OrderStatus.CONFIRMED.code());
        when(orderMock.getCreatedTime()).thenReturn(DateUtil.parseDate("2019-08-09 08:00:00",
                DateUtil.ISO_DATE_TIME_PATTERN));
        when(orderHistoryMock.getModifiedTime()).thenReturn(DateUtil.parseDate("2019-08-09 12:00:00",
                DateUtil.ISO_DATE_TIME_PATTERN));
        validateModifiedOrderAspect.validateValidToModified("orderCode", 1l);
        verify(orderHistoryService, times(0)).findFirstSuccessStatusOf(orderMock);
    }

    @Test
    public void validateTimeToModified_Online_ValidToModify() {
        ValidateModifiedOrderAspect validateModifiedOrderAspect = init(DateUtil.parseDate("2019-08-09 13:50:01",
                DateUtil.ISO_DATE_TIME_PATTERN));
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderMock);
        when(orderMock.getType()).thenReturn(OrderType.ONLINE.toString());
        when(orderMock.getOrderStatus()).thenReturn(OrderStatus.COMPLETED.code());
        when(orderMock.getCreatedTime()).thenReturn(DateUtil.parseDate("2019-08-09 08:00:00",
                DateUtil.ISO_DATE_TIME_PATTERN));
        when(orderHistoryMock.getModifiedTime()).thenReturn(DateUtil.parseDate("2019-08-09 12:00:00",
                DateUtil.ISO_DATE_TIME_PATTERN));
        validateModifiedOrderAspect.validateValidToModified("orderCode", 1l);
        verify(orderHistoryService, times(1)).findFirstSuccessStatusOf(orderMock);
    }

    @Test
    public void validateTimeToModified_Online_ValidToModify_NotChangeToCompleted() {
        ValidateModifiedOrderAspect validateModifiedOrderAspect = init(DateUtil.parseDate("2019-08-09 13:50:01",
                DateUtil.ISO_DATE_TIME_PATTERN));
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderMock);
        when(orderMock.getType()).thenReturn(OrderType.ONLINE.toString());
        when(orderMock.getOrderStatus()).thenReturn(OrderStatus.COMPLETED.code());
        when(orderMock.getCreatedTime()).thenReturn(DateUtil.parseDate("2019-08-09 08:00:00",
                DateUtil.ISO_DATE_TIME_PATTERN));
        when(orderHistoryService.findFirstSuccessStatusOf(orderMock)).thenReturn(Optional.empty());

        validateModifiedOrderAspect.validateValidToModified("orderCode", 1l);
        verify(orderHistoryService, times(1)).findFirstSuccessStatusOf(orderMock);
    }

    @Test
    public void validateTimeToModified_Online_InValidToModify() {
        try {
            ValidateModifiedOrderAspect validateModifiedOrderAspect = init(DateUtil.parseDate("2019-08-10 12:50:01",
                    DateUtil.ISO_DATE_TIME_PATTERN));
            when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderMock);
            when(orderMock.getType()).thenReturn(OrderType.ONLINE.toString());
            when(orderMock.getOrderStatus()).thenReturn(OrderStatus.COMPLETED.code());
            when(orderMock.getCreatedTime()).thenReturn(DateUtil.parseDate("2019-08-09 08:00:00",
                    DateUtil.ISO_DATE_TIME_PATTERN));
            when(orderHistoryMock.getModifiedTime()).thenReturn(DateUtil.parseDate("2019-08-09 12:00:00",
                    DateUtil.ISO_DATE_TIME_PATTERN));

            validateModifiedOrderAspect.validateValidToModified("orderCode", 1l);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.OVER_TIME_TO_MODIFY_ORDER.code(), e.getCode());
        }
    }

    @Test
    public void updatePriceOrderEntry_has_returnOrder() {
        try {
            ValidateModifiedOrderAspect validateModifiedOrderAspect = init(DateUtil.parseDate("2019-08-10 12:50:01",
                    DateUtil.ISO_DATE_TIME_PATTERN));
            when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderMock);
            ReturnOrderModel returnOrderModel = new ReturnOrderModel();
            returnOrderModel.setId(1l);
            when(orderMock.getReturnOrders()).thenReturn(new HashSet<>(Arrays.asList(returnOrderModel)));
            validateModifiedOrderAspect.validateValidToModified("code", 1l);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CANNOT_UPDATE_BECAUSE_ORDER_HAS_RETURN_ORDER.code(), e.getCode());
        }
    }

    @Test
    public void updatePriceOrderEntry_has_lockOrderOnline() {
        try {
            ValidateModifiedOrderAspect validateModifiedOrderAspect = init(DateUtil.parseDate("2019-08-10 12:50:01",
                    DateUtil.ISO_DATE_TIME_PATTERN));
            when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderMock);
            when(orderMock.getType()).thenReturn(OrderType.ONLINE.toString());
            when(orderMock.isImportOrderProcessing()).thenReturn(true);
            validateModifiedOrderAspect.validateValidToModified("code", 1l);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.ORDER_PROCESS_IMPORT_CHANGE_STATUS_CANNOT_UPDATE.code(), e.getCode());
        }
    }

    @Test
    public void validateCreateRedeemOnline_requestHasNotRedeem() {
        ValidateModifiedOrderAspect validateModifiedOrderAspect = init(DateUtil.parseDate("2019-08-10 12:50:01",
                DateUtil.ISO_DATE_TIME_PATTERN));
        OrderModel orderModel = new OrderModel();
        orderModel.setOrderStatus(OrderStatus.NEW.code());
        orderModel.setType(OrderType.ONLINE.toString());
        PaymentTransactionRequest request = new PaymentTransactionRequest();
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderModel);
        validateModifiedOrderAspect.validateCreateRedeemOnline("code", 1L, request);
        verify(orderService, times(0)).findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean());

    }

    @Test
    public void validateCreateRedeemOnline_OrderHasReturnOrder() {
        try {
            ValidateModifiedOrderAspect validateModifiedOrderAspect = init(DateUtil.parseDate("2019-08-10 12:50:01",
                    DateUtil.ISO_DATE_TIME_PATTERN));
            ReturnOrderModel returnOrderModel = new ReturnOrderModel();
            Set<ReturnOrderModel> returnOrderModels = new HashSet<>();
            returnOrderModels.add(returnOrderModel);
            OrderModel orderModel = new OrderModel();
            orderModel.setOrderStatus(OrderStatus.NEW.code());
            orderModel.setType(OrderType.ONLINE.toString());
            orderModel.setReturnOrders(returnOrderModels);
            PaymentTransactionRequest request = new PaymentTransactionRequest();
            request.setAmount(1d);
            when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderModel);
            validateModifiedOrderAspect.validateCreateRedeemOnline("code", 1L, request);
            verify(orderService, times(1)).findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean());
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CANNOT_UPDATE_BECAUSE_ORDER_HAS_RETURN_ORDER.code(), e.getCode());
        }

    }

    @Test
    public void validateCreateRedeemOnline_OrderNotOnline() {
        try {
            ValidateModifiedOrderAspect validateModifiedOrderAspect = init(DateUtil.parseDate("2019-08-10 12:50:01",
                    DateUtil.ISO_DATE_TIME_PATTERN));
            OrderModel orderModel = new OrderModel();
            orderModel.setOrderStatus(OrderStatus.NEW.code());
            orderModel.setType(OrderType.RETAIL.toString());
            PaymentTransactionRequest request = new PaymentTransactionRequest();
            request.setAmount(1d);
            when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderModel);
            validateModifiedOrderAspect.validateCreateRedeemOnline("code", 1L, request);
            verify(orderService, times(1)).findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean());
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.NOT_ACCEPT_MODIFIED_ORDER.code(), e.getCode());
        }

    }

    @Test
    public void validateCreateRedeemOnline_OrderStatusGreaterThanOrEqualConfirmedButNotEqualCompleted() {
        try {
            ValidateModifiedOrderAspect validateModifiedOrderAspect = init(DateUtil.parseDate("2019-08-10 12:50:01",
                    DateUtil.ISO_DATE_TIME_PATTERN));
            OrderModel orderModel = new OrderModel();
            orderModel.setOrderStatus(OrderStatus.CONFIRMED.code());
            orderModel.setType(OrderType.ONLINE.toString());
            PaymentTransactionRequest request = new PaymentTransactionRequest();
            request.setAmount(1d);
            when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderModel);
            validateModifiedOrderAspect.validateCreateRedeemOnline("code", 1L, request);
            verify(orderService, times(1)).findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean());
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.NOT_ACCEPT_MODIFIED_ORDER.code(), e.getCode());
        }

    }

    @Test
    public void validateCreateRedeemOnline_OrderStatusSmallerThanConfirmed() {
        ValidateModifiedOrderAspect validateModifiedOrderAspect = init(DateUtil.parseDate("2019-08-10 12:50:01",
                DateUtil.ISO_DATE_TIME_PATTERN));
        OrderModel orderModel = new OrderModel();
        orderModel.setOrderStatus(OrderStatus.NEW.code());
        orderModel.setType(OrderType.ONLINE.toString());
        PaymentTransactionRequest request = new PaymentTransactionRequest();
        request.setAmount(1d);
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderModel);
        validateModifiedOrderAspect.validateCreateRedeemOnline("code", 1L, request);
        verify(orderService, times(1)).findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean());

    }

    @Test
    public void validateCreateRedeemOnline_OrderStatusIsCompleted() {
        ValidateModifiedOrderAspect validateModifiedOrderAspect = init(DateUtil.parseDate("2019-08-10 12:50:01",
                DateUtil.ISO_DATE_TIME_PATTERN));
        OrderModel orderModel = new OrderModel();
        orderModel.setOrderStatus(OrderStatus.NEW.code());
        orderModel.setType(OrderType.ONLINE.toString());
        PaymentTransactionRequest request = new PaymentTransactionRequest();
        request.setAmount(1d);
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderModel);
        validateModifiedOrderAspect.validateCreateRedeemOnline("code", 1L, request);
        verify(orderService, times(1)).findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean());

    }


    @Test
    public void validateCancelRedeemOnline_OrderHasReturnOrder() {
        try {
            ValidateModifiedOrderAspect validateModifiedOrderAspect = init(DateUtil.parseDate("2019-08-10 12:50:01",
                    DateUtil.ISO_DATE_TIME_PATTERN));
            ReturnOrderModel returnOrderModel = new ReturnOrderModel();
            Set<ReturnOrderModel> returnOrderModels = new HashSet<>();
            returnOrderModels.add(returnOrderModel);
            OrderModel orderModel = new OrderModel();
            orderModel.setOrderStatus(OrderStatus.NEW.code());
            orderModel.setType(OrderType.ONLINE.toString());
            orderModel.setReturnOrders(returnOrderModels);
            when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderModel);
            validateModifiedOrderAspect.validateCancelRedeemOnline("code", 1L);
            verify(orderService, times(1)).findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean());
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CANNOT_UPDATE_BECAUSE_ORDER_HAS_RETURN_ORDER.code(), e.getCode());
        }

    }

    @Test
    public void validateCancelRedeemOnline_OrderNotOnline() {
        try {
            ValidateModifiedOrderAspect validateModifiedOrderAspect = init(DateUtil.parseDate("2019-08-10 12:50:01",
                    DateUtil.ISO_DATE_TIME_PATTERN));
            OrderModel orderModel = new OrderModel();
            orderModel.setOrderStatus(OrderStatus.NEW.code());
            orderModel.setType(OrderType.RETAIL.toString());
            when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderModel);
            validateModifiedOrderAspect.validateCancelRedeemOnline("code", 1L);
            verify(orderService, times(1)).findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean());
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.NOT_ACCEPT_MODIFIED_ORDER.code(), e.getCode());
        }

    }

    @Test
    public void validateCreateRedeemOnline_OrderStatusGreaterThanOrEqualConfirmed() {
        try {
            ValidateModifiedOrderAspect validateModifiedOrderAspect = init(DateUtil.parseDate("2019-08-10 12:50:01",
                    DateUtil.ISO_DATE_TIME_PATTERN));
            OrderModel orderModel = new OrderModel();
            orderModel.setOrderStatus(OrderStatus.CONFIRMED.code());
            orderModel.setType(OrderType.ONLINE.toString());
            when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderModel);
            validateModifiedOrderAspect.validateCancelRedeemOnline("code", 1L);
            verify(orderService, times(1)).findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean());
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.NOT_ACCEPT_MODIFIED_ORDER.code(), e.getCode());
        }

    }

    @Test
    public void validateCancelRedeemOnline_OrderStatusSmallerThanConfirmed() {
        ValidateModifiedOrderAspect validateModifiedOrderAspect = init(DateUtil.parseDate("2019-08-10 12:50:01",
                DateUtil.ISO_DATE_TIME_PATTERN));
        OrderModel orderModel = new OrderModel();
        orderModel.setOrderStatus(OrderStatus.NEW.code());
        orderModel.setType(OrderType.ONLINE.toString());
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderModel);
        validateModifiedOrderAspect.validateCancelRedeemOnline("code", 1L);
        verify(orderService, times(1)).findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean());

    }

    @Test
    public void validateUpdateSettingCustomerAndShippingFee() {
        try {
            ValidateModifiedOrderAspect validateModifiedOrderAspect = init(DateUtil.parseDate("2019-08-10 12:50:01",
                    DateUtil.ISO_DATE_TIME_PATTERN));
            OrderModel orderModel = new OrderModel();
            orderModel.setOrderStatus(OrderStatus.CONFIRMED.code());
            orderModel.setType(OrderType.ONLINE.toString());
            orderModel.setImportOrderProcessing(true);
            OrderRequest request = new OrderRequest();
            request.setCode("code");
            request.setCompanyId(2L);
            when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderModel);
            validateModifiedOrderAspect.validateUpdateSettingCustomerAndShippingFee(request);
            verify(orderService, times(1)).findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean());
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.ORDER_PROCESS_IMPORT_CHANGE_STATUS_CANNOT_UPDATE.message(), e.getMessage());
        }
    }

}
