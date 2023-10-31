package com.vctek.orderservice.facade.impl;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.CheckPermissionData;
import com.vctek.orderservice.dto.OrderEntryDTO;
import com.vctek.orderservice.dto.request.CheckPermissionRequest;
import com.vctek.orderservice.dto.request.OrderSearchRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.CheckPermissionClient;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.service.AuthService;
import com.vctek.orderservice.service.CartService;
import com.vctek.orderservice.service.OrderService;
import com.vctek.util.OrderStatus;
import com.vctek.util.OrderType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class PermissionFacadeTest {
    private PermissionFacadeImpl facade;

    @Mock
    private CheckPermissionClient permissionClient;
    @Mock
    private AuthService authService;
    @Mock
    private OrderService orderService;
    @Mock
    private CartService cartService;
    @Mock
    private CheckPermissionData data;
    @Mock
    private OrderModel order;
    @Mock
    private CartModel cart;
    @Mock
    private OrderSearchRequest orderSearchReqMock;
    @Mock
    private CheckPermissionData checkPermissionDataMock;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        facade = new PermissionFacadeImpl(permissionClient);
        facade.setCartService(cartService);
        facade.setOrderService(orderService);
        facade.setAuthService(authService);
        when(data.getPermission()).thenReturn(true);
        when(authService.getCurrentUserId()).thenReturn(1l);
        when(permissionClient.checkPermission(any())).thenReturn(checkPermissionDataMock);
        facade.init();
    }

    @Test
    public void checkPermission_returnNull() {
        when(permissionClient.checkPermission(any(CheckPermissionRequest.class))).thenReturn(null);

        assertFalse(facade.checkPermission(new CheckPermissionRequest()));
    }

    @Test
    public void checkPermission_returnTrue() {
        when(permissionClient.checkPermission(any(CheckPermissionRequest.class))).thenReturn(data);
        assertTrue(facade.checkPermission(new CheckPermissionRequest()));
    }

    @Test
    public void checkUpdateOrder_NotFoundOrder() {
        try {
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(null);
            facade.checkUpdateOrder(2l, "orderCode");
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_CODE.code(), e.getCode());
        }
    }

    @Test
    public void checkUpdateOrder_HasNotCompanyId() {
        try {
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
            facade.checkUpdateOrder(null, "orderCode");
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_COMPANY_ID.code(), e.getCode());
        }
    }

    @Test
    public void checkUpdateOrder_HasNotPermission_OnRetailOrder() {
        try {
            when(order.getType()).thenReturn(OrderType.RETAIL.toString());
            when(order.getOrderStatus()).thenReturn(OrderStatus.COMPLETED.code());
            when(permissionClient.checkPermission(any(CheckPermissionRequest.class))).thenReturn(null);
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
            facade.checkUpdateOrder(2l, "orderCode");
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.HAS_NOT_PERMISSION_TO_UPDATE_RETAIL_BILL.code(), e.getCode());
        }
    }

    @Test
    public void checkUpdateOrder_HasNotPermission_OnOnlineOrder() {
        try {
            when(order.getType()).thenReturn(OrderType.ONLINE.toString());
            when(order.getOrderStatus()).thenReturn(OrderStatus.COMPLETED.code());
            when(permissionClient.checkPermission(any(CheckPermissionRequest.class))).thenReturn(null);
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
            facade.checkUpdateOrder(2l, "orderCode");
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.HAS_NOT_PERMISSION_TO_UPDATE_ORDER.code(), e.getCode());
        }
    }

    @Test
    public void checkUpdateOrder_HasNotPermission_OnWholesaleOrder() {
        try {
            when(order.getType()).thenReturn(OrderType.WHOLESALE.toString());
            when(order.getOrderStatus()).thenReturn(OrderStatus.COMPLETED.code());
            when(permissionClient.checkPermission(any(CheckPermissionRequest.class))).thenReturn(null);
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
            facade.checkUpdateOrder(2l, "orderCode");
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.HAS_NOT_PERMISSION_TO_UPDATE_WHOLESALE_BILL.code(), e.getCode());
        }
    }


    @Test
    public void checkUpdateOrder_HasPermission_OnWholeSaleOrder() {
        when(order.getType()).thenReturn(OrderType.WHOLESALE.toString());
        when(order.getOrderStatus()).thenReturn(OrderStatus.COMPLETED.code());
        when(permissionClient.checkPermission(any(CheckPermissionRequest.class))).thenReturn(data);
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
        facade.checkUpdateOrder(2l, "orderCode");
    }

    @Test
    public void checkUpdateOrder_HasPermissionOnOrder_ButOrderStatusHadConfirmed_CannotUpdate() {
        when(order.getType()).thenReturn(OrderType.ONLINE.toString());
        when(order.getCompanyId()).thenReturn(2l);
        CheckPermissionData checkPermissionData = new CheckPermissionData();
        checkPermissionData.setPermission(false);
        when(permissionClient.checkPermission(any(CheckPermissionRequest.class))).thenReturn(data).thenReturn(checkPermissionData);
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
        when(order.getOrderStatus()).thenReturn(OrderStatus.CONFIRMED.code());
        try {
            facade.checkUpdateOrder(2l, "orderCode");
            fail("Must throw exeption");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CANNOT_UPDATE_ONLINE_ORDER.message(), e.getMessage());
        }
    }

    @Test
    public void checkViewRetailBillDetail_hasNotPermission() {
        try {
            when(data.getPermission()).thenReturn(false);
            when(order.getType()).thenReturn(OrderType.RETAIL.toString());
            when(permissionClient.checkPermission(any(CheckPermissionRequest.class))).thenReturn(data);
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);

            facade.checkViewOrderDetail(2l, "orderCode");
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.HAS_NOT_PERMISSION_TO_VIEW_DETAIL_RETAIL_BILL.code(), e.getCode());
        }
    }

    @Test
    public void checkViewOrderDetail_hasNotPermission() {
        try {
            when(data.getPermission()).thenReturn(false);
            when(order.getType()).thenReturn(OrderType.ONLINE.toString());
            when(permissionClient.checkPermission(any(CheckPermissionRequest.class))).thenReturn(data);
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);

            facade.checkViewOrderDetail(2l, "orderCode");
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.HAS_NOT_PERMISSION_TO_VIEW_DETAIL_ORDER.code(), e.getCode());
        }
    }

    @Test
    public void checkViewOrderDetail_hasViewDetailPermission_NotViewOtherUserOrder_ShouldThrowException() {
        try {
            when(data.getPermission()).thenReturn(true, false);
            when(order.getType()).thenReturn(OrderType.RETAIL.toString());
            when(permissionClient.checkPermission(any(CheckPermissionRequest.class))).thenReturn(data);
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
            when(order.getCreateByUser()).thenReturn(1l);
            when(authService.getCurrentUserId()).thenReturn(2l);

            facade.checkViewOrderDetail(2l, "orderCode");
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CANNOT_VIEW_ORDER_NOT_BELONG_TO_ACCOUNT.code(), e.getCode());
        }
    }

    @Test
    public void checkViewOrderDetail_hasViewDetailPermission_CanViewOwnerOrder() {
        when(data.getPermission()).thenReturn(true, false);
        when(order.getType()).thenReturn(OrderType.RETAIL.toString());
        when(permissionClient.checkPermission(any(CheckPermissionRequest.class))).thenReturn(data);
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
        when(order.getCreateByUser()).thenReturn(1l);
        when(authService.getCurrentUserId()).thenReturn(1l);

        facade.checkViewOrderDetail(2l, "orderCode");
        assertTrue("Can view owner's order", true);
    }

    @Test
    public void checkViewWholesaleBillDetail_hasNotPermission() {
        try {
            when(data.getPermission()).thenReturn(false);
            when(order.getType()).thenReturn(OrderType.WHOLESALE.toString());
            when(permissionClient.checkPermission(any(CheckPermissionRequest.class))).thenReturn(data);
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);

            facade.checkViewOrderDetail(2l, "orderCode");
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.HAS_NOT_PERMISSION_TO_VIEW_DETAIL_WHOLESALE_BILL.code(), e.getCode());
        }
    }


    @Test
    public void checkUpdateRetailDiscount_hasNotPermission() {
        try {
            when(data.getPermission()).thenReturn(false);
            when(order.getType()).thenReturn(OrderType.RETAIL.toString());
            when(permissionClient.checkPermission(any(CheckPermissionRequest.class))).thenReturn(data);
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);

            facade.checkUpdateOrderDiscount(2l, "orderCode");
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.HAS_NOT_PERMISSION_TO_EDIT_RETAIL_BILL_DISCOUNT.code(), e.getCode());
        }
    }


    @Test
    public void checkUpdateOrderDiscount_hasNotPermission() {
        try {
            when(data.getPermission()).thenReturn(false);
            when(order.getType()).thenReturn(OrderType.ONLINE.toString());
            when(permissionClient.checkPermission(any(CheckPermissionRequest.class))).thenReturn(data);
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);

            facade.checkUpdateOrderDiscount(2l, "orderCode");
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.HAS_NOT_PERMISSION_TO_EDIT_ORDER_DISCOUNT.code(), e.getCode());
        }
    }

    @Test
    public void checkUpdateWholesaleDiscount_hasNotPermission() {
        try {
            when(data.getPermission()).thenReturn(false);
            when(order.getType()).thenReturn(OrderType.WHOLESALE.toString());
            when(permissionClient.checkPermission(any(CheckPermissionRequest.class))).thenReturn(data);
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);

            facade.checkUpdateOrderDiscount(2l, "orderCode");
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.HAS_NOT_PERMISSION_TO_EDIT_WHOLESALE_BILL_DISCOUNT.code(), e.getCode());
        }
    }

    @Test
    public void checkUpdateOrderPrice_hasNotPermissionEditPriceOfNormalEntry() {
        List<String> orderType = Arrays.asList(OrderType.RETAIL.toString(), OrderType.ONLINE.toString(),
                OrderType.WHOLESALE.toString());
        for (String type : orderType) {
            try {
                when(data.getPermission()).thenReturn(false);
                when(order.getType()).thenReturn(type);
                when(permissionClient.checkPermission(any(CheckPermissionRequest.class))).thenReturn(data);
                when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
                OrderEntryDTO orderEntryDTO = new OrderEntryDTO();
                orderEntryDTO.setCompanyId(2L);
                facade.checkUpdateOrderPrice(orderEntryDTO, "orderCode");
                fail("Must throw exception");
            } catch (ServiceException e) {
                switch (type) {
                    case "RETAIL":
                        assertEquals(ErrorCodes.HAS_NOT_PERMISSION_TO_EDIT_RETAIL_BILL_PRICE.code(), e.getCode());
                        break;
                    case "ONLINE":
                        assertEquals(ErrorCodes.HAS_NOT_PERMISSION_TO_EDIT_ORDER_PRICE.code(), e.getCode());
                        break;
                    case "WHOLESALE":
                        assertEquals(ErrorCodes.HAS_NOT_PERMISSION_TO_EDIT_WHOLESALE_BILL_PRICE.code(), e.getCode());
                        break;
                    default:
                        fail("Must throw exception");
                }

            }
        }
    }

    @Test
    public void checkUpdateOrderPrice_hasNotPermissionEditPriceOfNormalEntry_HasPermissionToEditComboPrice_NotAcceptEditPriceNormalEntry() {
        List<String> orderType = Arrays.asList(OrderType.RETAIL.toString(), OrderType.ONLINE.toString(),
                OrderType.WHOLESALE.toString());
        for (String type : orderType) {
            try {
                when(data.getPermission()).thenReturn(false, true);
                when(order.getType()).thenReturn(type);
                when(permissionClient.checkPermission(any(CheckPermissionRequest.class))).thenReturn(data);
                when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
                OrderEntryDTO orderEntryDTO = new OrderEntryDTO();
                orderEntryDTO.setCompanyId(2L);
                facade.checkUpdateOrderPrice(orderEntryDTO, "orderCode");
                fail("Must throw exception");
            } catch (ServiceException e) {
                switch (type) {
                    case "RETAIL":
                        assertEquals(ErrorCodes.HAS_NOT_PERMISSION_TO_EDIT_RETAIL_BILL_PRICE.code(), e.getCode());
                        break;
                    case "ONLINE":
                        assertEquals(ErrorCodes.HAS_NOT_PERMISSION_TO_EDIT_ORDER_PRICE.code(), e.getCode());
                        break;
                    case "WHOLESALE":
                        assertEquals(ErrorCodes.HAS_NOT_PERMISSION_TO_EDIT_WHOLESALE_BILL_PRICE.code(), e.getCode());
                        break;
                    default:
                        fail("Must throw exception");
                }

            }
        }
    }

    @Test
    public void checkUpdateOrderPrice_hasNotPermissionEditPriceOfNormalEntry_HasNotPermissionToEditComboPrice_NotAcceptEditPriceComboEntry() {
        List<String> orderType = Arrays.asList(OrderType.RETAIL.toString(), OrderType.ONLINE.toString(),
                OrderType.WHOLESALE.toString());
        for (String type : orderType) {
            try {
                when(data.getPermission()).thenReturn(false, false);
                when(order.getType()).thenReturn(type);
                when(permissionClient.checkPermission(any(CheckPermissionRequest.class))).thenReturn(data);
                when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
                OrderEntryDTO orderEntryDTO = new OrderEntryDTO();
                orderEntryDTO.setCompanyId(2L);
                orderEntryDTO.setCombo(true);
                facade.checkUpdateOrderPrice(orderEntryDTO, "orderCode");
                fail("Must throw exception");
            } catch (ServiceException e) {
                switch (type) {
                    case "RETAIL":
                        assertEquals(ErrorCodes.HAS_NOT_PERMISSION_TO_EDIT_RETAIL_PRICE_COMBO.code(), e.getCode());
                        break;
                    case "ONLINE":
                        assertEquals(ErrorCodes.HAS_NOT_PERMISSION_TO_EDIT_ORDER_PRICE_COMBO.code(), e.getCode());
                        break;
                    case "WHOLESALE":
                        assertEquals(ErrorCodes.HAS_NOT_PERMISSION_TO_EDIT_WHOLESALE_PRICE_COMBO.code(), e.getCode());
                        break;
                    default:
                        fail("Must throw exception");
                }

            }
        }
    }

    @Test
    public void checkUpdateOrderPrice_hasPermissionEditPriceOfNormalEntry_HasNotPermissionToEditComboPrice_AcceptEditPriceComboEntry() {
        List<String> orderType = Arrays.asList(OrderType.RETAIL.toString(), OrderType.ONLINE.toString(),
                OrderType.WHOLESALE.toString());
        for (String type : orderType) {
            if(OrderType.ONLINE.toString().equals(type)) {
                when(order.getOrderStatus()).thenReturn(OrderStatus.NEW.code());
            }
            when(data.getPermission()).thenReturn(true, false);
            when(order.getType()).thenReturn(type);
            when(permissionClient.checkPermission(any(CheckPermissionRequest.class))).thenReturn(data);
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
            OrderEntryDTO orderEntryDTO = new OrderEntryDTO();
            orderEntryDTO.setCompanyId(2L);
            orderEntryDTO.setCombo(true);
            facade.checkUpdateOrderPrice(orderEntryDTO, "orderCode");
            assertTrue("success", true);
        }
    }

    @Test
    public void checkUpdateOrderPrice_HasNotPermissionEditPriceOnExchange_NotAcceptEditPriceOfEntry() {
        List<String> orderType = Arrays.asList(OrderType.RETAIL.toString(), OrderType.ONLINE.toString(),
                OrderType.WHOLESALE.toString());
        for (String type : orderType) {
            if(OrderType.ONLINE.toString().equals(type)) {
                when(order.getOrderStatus()).thenReturn(OrderStatus.NEW.code());
            }
            when(data.getPermission()).thenReturn(false);
            when(order.getType()).thenReturn(type);
            when(order.isExchange()).thenReturn(true);
            when(permissionClient.checkPermission(any(CheckPermissionRequest.class))).thenReturn(data);
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
            OrderEntryDTO orderEntryDTO = new OrderEntryDTO();
            orderEntryDTO.setCompanyId(2L);
            orderEntryDTO.setCombo(true);
            try {
                facade.checkUpdateOrderPrice(orderEntryDTO, "orderCode");
                fail("Must throw exception");
            } catch (ServiceException e) {
                assertEquals(ErrorCodes.HAS_NOT_PERMISSION_EDIT_PRICE_ON_EXCHANGE.message(), e.getMessage());
            }
        }
    }

    @Test
    public void checkUpdateOrderPrice_HasPermissionEditPriceOnExchange_AcceptEditPriceOfEntry() {
        List<String> orderType = Arrays.asList(OrderType.RETAIL.toString(), OrderType.ONLINE.toString(),
                OrderType.WHOLESALE.toString());
        for (String type : orderType) {
            if(OrderType.ONLINE.toString().equals(type)) {
                when(order.getOrderStatus()).thenReturn(OrderStatus.NEW.code());
            }
            when(data.getPermission()).thenReturn(true);
            when(order.getType()).thenReturn(type);
            when(order.isExchange()).thenReturn(true);
            when(permissionClient.checkPermission(any(CheckPermissionRequest.class))).thenReturn(data);
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
            OrderEntryDTO orderEntryDTO = new OrderEntryDTO();
            orderEntryDTO.setCompanyId(2L);
            orderEntryDTO.setCombo(true);
            facade.checkUpdateOrderPrice(orderEntryDTO, "orderCode");
            assertTrue("success", true);
        }
    }

    @Test
    public void checkUpdateOrderPrice_hasNotPermissionEditPriceOfNormalEntry_HasPermissionToEditComboPrice_AcceptEditPriceComboEntry() {
        List<String> orderType = Arrays.asList(OrderType.RETAIL.toString(), OrderType.ONLINE.toString(),
                OrderType.WHOLESALE.toString());
        for (String type : orderType) {
            when(data.getPermission()).thenReturn(false, true);
            when(order.getType()).thenReturn(type);
            when(permissionClient.checkPermission(any(CheckPermissionRequest.class))).thenReturn(data);
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
            OrderEntryDTO orderEntryDTO = new OrderEntryDTO();
            orderEntryDTO.setCompanyId(2L);
            orderEntryDTO.setCombo(true);
            facade.checkUpdateOrderPrice(orderEntryDTO, "orderCode");
            assertTrue("success", true);
        }
    }

    @Test
    public void checkUpdateCartDiscount_hasNotPermission() {
        List<String> orderType = Arrays.asList(OrderType.RETAIL.toString(), OrderType.ONLINE.toString(),
                OrderType.WHOLESALE.toString());
        for (String orderCode : orderType) {
            try {
                when(data.getPermission()).thenReturn(false);
                when(cart.getType()).thenReturn(orderCode);
                when(permissionClient.checkPermission(any(CheckPermissionRequest.class))).thenReturn(data);
                when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);

                facade.checkUpdateCartDiscount(2l, "cartCode");
                fail("Must throw exception");
            } catch (ServiceException e) {
                switch (orderCode) {
                    case "RETAIL":
                        assertEquals(ErrorCodes.HAS_NOT_PERMISSION_TO_EDIT_DISCOUNT_NEW_RETAIL_BILL.code(), e.getCode());
                        break;
                    case "ONLINE":
                        assertEquals(ErrorCodes.HAS_NOT_PERMISSION_TO_EDIT_DISCOUNT_NEW_ONLINE_BILL.code(), e.getCode());
                        break;
                    case "WHOLESALE":
                        assertEquals(ErrorCodes.HAS_NOT_PERMISSION_TO_EDIT_DISCOUNT_NEW_WHOLESALE_BILL.code(), e.getCode());
                        break;
                    default:
                        fail("Must throw exception");
                }

            }
        }
    }

    @Test
    public void checkUpdateExchangeCartDiscount_hasNotPermission() {
        List<String> orderType = Arrays.asList(OrderType.RETAIL.toString(), OrderType.ONLINE.toString(),
                OrderType.WHOLESALE.toString());
        for (String orderCode : orderType) {
            try {
                when(data.getPermission()).thenReturn(false);
                when(cart.getType()).thenReturn(orderCode);
                when(cart.isExchange()).thenReturn(true);
                when(permissionClient.checkPermission(any(CheckPermissionRequest.class))).thenReturn(data);
                when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);

                facade.checkUpdateCartDiscount(2l, "cartCode");
                fail("Must throw exception");
            } catch (ServiceException e) {
                switch (orderCode) {
                    case "RETAIL":
                        assertEquals(ErrorCodes.HAS_NOT_PERMISSION_EDIT_DISCOUNT_ON_EXCHANGE.code(), e.getCode());
                        break;
                    case "ONLINE":
                        assertEquals(ErrorCodes.HAS_NOT_PERMISSION_EDIT_DISCOUNT_ON_EXCHANGE.code(), e.getCode());
                        break;
                    case "WHOLESALE":
                        assertEquals(ErrorCodes.HAS_NOT_PERMISSION_EDIT_DISCOUNT_ON_EXCHANGE.code(), e.getCode());
                        break;
                    default:
                        fail("Must throw exception");
                }

            }
        }
    }

    @Test
    public void checkUpdateExchangeCartDiscount_hasPermission() {
        List<String> orderType = Arrays.asList(OrderType.RETAIL.toString(), OrderType.ONLINE.toString(),
                OrderType.WHOLESALE.toString());
        for (String orderCode : orderType) {
            when(data.getPermission()).thenReturn(true);
            when(cart.getType()).thenReturn(orderCode);
            when(cart.isExchange()).thenReturn(true);
            when(permissionClient.checkPermission(any(CheckPermissionRequest.class))).thenReturn(data);
            when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);

            facade.checkUpdateCartDiscount(2l, "cartCode");
            assertTrue("success", true);
        }
    }

    @Test
    public void checkUpdateCartPrice_hasNotPermission() {
        List<String> orderType = Arrays.asList(OrderType.RETAIL.toString(), OrderType.ONLINE.toString(),
                OrderType.WHOLESALE.toString());
        for (String orderCode : orderType) {

            try {
                when(data.getPermission()).thenReturn(false);
                when(cart.getType()).thenReturn(orderCode);
                when(permissionClient.checkPermission(any(CheckPermissionRequest.class))).thenReturn(data);
                when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
                OrderEntryDTO orderEntryDTO = new OrderEntryDTO();
                orderEntryDTO.setCompanyId(2L);
                facade.checkUpdateCartPrice(orderEntryDTO, "cartCode");
                fail("Must throw exception");
            } catch (ServiceException e) {
                switch (orderCode) {
                    case "RETAIL":
                        assertEquals(ErrorCodes.HAS_NOT_PERMISSION_TO_EDIT_RETAIL_BILL_PRICE.code(), e.getCode());
                        break;
                    case "ONLINE":
                        assertEquals(ErrorCodes.HAS_NOT_PERMISSION_TO_EDIT_ORDER_PRICE.code(), e.getCode());
                        break;
                    case "WHOLESALE":
                        assertEquals(ErrorCodes.HAS_NOT_PERMISSION_TO_EDIT_WHOLESALE_BILL_PRICE.code(), e.getCode());
                        break;
                    default:
                        fail("Must throw exception");
                }

            }
        }
    }

    @Test
    public void checkUpdateCartPriceInExchangeOrder_hasNotPermission() {
        List<String> orderType = Arrays.asList(OrderType.RETAIL.toString(), OrderType.ONLINE.toString(),
                OrderType.WHOLESALE.toString());
        for (String orderCode : orderType) {
            try {
                when(data.getPermission()).thenReturn(false);
                when(cart.getType()).thenReturn(orderCode);
                when(cart.isExchange()).thenReturn(true);
                when(permissionClient.checkPermission(any(CheckPermissionRequest.class))).thenReturn(data);
                when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
                OrderEntryDTO orderEntryDTO = new OrderEntryDTO();
                orderEntryDTO.setCompanyId(2L);
                facade.checkUpdateCartPrice(orderEntryDTO, "cartCode");
                fail("Must throw exception");
            } catch (ServiceException e) {
                assertEquals(ErrorCodes.HAS_NOT_PERMISSION_EDIT_PRICE_ON_EXCHANGE.code(), e.getCode());
            }
        }
    }

    @Test
    public void checkUpdateCartPriceOfComboEntry_hasNotPermissionEditPriceAndCombo() {
        List<String> orderType = Arrays.asList(OrderType.RETAIL.toString(), OrderType.ONLINE.toString(),
                OrderType.WHOLESALE.toString());
        for (String orderCode : orderType) {
            try {
                when(data.getPermission()).thenReturn(false, false);
                when(cart.getType()).thenReturn(orderCode);
                when(cart.isExchange()).thenReturn(false);
                when(permissionClient.checkPermission(any(CheckPermissionRequest.class))).thenReturn(data);
                when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
                OrderEntryDTO orderEntryDTO = new OrderEntryDTO();
                orderEntryDTO.setCompanyId(2L);
                orderEntryDTO.setCombo(true);
                facade.checkUpdateCartPrice(orderEntryDTO, "cartCode");
                fail("Must throw exception");
            } catch (ServiceException e) {
                switch (orderCode) {
                    case "RETAIL":
                        assertEquals(ErrorCodes.HAS_NOT_PERMISSION_TO_EDIT_RETAIL_PRICE_COMBO.message(), e.getMessage());
                        break;
                    case "ONLINE":
                        assertEquals(ErrorCodes.HAS_NOT_PERMISSION_TO_EDIT_ORDER_PRICE_COMBO.message(), e.getMessage());
                        break;
                    case "WHOLESALE":
                        assertEquals(ErrorCodes.HAS_NOT_PERMISSION_TO_EDIT_WHOLESALE_PRICE_COMBO.message(), e.getMessage());
                        break;
                    default:
                        fail("Must throw exception");
                }
            }
        }
    }

    @Test
    public void checkUpdateCartPriceOfComboEntry_hasPermissionEditPriceAndNotPermissionEditComboPrice_shouldAccepted() {
        List<String> orderType = Arrays.asList(OrderType.RETAIL.toString(), OrderType.ONLINE.toString(),
                OrderType.WHOLESALE.toString());
        for (String orderCode : orderType) {
            when(data.getPermission()).thenReturn(true, false);
            when(cart.getType()).thenReturn(orderCode);
            when(cart.isExchange()).thenReturn(false);
            when(permissionClient.checkPermission(any(CheckPermissionRequest.class))).thenReturn(data);
            when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
            OrderEntryDTO orderEntryDTO = new OrderEntryDTO();
            orderEntryDTO.setCompanyId(2L);
            orderEntryDTO.setCombo(true);
            facade.checkUpdateCartPrice(orderEntryDTO, "cartCode");
            assertTrue("success", true);
        }
    }

    @Test
    public void checkPlaceOrder_hasNotPermission() {
        List<String> orderType = Arrays.asList(OrderType.RETAIL.toString(), OrderType.ONLINE.toString(),
                OrderType.WHOLESALE.toString());
        for (String orderCode : orderType) {

            try {
                when(data.getPermission()).thenReturn(false);
                when(cart.getType()).thenReturn(orderCode);
                when(permissionClient.checkPermission(any(CheckPermissionRequest.class))).thenReturn(data);
                when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
                facade.checkPlaceOrder(2l, "cartCode");
                fail("Must throw exception");
            } catch (ServiceException e) {
                switch (orderCode) {
                    case "RETAIL":
                        assertEquals(ErrorCodes.HAS_NOT_PERMISSION_TO_CREATE_RETAIL_BILL.code(), e.getCode());
                        break;
                    case "ONLINE":
                        assertEquals(ErrorCodes.HAS_NOT_PERMISSION_TO_CREATE_ORDER.code(), e.getCode());
                        break;
                    case "WHOLESALE":
                        assertEquals(ErrorCodes.HAS_NOT_PERMISSION_TO_CREATE_WHOLESALE_BILL.code(), e.getCode());
                        break;
                    default:
                        fail("Must throw exception");
                }

            }
        }
    }

    @Test
    public void checkPlaceOrder_hasPermission() {
        when(data.getPermission()).thenReturn(true);
        when(cart.getType()).thenReturn(OrderType.ONLINE.toString());
        when(permissionClient.checkPermission(any(CheckPermissionRequest.class))).thenReturn(data);
        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
        facade.checkPlaceOrder(2l, "cartCode");
    }

    @Test
    public void checkSearchingOrderPermission_RetailOrder_HasViewAll_NotFilterWarehouse() {
        when(orderSearchReqMock.getOrderType()).thenReturn(OrderType.RETAIL.name());
        when(checkPermissionDataMock.getPermission()).thenReturn(true);
        when(orderSearchReqMock.getWarehouseId()).thenReturn(null);

        facade.checkSearchingOrderPermission(orderSearchReqMock);
        assertTrue("success", true);
    }

    @Test
    public void checkSearchingOrderPermission_OnlineOrder_OnlyViewList_NotFilterWarehouse() {
        when(orderSearchReqMock.getOrderType()).thenReturn(OrderType.ONLINE.name());
        when(checkPermissionDataMock.getPermission()).thenReturn(false, true);
        when(orderSearchReqMock.getWarehouseId()).thenReturn(null);

        facade.checkSearchingOrderPermission(orderSearchReqMock);
        assertTrue("success", true);
    }

    @Test
    public void checkSearchingOrderPermission_OnlineOrder_HasNotPermissionToViewList_NotFilterWarehouse() {
        when(orderSearchReqMock.getOrderType()).thenReturn(OrderType.ONLINE.name());
        when(checkPermissionDataMock.getPermission()).thenReturn(false, false);
        when(orderSearchReqMock.getWarehouseId()).thenReturn(null);
        try {
            facade.checkSearchingOrderPermission(orderSearchReqMock);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.HAS_NOT_PERMISSION_TO_VIEW_LIST_ORDER.message(), e.getMessage());
        }
    }

    @Test
    public void checkSearchingOrderPermission_RetailOrder_HasNotPermissionToViewList_NotFilterWarehouse() {
        when(orderSearchReqMock.getOrderType()).thenReturn(OrderType.RETAIL.name());
        when(checkPermissionDataMock.getPermission()).thenReturn(false, false);
        when(orderSearchReqMock.getWarehouseId()).thenReturn(null);
        try {
            facade.checkSearchingOrderPermission(orderSearchReqMock);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.HAS_NOT_PERMISSION_TO_VIEW_LIST_RETAIL_BILL.message(), e.getMessage());
        }
    }

    @Test
    public void checkSearchingOrderPermission_WholesaleOrder_HasNotPermissionToViewList_NotFilterWarehouse() {
        when(orderSearchReqMock.getOrderType()).thenReturn(OrderType.WHOLESALE.name());
        when(checkPermissionDataMock.getPermission()).thenReturn(false, false);
        when(orderSearchReqMock.getWarehouseId()).thenReturn(null);
        try {
            facade.checkSearchingOrderPermission(orderSearchReqMock);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.HAS_NOT_PERMISSION_TO_VIEW_LIST_WHOLESALE_BILL.message(), e.getMessage());
        }
    }

    @Test
    public void checkSearchingOrderPermission_InvalidOrderType() {
        when(orderSearchReqMock.getOrderType()).thenReturn("OTHER_TYPE");
        when(checkPermissionDataMock.getPermission()).thenReturn(false, false);
        when(orderSearchReqMock.getWarehouseId()).thenReturn(null);
        try {
            facade.checkSearchingOrderPermission(orderSearchReqMock);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.ACCESS_DENIED.message(), e.getMessage());
        }
    }

    @Test
    public void checkSearchingOrderPermission_WholesaleOrder_HasPermissionToViewList_NotPermissionOnWarehouse() {
        when(orderSearchReqMock.getOrderType()).thenReturn(OrderType.RETAIL.name());
        when(checkPermissionDataMock.getPermission()).thenReturn(false, true);
        when(orderSearchReqMock.getWarehouseId()).thenReturn(1l);
        when(permissionClient.userHasWarehouse(any())).thenReturn(false);
        try {
            facade.checkSearchingOrderPermission(orderSearchReqMock);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.USER_HAS_NOT_PERMISSION_ON_WAREHOUSE.message(), e.getMessage());
        }
    }

    @Test
    public void checkSearchingOrderPermission_WholesaleOrder_HasPermissionToViewListAndPermissionOnWarehouse() {
        when(orderSearchReqMock.getOrderType()).thenReturn(OrderType.RETAIL.name());
        when(checkPermissionDataMock.getPermission()).thenReturn(false, true);
        when(orderSearchReqMock.getWarehouseId()).thenReturn(1l);
        when(permissionClient.userHasWarehouse(any())).thenReturn(true);
        facade.checkSearchingOrderPermission(orderSearchReqMock);
        assertTrue("success", true);
    }
}
