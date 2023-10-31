package com.vctek.orderservice.strategy.impl;

import com.vctek.converter.Converter;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.CommerceChangeOrderStatusModification;
import com.vctek.orderservice.dto.CommerceChangeOrderStatusParameter;
import com.vctek.orderservice.dto.OrderEntryData;
import com.vctek.orderservice.elasticsearch.model.ProductSearchModel;
import com.vctek.orderservice.elasticsearch.service.ProductSearchService;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.repository.OrderEntryRepository;
import com.vctek.orderservice.service.*;
import com.vctek.orderservice.strategy.CommerceCartCalculationStrategy;
import com.vctek.orderservice.strategy.CommercePlaceOrderStrategy;
import com.vctek.util.OrderStatus;
import com.vctek.util.OrderType;
import com.vctek.util.ProductType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DefaultCommerceChangeOrderStatusStrategyTest {
    private DefaultCommerceChangeOrderStatusStrategy strategy;
    @Mock
    private CommercePlaceOrderStrategy commercePlaceOrderStrategy;
    private CommerceChangeOrderStatusParameter parameter;
    private OrderModel order;
    @Mock
    private InventoryService inventoryService;
    @Mock
    private BillService billService;
    private OrderEntryModel entry1;
    private OrderEntryModel entry2;
    @Mock
    private OrderService orderService;
    @Mock
    private OrderHistoryService orderHistoryService;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private AuthService authService;
    @Mock
    private OrderModel retailOrderMock;
    @Mock
    private InvoiceService invoiceService;
    @Mock
    private LoyaltyService loyaltyService;
    @Mock
    private LoyaltyTransactionService loyaltyTransactionService;
    @Mock
    private CommerceCartCalculationStrategy commerceCartCalculationStrategy;
    @Mock
    private PaymentTransactionService paymentTransactionService;
    @Mock
    private ProductSearchService productSearchService;
    @Mock
    private OrderEntryRepository orderEntryRepository;
    @Mock
    private Converter<AbstractOrderEntryModel, OrderEntryData> entryDataConverter;
    @Mock
    private CouponService couponService;
    @Mock
    private WebSocketService webSocketService;

    @Before
    public void setUp() {
        entry1 = new OrderEntryModel();
        entry2 = new OrderEntryModel();
        order = new OrderModel();
        order.setId(222l);
        order.setCode("orderCode");
        order.setCompanyId(1l);
        order.setEntries(Arrays.asList(entry1, entry2));
        MockitoAnnotations.initMocks(this);
        strategy = new DefaultCommerceChangeOrderStatusStrategy();
        strategy.setInventoryService(inventoryService);
        strategy.setBillService(billService);
        strategy.setCommercePlaceOrderStrategy(commercePlaceOrderStrategy);
        strategy.setOrderService(orderService);
        strategy.setOrderHistoryService(orderHistoryService);
        strategy.setApplicationEventPublisher(applicationEventPublisher);
        strategy.setAuthService(authService);
        strategy.setInvoiceService(invoiceService);
        strategy.setLoyaltyService(loyaltyService);
        strategy.setLoyaltyTransactionService(loyaltyTransactionService);
        strategy.setCommerceCartCalculationStrategy(commerceCartCalculationStrategy);
        strategy.setPaymentTransactionService(paymentTransactionService);
        strategy.setProductSearchService(productSearchService);
        strategy.setOrderEntryRepository(orderEntryRepository);
        strategy.setEntryDataConverter(entryDataConverter);
        strategy.setCouponService(couponService);
        strategy.setWebSocketService(webSocketService);
        strategy.init();
        when(retailOrderMock.getCode()).thenReturn("retailCode");
        when(commercePlaceOrderStrategy.changeBillToRetail(order)).thenReturn(retailOrderMock);
        when(orderService.save(order)).thenReturn(order);
    }

    @Test
    public void changeStatus_FromNewToCompleted_NotAllowed() {
        try {
            parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.PRE_ORDER, OrderStatus.COMPLETED);
            strategy.changeToHigherStatus(parameter);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CANNOT_CHANGE_STATUS_OVER_CONFIRMED.code(), e.getCode());
        }
    }

    @Test
    public void changeStatus_OldStatusIsChangeToRetail_NotAllowed() {
        try {
            parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.CHANGE_TO_RETAIL, OrderStatus.COMPLETED);
            strategy.changeToHigherStatus(parameter);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CAN_NOT_CHANGE_STATUS_THIS_ORDER.code(), e.getCode());
        }
    }

    @Test
    public void changeStatus_OldAndNewIsSmallerThanConfirmed_ShouldDoNothing() {
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.NEW, OrderStatus.CONFIRMING);
        CommerceChangeOrderStatusModification modification = strategy.changeToHigherStatus(parameter);
        assertNull(modification.getOrderModel().getBillId());
    }

    @Test
    public void changeStatus_SmallerThanPreOrder_ToPreOrder_DoNothing() {
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.NEW, OrderStatus.CONFIRMING);
        CommerceChangeOrderStatusModification modification = strategy.changeToHigherStatus(parameter);
        assertNull(modification.getOrderModel().getBillId());
    }

    @Test
    public void changeStatus_New_to_Confirmed_ShouldHoldingAllQuantityOfProductInOrder() {
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.NEW, OrderStatus.CONFIRMED);
        CommerceChangeOrderStatusModification modification = strategy.changeToHigherStatus(parameter);
        verify(inventoryService).holdingAllQuantityOf(order);
        verify(inventoryService).subtractPreOrder(order);
        verify(orderService).resetPreAndHoldingStockOf(order);
        assertNull(modification.getOrderModel().getBillId());
    }

    @Test
    public void changeStatus_PreOrder_to_Confirmed_ShouldHoldingPreOrderQtyOfProductInOrder() {
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.PRE_ORDER, OrderStatus.CONFIRMED);
        CommerceChangeOrderStatusModification modification = strategy.changeToHigherStatus(parameter);
        verify(inventoryService).holdingAllQuantityOf(order);
        verify(inventoryService).subtractPreOrder(order);
        verify(orderService).resetPreAndHoldingStockOf(order);
        assertNull(modification.getOrderModel().getBillId());
    }

    @Test
    public void changeStatus_Confirmed_To_Packing_ShouldDoNothing() {
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.CONFIRMED, OrderStatus.PACKING);

        CommerceChangeOrderStatusModification modification = strategy.changeToHigherStatus(parameter);
        verify(inventoryService, times(0)).holdingAllQuantityOf(order);
        verify(inventoryService, times(0)).subtractPreOrder(order);
        assertNull(modification.getOrderModel().getBillId());
    }

    @Test
    public void changeStatus_Confirmed_To_Packaged_ShouldDoNothing() {
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.CONFIRMED, OrderStatus.PACKAGED);

        CommerceChangeOrderStatusModification modification = strategy.changeToHigherStatus(parameter);
        verify(inventoryService, times(0)).holdingAllQuantityOf(order);
        verify(inventoryService, times(0)).subtractPreOrder(order);
        assertNull(modification.getOrderModel().getBillId());
    }

    @Test
    public void changeStatus_Confirmed_To_Shipping_ShouldCreateReturnBillForShippingOrder() {
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.CONFIRMED, OrderStatus.SHIPPING);
        when(billService.createReturnBillWithOrderOnline(order)).thenReturn(11l);

        CommerceChangeOrderStatusModification modification = strategy.changeToHigherStatus(parameter);
        verify(inventoryService, times(0)).holdingAllQuantityOf(order);
        verify(inventoryService, times(0)).subtractPreOrder(order);
        verify(billService).createReturnBillWithOrderOnline(order);
        assertEquals(11l, modification.getOrderModel().getBillId(), 0);
    }

    @Test
    public void changeStatus_Packing_To_Shipping_ShouldCreateReturnBillForShippingOrder() {
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.PACKING, OrderStatus.SHIPPING);
        when(billService.createReturnBillWithOrderOnline(order)).thenReturn(11l);

        CommerceChangeOrderStatusModification modification = strategy.changeToHigherStatus(parameter);
        verify(inventoryService, times(0)).holdingAllQuantityOf(order);
        verify(inventoryService, times(0)).subtractPreOrder(order);
        verify(billService).createReturnBillWithOrderOnline(order);
        assertEquals(11l, modification.getOrderModel().getBillId(), 0);
    }

    @Test
    public void changeStatus_Packaged_To_Shipping_ShouldCreateReturnBillForShippingOrder() {
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.PACKAGED, OrderStatus.SHIPPING);
        when(billService.createReturnBillWithOrderOnline(order)).thenReturn(11l);

        CommerceChangeOrderStatusModification modification = strategy.changeToHigherStatus(parameter);
        verify(inventoryService, times(0)).holdingAllQuantityOf(order);
        verify(inventoryService, times(0)).subtractPreOrder(order);
        verify(billService).createReturnBillWithOrderOnline(order);
        assertEquals(11l, modification.getOrderModel().getBillId(), 0);
    }

    @Test
    public void changeStatus_Shipping_To_Completed_ShouldSubtractShippingStock() {
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.SHIPPING, OrderStatus.COMPLETED);

        CommerceChangeOrderStatusModification modification = strategy.changeToHigherStatus(parameter);
        verify(inventoryService, times(0)).holdingAllQuantityOf(order);
        verify(inventoryService, times(0)).subtractPreOrder(order);
        verify(billService, times(0)).createReturnBillWithOrderOnline(order);
        verify(billService, times(1)).subtractShippingStockOf(order);
        assertNull(modification.getOrderModel().getBillId());
    }

    @Test
    public void changeStatus_Confirmed_To_Completed_NotAccepted() {
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.CONFIRMED, OrderStatus.COMPLETED);
        when(billService.createReturnBillWithOrderOnline(order)).thenReturn(11l);
        try {
            CommerceChangeOrderStatusModification modification = strategy.changeToHigherStatus(parameter);
            verify(inventoryService, times(0)).holdingAllQuantityOf(order);
            verify(inventoryService, times(0)).subtractPreOrder(order);
            verify(billService, times(1)).createReturnBillWithOrderOnline(order);
            verify(billService, times(1)).subtractShippingStockOf(order);
            assertEquals(11l, modification.getOrderModel().getBillId(), 0);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.NOT_ACCEPT_CHANGE_STATUS_BEFORE_SHIPPING.code(), e.getCode());
        }
    }

    @Test
    public void changeStatus_Packing_To_Completed_NotAccepted() {
        try {
            parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.PACKING, OrderStatus.COMPLETED);
            when(billService.createReturnBillWithOrderOnline(order)).thenReturn(11l);

            CommerceChangeOrderStatusModification modification = strategy.changeToHigherStatus(parameter);
            verify(inventoryService, times(0)).holdingAllQuantityOf(order);
            verify(inventoryService, times(0)).subtractPreOrder(order);
            verify(billService, times(1)).createReturnBillWithOrderOnline(order);
            verify(billService, times(1)).subtractShippingStockOf(order);
            assertEquals(11l, modification.getOrderModel().getBillId(), 0);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.NOT_ACCEPT_CHANGE_STATUS_BEFORE_SHIPPING.code(), e.getCode());
        }
    }

    @Test
    public void changeStatus_Packaged_To_Completed_NotAccepted() {
        try {
            parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.PACKAGED, OrderStatus.COMPLETED);
            when(billService.createReturnBillWithOrderOnline(order)).thenReturn(11l);

            CommerceChangeOrderStatusModification modification = strategy.changeToHigherStatus(parameter);
            verify(inventoryService, times(0)).holdingAllQuantityOf(order);
            verify(inventoryService, times(0)).subtractPreOrder(order);
            verify(billService, times(1)).createReturnBillWithOrderOnline(order);
            verify(billService, times(1)).subtractShippingStockOf(order);
            assertEquals(11l, modification.getOrderModel().getBillId(), 0);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.NOT_ACCEPT_CHANGE_STATUS_BEFORE_SHIPPING.code(), e.getCode());
        }
    }

    @Test
    public void changeStatus_Confirmed_To_Returning_NotAccepted() {
        try {
            parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.CONFIRMED, OrderStatus.RETURNING);
            when(billService.createReturnBillWithOrderOnline(order)).thenReturn(11l);

            CommerceChangeOrderStatusModification modification = strategy.changeToHigherStatus(parameter);
            verify(inventoryService, times(0)).holdingAllQuantityOf(order);
            verify(inventoryService, times(0)).subtractPreOrder(order);
            verify(billService, times(1)).createReturnBillWithOrderOnline(order);
            verify(billService, times(0)).subtractShippingStockOf(order);
            verify(billService, times(0)).addShippingStockOf(order);
            assertEquals(11l, modification.getOrderModel().getBillId(), 0);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.NOT_ACCEPT_CHANGE_STATUS_BEFORE_SHIPPING.code(), e.getCode());
        }
    }

    @Test
    public void changeStatus_Packing_To_Returning_NotAccepted() {
        try {
            parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.PACKING, OrderStatus.RETURNING);
            when(billService.createReturnBillWithOrderOnline(order)).thenReturn(11l);

            CommerceChangeOrderStatusModification modification = strategy.changeToHigherStatus(parameter);
            verify(inventoryService, times(0)).holdingAllQuantityOf(order);
            verify(inventoryService, times(0)).subtractPreOrder(order);
            verify(billService, times(1)).createReturnBillWithOrderOnline(order);
            verify(billService, times(0)).subtractShippingStockOf(order);
            verify(billService, times(0)).addShippingStockOf(order);
            assertEquals(11l, modification.getOrderModel().getBillId(), 0);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.NOT_ACCEPT_CHANGE_STATUS_BEFORE_SHIPPING.code(), e.getCode());
        }
    }

    @Test
    public void changeStatus_Packaged_To_Returning_NotAccepted() {
        try {
            parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.PACKAGED, OrderStatus.RETURNING);
            when(billService.createReturnBillWithOrderOnline(order)).thenReturn(11l);

            CommerceChangeOrderStatusModification modification = strategy.changeToHigherStatus(parameter);
            verify(inventoryService, times(0)).holdingAllQuantityOf(order);
            verify(inventoryService, times(0)).subtractPreOrder(order);
            verify(billService, times(1)).createReturnBillWithOrderOnline(order);
            verify(billService, times(0)).subtractShippingStockOf(order);
            verify(billService, times(0)).addShippingStockOf(order);
            assertEquals(11l, modification.getOrderModel().getBillId(), 0);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.NOT_ACCEPT_CHANGE_STATUS_BEFORE_SHIPPING.code(), e.getCode());
        }
    }

    @Test
    public void changeStatus_Shipping_To_Returning_ShouldDoNothing() {
        order.setBillId(11l);
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.SHIPPING, OrderStatus.RETURNING);

        CommerceChangeOrderStatusModification modification = strategy.changeToHigherStatus(parameter);
        verify(inventoryService, times(0)).holdingAllQuantityOf(order);
        verify(inventoryService, times(0)).subtractPreOrder(order);
        verify(billService, times(0)).createReturnBillWithOrderOnline(order);
        verify(billService, times(0)).subtractShippingStockOf(order);
        verify(billService, times(0)).addShippingStockOf(order);
        assertEquals(11l, modification.getOrderModel().getBillId(), 0);
    }

    @Test
    public void changeStatus_Confirmed_To_OrderReturn_NotAccepted() {
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.CONFIRMED, OrderStatus.ORDER_RETURN);
        try {
            CommerceChangeOrderStatusModification modification = strategy.changeToHigherStatus(parameter);
            verify(inventoryService, times(0)).holdingAllQuantityOf(order);
            verify(inventoryService, times(0)).subtractPreOrder(order);
            verify(inventoryService, times(1)).changeAllHoldingToAvailableOf(order);
            verify(billService, times(0)).createReturnBillWithOrderOnline(order);
            verify(billService, times(0)).subtractShippingStockOf(order);
            verify(billService, times(0)).changeOrderStatusToOrderReturn(order);
            assertNull(modification.getOrderModel().getBillId());
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.NOT_ACCEPT_CHANGE_STATUS_BEFORE_SHIPPING.code(), e.getCode());
        }
    }

    @Test
    public void changeStatus_Packing_To_OrderReturn_NotAccepted() {
        try {
            parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.PACKING, OrderStatus.ORDER_RETURN);

            CommerceChangeOrderStatusModification modification = strategy.changeToHigherStatus(parameter);
            verify(inventoryService, times(0)).holdingAllQuantityOf(order);
            verify(inventoryService, times(0)).subtractPreOrder(order);
            verify(inventoryService, times(1)).changeAllHoldingToAvailableOf(order);
            verify(billService, times(0)).createReturnBillWithOrderOnline(order);
            verify(billService, times(0)).subtractShippingStockOf(order);
            verify(billService, times(0)).changeOrderStatusToOrderReturn(order);
            assertNull(modification.getOrderModel().getBillId());
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.NOT_ACCEPT_CHANGE_STATUS_BEFORE_SHIPPING.code(), e.getCode());
        }
    }

    @Test
    public void changeStatus_Packaged_To_OrderReturn_NotAccepted() {
        try {
            parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.PACKAGED, OrderStatus.ORDER_RETURN);
            CommerceChangeOrderStatusModification modification = strategy.changeToHigherStatus(parameter);
            verify(inventoryService, times(0)).holdingAllQuantityOf(order);
            verify(inventoryService, times(0)).subtractPreOrder(order);
            verify(inventoryService, times(1)).changeAllHoldingToAvailableOf(order);
            verify(billService, times(0)).createReturnBillWithOrderOnline(order);
            verify(billService, times(0)).subtractShippingStockOf(order);
            verify(billService, times(0)).changeOrderStatusToOrderReturn(order);
            assertNull(modification.getOrderModel().getBillId());
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.NOT_ACCEPT_CHANGE_STATUS_BEFORE_SHIPPING.code(), e.getCode());
        }
    }

    @Test
    public void changeStatus_Completed_To_OrderReturn_OnlyChangeToOrderReturn() {
        order.setBillId(11l);
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.COMPLETED, OrderStatus.ORDER_RETURN);

        CommerceChangeOrderStatusModification modification = strategy.changeToHigherStatus(parameter);
        verify(inventoryService, times(0)).holdingAllQuantityOf(order);
        verify(inventoryService, times(0)).subtractPreOrder(order);
        verify(billService, times(0)).createReturnBillWithOrderOnline(order);
        verify(billService, times(0)).subtractShippingStockOf(order);
        verify(billService, times(1)).addShippingStockOf(order);
        verify(billService, times(1)).changeOrderStatusToOrderReturn(order);
        verify(loyaltyService, times(0)).completeRedeemLoyaltyForOnline(any());
        verify(loyaltyTransactionService, times(0)).cloneAwardRedeemLoyaltyTransaction(anyString(), anyString());
        assertNull(modification.getOrderModel().getBillId());
    }


    @Test
    public void changeStatus_Confirmed_To_CustomerCancel_ShouldChangeStockFromHoldingToAvailable() {
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.CONFIRMED, OrderStatus.CUSTOMER_CANCEL);

        CommerceChangeOrderStatusModification modification = strategy.changeToHigherStatus(parameter);
        verify(inventoryService, times(0)).holdingAllQuantityOf(order);
        verify(inventoryService, times(0)).subtractPreOrder(order);
        verify(inventoryService, times(1)).changeAllHoldingToAvailableOf(order);
        verify(billService, times(0)).createReturnBillWithOrderOnline(order);
        verify(billService, times(0)).subtractShippingStockOf(order);
        verify(billService, times(0)).changeOrderStatusToOrderReturn(order);
        verify(loyaltyService, times(0)).completeRedeemLoyaltyForOnline(any());
        verify(loyaltyTransactionService, times(0)).cloneAwardRedeemLoyaltyTransaction(anyString(), anyString());
        assertNull(modification.getOrderModel().getBillId());
    }

    @Test
    public void changeStatus_Confirmed_To_CustomerCancel_ShouldChangeStockFromHoldingToAvailable_existRedeemAmount() {
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.CONFIRMED, OrderStatus.CUSTOMER_CANCEL);
        order.setRedeemAmount(1000d);

        CommerceChangeOrderStatusModification modification = strategy.changeToHigherStatus(parameter);
        verify(inventoryService, times(0)).holdingAllQuantityOf(order);
        verify(inventoryService, times(0)).subtractPreOrder(order);
        verify(inventoryService, times(1)).changeAllHoldingToAvailableOf(order);
        verify(billService, times(0)).createReturnBillWithOrderOnline(order);
        verify(billService, times(0)).subtractShippingStockOf(order);
        verify(billService, times(0)).changeOrderStatusToOrderReturn(order);
        verify(loyaltyService, times(0)).cancelPendingRedeemForCancelOrder(order);
        verify(loyaltyService, times(0)).completeRedeemLoyaltyForOnline(any());
        verify(loyaltyTransactionService, times(0)).cloneAwardRedeemLoyaltyTransaction(anyString(), anyString());
        assertNull(modification.getOrderModel().getBillId());
        assertNotNull(order.getRedeemAmount());
    }

    @Test
    public void changeStatus_Packing_To_CustomerCancel_ShouldChangeStockFromHoldingToAvailable() {
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.PACKING, OrderStatus.CUSTOMER_CANCEL);

        CommerceChangeOrderStatusModification modification = strategy.changeToHigherStatus(parameter);
        verify(inventoryService, times(0)).holdingAllQuantityOf(order);
        verify(inventoryService, times(0)).subtractPreOrder(order);
        verify(inventoryService, times(1)).changeAllHoldingToAvailableOf(order);
        verify(billService, times(0)).createReturnBillWithOrderOnline(order);
        verify(billService, times(0)).subtractShippingStockOf(order);
        verify(billService, times(0)).changeOrderStatusToOrderReturn(order);
        verify(loyaltyService, times(0)).completeRedeemLoyaltyForOnline(any());
        verify(loyaltyTransactionService, times(0)).cloneAwardRedeemLoyaltyTransaction(anyString(), anyString());
        assertNull(modification.getOrderModel().getBillId());
    }

    @Test
    public void changeStatus_Packaged_To_CustomerCancel_ShouldChangeStockFromHoldingToAvailable() {
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.PACKAGED, OrderStatus.CUSTOMER_CANCEL);

        CommerceChangeOrderStatusModification modification = strategy.changeToHigherStatus(parameter);
        verify(inventoryService, times(0)).holdingAllQuantityOf(order);
        verify(inventoryService, times(0)).subtractPreOrder(order);
        verify(inventoryService, times(1)).changeAllHoldingToAvailableOf(order);
        verify(billService, times(0)).createReturnBillWithOrderOnline(order);
        verify(billService, times(0)).subtractShippingStockOf(order);
        verify(billService, times(0)).changeOrderStatusToOrderReturn(order);
        verify(loyaltyService, times(0)).completeRedeemLoyaltyForOnline(any());
        verify(loyaltyTransactionService, times(0)).cloneAwardRedeemLoyaltyTransaction(anyString(), anyString());
        assertNull(modification.getOrderModel().getBillId());
    }

    @Test
    public void changeStatus_Shipping_To_CustomerCancel_ShouldSubtractShippingAndRevertOrder() {
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.SHIPPING, OrderStatus.CUSTOMER_CANCEL);

        CommerceChangeOrderStatusModification modification = strategy.changeToHigherStatus(parameter);
        verify(inventoryService, times(0)).holdingAllQuantityOf(order);
        verify(inventoryService, times(0)).subtractPreOrder(order);
        verify(inventoryService, times(0)).changeAllHoldingToAvailableOf(order);
        verify(billService, times(0)).createReturnBillWithOrderOnline(order);
        verify(billService, times(0)).changeOrderStatusToOrderReturn(order);
        verify(billService, times(1)).subtractShippingStockOf(order);
        verify(billService, times(1)).cancelOnlineOrder(order);
        verify(loyaltyService, times(0)).completeRedeemLoyaltyForOnline(any());
        verify(loyaltyTransactionService, times(0)).cloneAwardRedeemLoyaltyTransaction(anyString(), anyString());
        assertNull(modification.getOrderModel().getBillId());
    }

    @Test
    public void changeStatus_Completed_To_CustomerCancel_ShouldSubtractShippingAndRevertOrder() {
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.COMPLETED, OrderStatus.CUSTOMER_CANCEL);

        CommerceChangeOrderStatusModification modification = strategy.changeToHigherStatus(parameter);
        verify(inventoryService, times(0)).holdingAllQuantityOf(order);
        verify(inventoryService, times(0)).subtractPreOrder(order);
        verify(inventoryService, times(0)).changeAllHoldingToAvailableOf(order);
        verify(billService, times(0)).createReturnBillWithOrderOnline(order);
        verify(billService, times(0)).changeOrderStatusToOrderReturn(order);
        verify(billService, times(0)).subtractShippingStockOf(order);
        verify(billService, times(1)).cancelOnlineOrder(order);
        verify(loyaltyService, times(0)).completeRedeemLoyaltyForOnline(any());
        verify(loyaltyTransactionService, times(0)).cloneAwardRedeemLoyaltyTransaction(anyString(), anyString());
        assertNull(modification.getOrderModel().getBillId());
    }

    @Test
    public void changeStatus_SystemCancel_To_CustomerCancel_DoNothing() {
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.SYSTEM_CANCEL, OrderStatus.CUSTOMER_CANCEL);

        CommerceChangeOrderStatusModification modification = strategy.changeToHigherStatus(parameter);
        verify(inventoryService, times(0)).holdingAllQuantityOf(order);
        verify(inventoryService, times(0)).subtractPreOrder(order);
        verify(inventoryService, times(0)).changeAllHoldingToAvailableOf(order);
        verify(billService, times(0)).createReturnBillWithOrderOnline(order);
        verify(billService, times(0)).changeOrderStatusToOrderReturn(order);
        verify(billService, times(0)).subtractShippingStockOf(order);
        verify(billService, times(0)).revertBillForOrder(anyString(), anyLong());
        verify(loyaltyService, times(0)).completeRedeemLoyaltyForOnline(any());
        verify(loyaltyTransactionService, times(0)).cloneAwardRedeemLoyaltyTransaction(anyString(), anyString());
        assertNull(modification.getOrderModel().getBillId());
    }

    @Test
    public void changeStatus_OrderReturn_To_CustomerCancel_DoNothing() {
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.ORDER_RETURN, OrderStatus.CUSTOMER_CANCEL);

        CommerceChangeOrderStatusModification modification = strategy.changeToHigherStatus(parameter);
        verify(inventoryService, times(0)).holdingAllQuantityOf(order);
        verify(inventoryService, times(0)).subtractPreOrder(order);
        verify(inventoryService, times(0)).changeAllHoldingToAvailableOf(order);
        verify(billService, times(0)).createReturnBillWithOrderOnline(order);
        verify(billService, times(0)).changeOrderStatusToOrderReturn(order);
        verify(billService, times(0)).subtractShippingStockOf(order);
        verify(billService, times(0)).revertBillForOrder(anyString(), anyLong());
        verify(loyaltyService, times(0)).completeRedeemLoyaltyForOnline(any());
        verify(loyaltyTransactionService, times(0)).cloneAwardRedeemLoyaltyTransaction(anyString(), anyString());
        assertNull(modification.getOrderModel().getBillId());
    }

    @Test
    public void changeStatus_Returning_To_CustomerCancel_ShouldSubtractShippingStock() {
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.RETURNING, OrderStatus.CUSTOMER_CANCEL);

        CommerceChangeOrderStatusModification modification = strategy.changeToHigherStatus(parameter);
        verify(inventoryService, times(0)).holdingAllQuantityOf(order);
        verify(inventoryService, times(0)).subtractPreOrder(order);
        verify(inventoryService, times(0)).changeAllHoldingToAvailableOf(order);
        verify(billService, times(0)).createReturnBillWithOrderOnline(order);
        verify(billService, times(0)).changeOrderStatusToOrderReturn(order);
        verify(billService, times(1)).subtractShippingStockOf(order);
        verify(billService, times(1)).cancelOnlineOrder(order);
        verify(loyaltyService, times(0)).completeRedeemLoyaltyForOnline(any());
        verify(loyaltyTransactionService, times(0)).cloneAwardRedeemLoyaltyTransaction(anyString(), anyString());
        assertNull(modification.getOrderModel().getBillId());
    }


    @Test
    public void changeStatus_Confirmed_To_Retail_CreateBillAndChangeToRetail() {
        when(billService.createBillForOrder(retailOrderMock)).thenReturn(22l);
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.CONFIRMED, OrderStatus.CHANGE_TO_RETAIL);

        CommerceChangeOrderStatusModification modification = strategy.changeToHigherStatus(parameter);
        verify(inventoryService, times(0)).holdingAllQuantityOf(order);
        verify(inventoryService, times(0)).subtractPreOrder(order);
        verify(orderService, times(1)).resetPreAndHoldingStockOf(order);
        verify(inventoryService, times(1)).changeAllHoldingToAvailableOf(order);
        verify(billService, times(0)).changeOrderStatusToOrderReturn(order);
        verify(billService, times(0)).createReturnBillWithOrderOnline(order);
        verify(billService, times(0)).subtractShippingStockOf(order);
        verify(billService, times(1)).createBillForOrder(retailOrderMock);
        verify(commercePlaceOrderStrategy, times(1)).changeBillToRetail(order);
        verify(commerceCartCalculationStrategy, times(1)).calculateLoyaltyRewardOrder(order);
        verify(invoiceService).saveRedeemLoyaltyForOnlineOrChangeToRetailOrder(retailOrderMock);
        verify(retailOrderMock).setBillId(22l);
        verify(orderService).save(retailOrderMock);
        verify(loyaltyService, times(1)).cancelPendingRedeemForCancelOrder(any());
        verify(loyaltyService, times(1)).redeem(any(), anyDouble());
        verify(loyaltyTransactionService, times(0)).cloneAwardRedeemLoyaltyTransaction(anyString(), anyString());
        assertEquals("retailCode", modification.getRetailOrderCode());
    }

    @Test
    public void changeStatus_Shipping_To_Retail_CreateBillAndChangeToRetail() {
        when(commercePlaceOrderStrategy.changeBillToRetail(order)).thenReturn(retailOrderMock);
        order.setBillId(22l);
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.SHIPPING, OrderStatus.CHANGE_TO_RETAIL);

        CommerceChangeOrderStatusModification modification = strategy.changeToHigherStatus(parameter);
        verify(inventoryService, times(0)).holdingAllQuantityOf(order);
        verify(inventoryService, times(0)).subtractPreOrder(order);
        verify(inventoryService, times(0)).changeAllHoldingToAvailableOf(order);
        verify(billService, times(0)).changeOrderStatusToOrderReturn(order);
        verify(billService, times(0)).revertBillForOrder(anyString(), anyLong());
        verify(billService, times(0)).createReturnBillWithOrderOnline(order);
        verify(billService, times(1)).subtractShippingStockOf(order);
        verify(commerceCartCalculationStrategy, times(1)).calculateLoyaltyRewardOrder(order);
        verify(commercePlaceOrderStrategy, times(1)).changeBillToRetail(order);
        verify(invoiceService).saveRedeemLoyaltyForOnlineOrChangeToRetailOrder(retailOrderMock);
        assertEquals(22l, modification.getOrderModel().getBillId(), 0);
        verify(loyaltyService, times(1)).cancelPendingRedeemForCancelOrder(any());
        verify(loyaltyService, times(1)).redeem(any(), anyDouble());
        verify(loyaltyTransactionService, times(0)).cloneAwardRedeemLoyaltyTransaction(anyString(), anyString());
        assertEquals("retailCode", modification.getRetailOrderCode());
    }

    @Test
    public void changeStatus_Completed_To_Retail_CreateBillAndChangeToRetail() {
        when(commercePlaceOrderStrategy.changeBillToRetail(order)).thenReturn(retailOrderMock);
        order.setBillId(22l);
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.COMPLETED, OrderStatus.CHANGE_TO_RETAIL);

        CommerceChangeOrderStatusModification modification = strategy.changeToHigherStatus(parameter);
        verify(inventoryService, times(0)).holdingAllQuantityOf(order);
        verify(inventoryService, times(0)).subtractPreOrder(order);
        verify(inventoryService, times(0)).changeAllHoldingToAvailableOf(order);
        verify(billService, times(0)).changeOrderStatusToOrderReturn(order);
        verify(billService, times(0)).revertBillForOrder(anyString(), anyLong());
        verify(billService, times(0)).createReturnBillWithOrderOnline(order);
        verify(billService, times(0)).subtractShippingStockOf(order);
        verify(commercePlaceOrderStrategy, times(1)).changeBillToRetail(order);
        assertEquals(22l, modification.getOrderModel().getBillId(), 0);
        assertEquals("retailCode", modification.getRetailOrderCode());
    }

    @Test
    public void changeStatus_Completed_To_Retail_CreateBillAndChangeToRetail_cloneLoyaltyTransaction() {
        when(commercePlaceOrderStrategy.changeBillToRetail(order)).thenReturn(retailOrderMock);
        order.setBillId(22l);
        order.setRedeemAmount(1200d);
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.COMPLETED, OrderStatus.CHANGE_TO_RETAIL);

        CommerceChangeOrderStatusModification modification = strategy.changeToHigherStatus(parameter);
        verify(inventoryService, times(0)).holdingAllQuantityOf(order);
        verify(inventoryService, times(0)).subtractPreOrder(order);
        verify(inventoryService, times(0)).changeAllHoldingToAvailableOf(order);
        verify(billService, times(0)).changeOrderStatusToOrderReturn(order);
        verify(billService, times(0)).revertBillForOrder(anyString(), anyLong());
        verify(billService, times(0)).createReturnBillWithOrderOnline(order);
        verify(billService, times(0)).subtractShippingStockOf(order);
        verify(commercePlaceOrderStrategy, times(1)).changeBillToRetail(order);
        verify(commerceCartCalculationStrategy, times(0)).calculateLoyaltyRewardOrder(order);
        verify(loyaltyTransactionService, times(1)).cloneAwardRedeemLoyaltyTransaction(anyString(), anyString());
        verify(loyaltyService, times(0)).completeRedeemLoyaltyForOnline(order);
        assertEquals(22l, modification.getOrderModel().getBillId(), 0);
        assertEquals("retailCode", modification.getRetailOrderCode());
    }

    @Test
    public void changeStatus_Returning_To_Retail_CreateBillAndChangeToRetail() {
        when(commercePlaceOrderStrategy.changeBillToRetail(order)).thenReturn(retailOrderMock);
        order.setBillId(22l);
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.RETURNING, OrderStatus.CHANGE_TO_RETAIL);

        CommerceChangeOrderStatusModification modification = strategy.changeToHigherStatus(parameter);
        verify(inventoryService, times(0)).changeAllHoldingToAvailableOf(order);
        verify(inventoryService, times(0)).subtractPreOrder(order);
        verify(billService, times(0)).changeOrderStatusToOrderReturn(order);
        verify(billService, times(0)).revertBillForOrder(anyString(), anyLong());
        verify(inventoryService, times(0)).holdingAllQuantityOf(order);
        verify(billService, times(0)).createReturnBillWithOrderOnline(order);
        verify(billService, times(1)).subtractShippingStockOf(order);
        verify(commercePlaceOrderStrategy, times(1)).changeBillToRetail(order);
        verify(invoiceService).saveRedeemLoyaltyForOnlineOrChangeToRetailOrder(retailOrderMock);
        verify(loyaltyService, times(1)).cancelPendingRedeemForCancelOrder(any());
        verify(loyaltyService, times(1)).redeem(any(), anyDouble());
        verify(commerceCartCalculationStrategy, times(1)).calculateLoyaltyRewardOrder(order);
        verify(loyaltyTransactionService, times(0)).cloneAwardRedeemLoyaltyTransaction(anyString(), anyString());
        assertEquals(22l, modification.getOrderModel().getBillId(), 0);
        assertEquals("retailCode", modification.getRetailOrderCode());
    }

    @Test
    public void changeStatus_OrderReturn_To_Retail_CreateBillAndChangeToRetail() {
        when(commercePlaceOrderStrategy.changeBillToRetail(order)).thenReturn(retailOrderMock);
        when(billService.createBillForOrder(retailOrderMock)).thenReturn(22l);
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.ORDER_RETURN, OrderStatus.CHANGE_TO_RETAIL);

        CommerceChangeOrderStatusModification modification = strategy.changeToHigherStatus(parameter);
        verify(inventoryService, times(0)).changeAllHoldingToAvailableOf(order);
        verify(inventoryService, times(0)).subtractPreOrder(order);
        verify(billService, times(0)).changeOrderStatusToOrderReturn(order);
        verify(billService, times(0)).revertBillForOrder(anyString(), anyLong());
        verify(inventoryService, times(0)).holdingAllQuantityOf(order);
        verify(billService, times(0)).createReturnBillWithOrderOnline(order);
        verify(billService, times(0)).subtractShippingStockOf(order);
        verify(loyaltyService, times(1)).cancelPendingRedeemForCancelOrder(any());
        verify(loyaltyService, times(1)).redeem(any(), anyDouble());
        verify(commerceCartCalculationStrategy, times(1)).calculateLoyaltyRewardOrder(order);
        verify(loyaltyTransactionService, times(0)).cloneAwardRedeemLoyaltyTransaction(anyString(), anyString());
        verify(commercePlaceOrderStrategy, times(1)).changeBillToRetail(order);
        verify(retailOrderMock).setBillId(22l);
        verify(orderService).save(retailOrderMock);
        assertEquals("retailCode", modification.getRetailOrderCode());
    }

    @Test
    public void changeStatus_SystemCancel_To_Retail_CreateBillAndChangeToRetail() {
        when(commercePlaceOrderStrategy.changeBillToRetail(order)).thenReturn(retailOrderMock);
        when(billService.createBillForOrder(retailOrderMock)).thenReturn(22l);
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.SYSTEM_CANCEL, OrderStatus.CHANGE_TO_RETAIL);

        CommerceChangeOrderStatusModification modification = strategy.changeToHigherStatus(parameter);
        verify(inventoryService, times(0)).changeAllHoldingToAvailableOf(order);
        verify(inventoryService, times(0)).subtractPreOrder(order);
        verify(billService, times(0)).changeOrderStatusToOrderReturn(order);
        verify(billService, times(0)).revertBillForOrder(anyString(), anyLong());
        verify(orderService, times(0)).resetPreAndHoldingStockOf(order);
        verify(inventoryService, times(0)).holdingAllQuantityOf(order);
        verify(billService, times(0)).createReturnBillWithOrderOnline(order);
        verify(billService, times(0)).subtractShippingStockOf(order);
        verify(commercePlaceOrderStrategy, times(1)).changeBillToRetail(order);
        verify(commerceCartCalculationStrategy, times(1)).calculateLoyaltyRewardOrder(order);
        verify(invoiceService).saveRedeemLoyaltyForOnlineOrChangeToRetailOrder(retailOrderMock);
        verify(loyaltyService, times(1)).cancelPendingRedeemForCancelOrder(any());
        verify(loyaltyService, times(1)).redeem(any(), anyDouble());
        verify(loyaltyTransactionService, times(0)).cloneAwardRedeemLoyaltyTransaction(anyString(), anyString());
        verify(retailOrderMock).setBillId(22l);
        verify(orderService).save(retailOrderMock);
        assertEquals("retailCode", modification.getRetailOrderCode());
    }

    @Test
    public void changeToLowerStatus_OrderReturn_To_New_ShouldDoNothing() {
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.ORDER_RETURN, OrderStatus.NEW);
        CommerceChangeOrderStatusModification modification = strategy.changeToLowerStatus(parameter);
        assertNull(modification.getOrderModel().getBillId());
    }

    @Test
    public void changeToLowerStatus_CustomerCancel_To_Packaged_ShouldDoNothing() {
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.CUSTOMER_CANCEL, OrderStatus.PACKAGED);
        CommerceChangeOrderStatusModification modification = strategy.changeToLowerStatus(parameter);
        assertNull(modification.getOrderModel().getBillId());
    }

    @Test
    public void changeToLowerStatus_SystemCancel_To_Packing_ShouldDoNothing() {
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.SYSTEM_CANCEL, OrderStatus.PACKING);
        CommerceChangeOrderStatusModification modification = strategy.changeToLowerStatus(parameter);
        assertNull(modification.getOrderModel().getBillId());
    }

    @Test
    public void changeToLowerStatus_SystemCancel_To_CustomerCancel_ShouldDoNothing() {
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.SYSTEM_CANCEL, OrderStatus.CUSTOMER_CANCEL);
        CommerceChangeOrderStatusModification modification = strategy.changeToLowerStatus(parameter);
        assertNull(modification.getOrderModel().getBillId());
    }

    @Test
    public void changeToLowerStatus_SystemCancel_To_Confirmed_ShouldHoldStock() {
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.SYSTEM_CANCEL, OrderStatus.CONFIRMED);
        CommerceChangeOrderStatusModification modification = strategy.changeToLowerStatus(parameter);
        verify(inventoryService, times(1)).holdingAllQuantityOf(order);
        verify(inventoryService, times(1)).subtractPreOrder(order);
        verify(billService, times(0)).changeOrderStatusToOrderReturn(order);
        verify(billService, times(0)).revertBillForOrder(anyString(), anyLong());
        verify(inventoryService, times(0)).changeAllHoldingToAvailableOf(order);
        verify(billService, times(0)).createReturnBillWithOrderOnline(order);
        verify(billService, times(0)).subtractShippingStockOf(order);
        assertNull(modification.getOrderModel().getBillId());
    }

    @Test
    public void changeToLowerStatus_SystemCancel_To_Shipping_ShouldHoldStock_CreateBill() {
        when(billService.createReturnBillWithOrderOnline(order)).thenReturn(22l);
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.SYSTEM_CANCEL, OrderStatus.SHIPPING);

        CommerceChangeOrderStatusModification modification = strategy.changeToLowerStatus(parameter);
        verify(inventoryService, times(1)).holdingAllQuantityOf(order);
        verify(inventoryService, times(1)).subtractPreOrder(order);
        verify(billService, times(1)).createReturnBillWithOrderOnline(order);
        verify(billService, times(0)).changeOrderStatusToOrderReturn(order);
        verify(billService, times(0)).revertBillForOrder(anyString(), anyLong());
        verify(inventoryService, times(0)).changeAllHoldingToAvailableOf(order);
        verify(billService, times(0)).subtractShippingStockOf(order);
        assertEquals(22l, modification.getOrderModel().getBillId(), 0);
    }

    @Test
    public void changeToLowerStatus_SystemCancel_To_Returning_ShouldHoldStock_CreateBill() {
        when(billService.createReturnBillWithOrderOnline(order)).thenReturn(22l);
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.SYSTEM_CANCEL, OrderStatus.RETURNING);

        CommerceChangeOrderStatusModification modification = strategy.changeToLowerStatus(parameter);
        verify(inventoryService, times(1)).holdingAllQuantityOf(order);
        verify(inventoryService, times(1)).subtractPreOrder(order);
        verify(billService, times(1)).createReturnBillWithOrderOnline(order);
        verify(billService, times(0)).changeOrderStatusToOrderReturn(order);
        verify(billService, times(0)).revertBillForOrder(anyString(), anyLong());
        verify(inventoryService, times(0)).changeAllHoldingToAvailableOf(order);
        verify(billService, times(0)).subtractShippingStockOf(order);
        assertEquals(22l, modification.getOrderModel().getBillId(), 0);
    }


    @Test
    public void changeToLowerStatus_SystemCancel_To_Completed_ShouldHoldStock_CreateBill_SubtractShipping() {
        when(billService.createReturnBillWithOrderOnline(order)).thenReturn(22l);
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.SYSTEM_CANCEL, OrderStatus.COMPLETED);

        CommerceChangeOrderStatusModification modification = strategy.changeToLowerStatus(parameter);
        verify(inventoryService, times(1)).holdingAllQuantityOf(order);
        verify(inventoryService, times(1)).subtractPreOrder(order);
        verify(billService, times(1)).createReturnBillWithOrderOnline(order);
        verify(billService, times(1)).subtractShippingStockOf(order);
        verify(billService, times(0)).changeOrderStatusToOrderReturn(order);
        verify(billService, times(0)).revertBillForOrder(anyString(), anyLong());
        verify(inventoryService, times(0)).changeAllHoldingToAvailableOf(order);
        assertEquals(22l, modification.getOrderModel().getBillId(), 0);
    }

    @Test
    public void changeToLowerStatus_Completed_To_New_ShouldRevertBillForOrder() {
        order.setBillId(22l);
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.COMPLETED, OrderStatus.NEW);

        CommerceChangeOrderStatusModification modification = strategy.changeToLowerStatus(parameter);
        verify(billService, times(1)).cancelOnlineOrder(order);
        verify(inventoryService, times(0)).holdingAllQuantityOf(order);
        verify(inventoryService, times(0)).subtractPreOrder(order);
        verify(billService, times(0)).createReturnBillWithOrderOnline(order);
        verify(billService, times(0)).subtractShippingStockOf(order);
        verify(billService, times(0)).changeOrderStatusToOrderReturn(order);
        verify(inventoryService, times(0)).changeAllHoldingToAvailableOf(order);
        assertNull(modification.getOrderModel().getBillId());
    }

    @Test
    public void changeToLowerStatus_Completed_To_PreOrder_ShouldRevertBillForOrder() {
        order.setBillId(22l);
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.COMPLETED, OrderStatus.PRE_ORDER);

        CommerceChangeOrderStatusModification modification = strategy.changeToLowerStatus(parameter);
        verify(billService, times(1)).cancelOnlineOrder(order);
        verify(inventoryService, times(0)).holdingAllQuantityOf(order);
        verify(inventoryService, times(0)).subtractPreOrder(order);
        verify(billService, times(0)).createReturnBillWithOrderOnline(order);
        verify(billService, times(0)).subtractShippingStockOf(order);
        verify(billService, times(0)).changeOrderStatusToOrderReturn(order);
        verify(inventoryService, times(0)).changeAllHoldingToAvailableOf(order);
        assertNull(modification.getOrderModel().getBillId());
    }

    @Test
    public void changeToLowerStatus_Completed_To_Confirmed_ShouldRevertBillForOrder_HoldingStock() {
        order.setBillId(22l);
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.COMPLETED, OrderStatus.CONFIRMED);

        CommerceChangeOrderStatusModification modification = strategy.changeToLowerStatus(parameter);
        verify(billService, times(1)).cancelOnlineOrder(order);
        verify(inventoryService, times(1)).holdingAllQuantityOf(order);
        verify(inventoryService, times(1)).subtractPreOrder(order);
        verify(billService, times(0)).createReturnBillWithOrderOnline(order);
        verify(billService, times(0)).subtractShippingStockOf(order);
        verify(billService, times(0)).changeOrderStatusToOrderReturn(order);
        verify(inventoryService, times(0)).changeAllHoldingToAvailableOf(order);
        assertNull(modification.getOrderModel().getBillId());
    }

    @Test
    public void changeToLowerStatus_Completed_To_Packing_ShouldRevertBillForOrder_HoldingStock() {
        order.setBillId(22l);
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.COMPLETED, OrderStatus.PACKING);

        CommerceChangeOrderStatusModification modification = strategy.changeToLowerStatus(parameter);
        verify(billService, times(1)).cancelOnlineOrder(order);
        verify(inventoryService, times(1)).holdingAllQuantityOf(order);
        verify(inventoryService, times(1)).subtractPreOrder(order);
        verify(billService, times(0)).createReturnBillWithOrderOnline(order);
        verify(billService, times(0)).subtractShippingStockOf(order);
        verify(billService, times(0)).changeOrderStatusToOrderReturn(order);
        verify(inventoryService, times(0)).changeAllHoldingToAvailableOf(order);
        assertNull(modification.getOrderModel().getBillId());
    }

    @Test
    public void changeToLowerStatus_Completed_To_Packaged_ShouldRevertBillForOrder_HoldingStock() {
        order.setBillId(22l);
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.COMPLETED, OrderStatus.PACKAGED);

        CommerceChangeOrderStatusModification modification = strategy.changeToLowerStatus(parameter);
        verify(billService, times(1)).cancelOnlineOrder(order);
        verify(inventoryService, times(1)).holdingAllQuantityOf(order);
        verify(inventoryService, times(1)).subtractPreOrder(order);
        verify(billService, times(0)).createReturnBillWithOrderOnline(order);
        verify(billService, times(0)).subtractShippingStockOf(order);
        verify(billService, times(0)).changeOrderStatusToOrderReturn(order);
        verify(inventoryService, times(0)).changeAllHoldingToAvailableOf(order);
        assertNull(modification.getOrderModel().getBillId());
    }

    @Test
    public void changeToLowerStatus_Completed_To_Shipping_ShouldRevertBillForOrder_HoldingStock_CreateNewBill() {
        order.setBillId(221l);
        when(billService.createReturnBillWithOrderOnline(order)).thenReturn(22l);
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.COMPLETED, OrderStatus.SHIPPING);

        CommerceChangeOrderStatusModification modification = strategy.changeToLowerStatus(parameter);
        verify(billService, times(1)).cancelOnlineOrder(order);
        verify(inventoryService, times(1)).holdingAllQuantityOf(order);
        verify(inventoryService, times(1)).subtractPreOrder(order);
        verify(billService, times(1)).createReturnBillWithOrderOnline(order);
        verify(billService, times(0)).subtractShippingStockOf(order);
        verify(billService, times(0)).changeOrderStatusToOrderReturn(order);
        verify(inventoryService, times(0)).changeAllHoldingToAvailableOf(order);
        assertEquals(22l, modification.getOrderModel().getBillId(), 0);
    }

    @Test
    public void changeToLowerStatus_Returning_To_Shipping_ShouldDoNothing() {
        order.setBillId(22l);
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.RETURNING, OrderStatus.SHIPPING);

        CommerceChangeOrderStatusModification modification = strategy.changeToLowerStatus(parameter);
        verify(billService, times(0)).revertBillForOrder(anyString(), anyLong());
        verify(inventoryService, times(0)).holdingAllQuantityOf(order);
        verify(inventoryService, times(0)).subtractPreOrder(order);
        verify(billService, times(0)).createReturnBillWithOrderOnline(order);
        verify(billService, times(0)).subtractShippingStockOf(order);
        verify(billService, times(0)).changeOrderStatusToOrderReturn(order);
        verify(inventoryService, times(0)).changeAllHoldingToAvailableOf(order);
        assertEquals(22l, modification.getOrderModel().getBillId(), 0);
    }

    @Test
    public void changeToLowerStatus_Returning_To_Packaged_ShouldRevertOrder_HoldingStock() {
        order.setBillId(221l);
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.RETURNING, OrderStatus.PACKAGED);

        CommerceChangeOrderStatusModification modification = strategy.changeToLowerStatus(parameter);
        verify(billService, times(1)).cancelOnlineOrder(order);
        verify(inventoryService, times(1)).holdingAllQuantityOf(order);
        verify(inventoryService, times(1)).subtractPreOrder(order);
        verify(billService, times(1)).subtractShippingStockOf(order);
        verify(billService, times(0)).createReturnBillWithOrderOnline(order);
        verify(billService, times(0)).changeOrderStatusToOrderReturn(order);
        verify(inventoryService, times(0)).changeAllHoldingToAvailableOf(order);
        assertNull(modification.getOrderModel().getBillId());
    }

    @Test
    public void changeToLowerStatus_Shipping_To_Packing_ShouldRevertOrder_HoldingStock() {
        order.setBillId(221l);
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.SHIPPING, OrderStatus.PACKING);

        CommerceChangeOrderStatusModification modification = strategy.changeToLowerStatus(parameter);
        verify(billService, times(1)).cancelOnlineOrder(order);
        verify(inventoryService, times(1)).holdingAllQuantityOf(order);
        verify(inventoryService, times(1)).subtractPreOrder(order);
        verify(billService, times(1)).subtractShippingStockOf(order);
        verify(billService, times(0)).createReturnBillWithOrderOnline(order);
        verify(billService, times(0)).changeOrderStatusToOrderReturn(order);
        verify(inventoryService, times(0)).changeAllHoldingToAvailableOf(order);
        assertNull(modification.getOrderModel().getBillId());
    }

    @Test
    public void changeToLowerStatus_Shipping_To_PreOrder_ShouldRevertOrder_SubTrackShippingStock() {
        order.setBillId(221l);

        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.SHIPPING, OrderStatus.PRE_ORDER);

        CommerceChangeOrderStatusModification modification = strategy.changeToLowerStatus(parameter);
        verify(billService, times(1)).cancelOnlineOrder(order);
        verify(billService, times(1)).subtractShippingStockOf(order);
        verify(inventoryService, times(0)).holdingAllQuantityOf(order);
        verify(inventoryService, times(0)).subtractPreOrder(order);
        verify(billService, times(0)).createReturnBillWithOrderOnline(order);
        verify(billService, times(0)).changeOrderStatusToOrderReturn(order);
        verify(inventoryService, times(0)).changeAllHoldingToAvailableOf(order);
        assertNull(modification.getOrderModel().getBillId());
    }

    @Test
    public void changeToLowerStatus_Shipping_To_New_ShouldRevertOrder_HoldingStock() {
        order.setBillId(221l);
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.SHIPPING, OrderStatus.NEW);

        CommerceChangeOrderStatusModification modification = strategy.changeToLowerStatus(parameter);
        verify(billService, times(1)).cancelOnlineOrder(order);
        verify(billService, times(1)).subtractShippingStockOf(order);
        verify(inventoryService, times(0)).holdingAllQuantityOf(order);
        verify(inventoryService, times(0)).subtractPreOrder(order);
        verify(billService, times(0)).createReturnBillWithOrderOnline(order);
        verify(billService, times(0)).changeOrderStatusToOrderReturn(order);
        verify(inventoryService, times(0)).changeAllHoldingToAvailableOf(order);
        assertNull(modification.getOrderModel().getBillId());
    }

    @Test
    public void changeToLowerStatus_Packing_To_New_ShouldChangeHoldingToAvailable() {
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.PACKING, OrderStatus.NEW);

        CommerceChangeOrderStatusModification modification = strategy.changeToLowerStatus(parameter);
        verify(inventoryService, times(1)).changeAllHoldingToAvailableOf(order);
        verify(billService, times(0)).revertBillForOrder(anyString(), anyLong());
        verify(inventoryService, times(0)).holdingAllQuantityOf(order);
        verify(inventoryService, times(0)).subtractPreOrder(order);
        verify(billService, times(0)).createReturnBillWithOrderOnline(order);
        verify(billService, times(0)).subtractShippingStockOf(order);
        verify(billService, times(0)).changeOrderStatusToOrderReturn(order);
        assertNull(modification.getOrderModel().getBillId());
    }

    @Test
    public void changeToLowerStatus_Confirmed_To_PreOrder_ShouldChangeHoldingToAvailable_resetPreStock() {
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.CONFIRMED, OrderStatus.PRE_ORDER);

        CommerceChangeOrderStatusModification modification = strategy.changeToLowerStatus(parameter);
        verify(inventoryService, times(1)).changeAllHoldingToAvailableOf(order);
        verify(orderService, times(1)).resetPreAndHoldingStockOf(order);
        verify(billService, times(0)).revertBillForOrder(anyString(), anyLong());
        verify(inventoryService, times(0)).holdingAllQuantityOf(order);
        verify(inventoryService, times(0)).subtractPreOrder(order);
        verify(billService, times(0)).createReturnBillWithOrderOnline(order);
        verify(billService, times(0)).subtractShippingStockOf(order);
        verify(billService, times(0)).changeOrderStatusToOrderReturn(order);
        assertNull(modification.getOrderModel().getBillId());
    }

    @Test
    public void changeToLowerStatus_PreOrder_To_New_ShouldResetHoldingEntry_resetPreStock() {
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.PRE_ORDER, OrderStatus.NEW);

        CommerceChangeOrderStatusModification modification = strategy.changeToLowerStatus(parameter);
        verify(inventoryService, times(0)).changeAllHoldingToAvailableOf(order);
        verify(inventoryService, times(1)).resetHoldingStockOf(order);
        verify(orderService, times(1)).resetPreAndHoldingStockOf(order);
        verify(inventoryService, times(1)).subtractPreOrder(order);
        verify(billService, times(0)).revertBillForOrder(anyString(), anyLong());
        verify(inventoryService, times(0)).holdingAllQuantityOf(order);
        verify(billService, times(0)).createReturnBillWithOrderOnline(order);
        verify(billService, times(0)).subtractShippingStockOf(order);
        verify(billService, times(0)).changeOrderStatusToOrderReturn(order);
        assertNull(modification.getOrderModel().getBillId());
    }

    @Test
    public void changeStatusOrder_NewToConfirmed_Success() {
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.NEW, OrderStatus.CONFIRMED);

        strategy.changeStatusOrder(parameter);
        verify(inventoryService).holdingAllQuantityOf(order);
        verify(inventoryService).subtractPreOrder(order);
        verify(orderService).resetPreAndHoldingStockOf(order);
        verify(orderService).save(order);
        verify(orderHistoryService).save(any(OrderHistoryModel.class));
    }

    @Test
    public void changeStatusOrder_NewToPreOrder_success() {
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.NEW, OrderStatus.PRE_ORDER);

        strategy.changeStatusOrder(parameter);
        verify(orderService).save(order);
        verify(orderHistoryService).save(any(OrderHistoryModel.class));
    }

    @Test
    public void changeStatusOrder_CompletedToRetail_success() {
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.COMPLETED, OrderStatus.CHANGE_TO_RETAIL);

        strategy.changeStatusOrder(parameter);
        verify(orderService).save(order);
        verify(orderHistoryService).save(any(OrderHistoryModel.class));
    }

    @Test
    public void changeStatusOrder_ConfirmedToSystemCancel_Success() {
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.CONFIRMED, OrderStatus.SYSTEM_CANCEL);
        parameter.setCancelText("cancel");

        strategy.changeStatusOrder(parameter);
        verify(orderService).save(order);
        verify(orderHistoryService).save(any(OrderHistoryModel.class));
    }

    @Test
    public void changeStatusOrder_ConfirmedToSystemCancel_revertCoupon() {
        order.setType(OrderType.ONLINE.toString());
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.CONFIRMED, OrderStatus.SYSTEM_CANCEL);
        parameter.setCancelText("cancel");

        strategy.changeStatusOrder(parameter);
        verify(orderService).save(order);
        verify(orderHistoryService).save(any(OrderHistoryModel.class));
        verify(couponService).revertAllCouponToOrder(any(OrderModel.class));
    }

    @Test
    public void changeStatusOrder_ConfirmedToCustomerCancel_WithEmptyReason() {
        try {
            parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.CONFIRMED, OrderStatus.SYSTEM_CANCEL);

            strategy.changeStatusOrder(parameter);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_CANCEL_TEXT_ORDER_ONLINE.code(), e.getCode());
        }
    }

    @Test
    public void changeStatusOrder_newToConfirmed_maximumDiscount() {
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.NEW, OrderStatus.CONFIRMED);
        parameter.setConfirmDiscount(true);

        strategy.changeStatusOrder(parameter);
        verify(orderService).save(order);
        verify(orderHistoryService).save(any(OrderHistoryModel.class));
    }

    @Test
    public void changeStatusOrder_Confirmed2New_maximumDiscount() {
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.CONFIRMED, OrderStatus.NEW);

        strategy.changeStatusOrder(parameter);
        verify(orderService).save(order);
        verify(orderHistoryService).save(any(OrderHistoryModel.class));
    }

    @Test
    public void changeStatusOnlineOrder_CompletedToNotChangeToRetail_hasRedeemAndReward() {
        order.setTotalRewardAmount(1d);
        order.setRedeemAmount(1d);
        order.setType(OrderType.ONLINE.toString());
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.COMPLETED, OrderStatus.CUSTOMER_CANCEL);
        parameter.setCancelText("cancel");
        strategy.changeStatusOrder(parameter);
        verify(orderService).save(order);
        verify(orderHistoryService).save(any(OrderHistoryModel.class));
        verify(loyaltyService, times(1)).refund(order, null, 1d);
        verify(loyaltyService, times(1)).revertOnlineOrderReward(order, 1d);
        verify(invoiceService, times(1)).cancelLoyaltyRedeemInvoice(order);
        verify(invoiceService, times(1)).cancelLoyaltyRewardInvoice(order);
    }


    @Test
    public void changeStatusOnlineOrder_CompletedToNotChangeToRetail_hasNotRedeemAndNotReward() {
        order.setType(OrderType.ONLINE.toString());
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.COMPLETED, OrderStatus.CUSTOMER_CANCEL);
        parameter.setCancelText("cancel");
        strategy.changeStatusOrder(parameter);
        verify(orderService).save(order);
        verify(orderHistoryService).save(any(OrderHistoryModel.class));
        verify(loyaltyService, times(0)).refund(order, null, 1d);
        verify(loyaltyService, times(0)).revert(order, null, 1d);
        verify(invoiceService, times(0)).cancelLoyaltyRedeemInvoice(order);
        verify(invoiceService, times(0)).cancelLoyaltyRewardInvoice(order);
    }

    @Test
    public void changeStatusOnlineOrder_CompletedToNotChangeToRetail_hasNotRedeemAndReward() {
        order.setTotalRewardAmount(1d);
        order.setType(OrderType.ONLINE.toString());
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.COMPLETED, OrderStatus.CUSTOMER_CANCEL);
        parameter.setCancelText("cancel");
        strategy.changeStatusOrder(parameter);
        verify(orderService).save(order);
        verify(orderHistoryService).save(any(OrderHistoryModel.class));
        verify(loyaltyService, times(0)).refund(order, null, 1d);
        verify(loyaltyService, times(1)).revertOnlineOrderReward(order, 1d);
        verify(invoiceService, times(0)).cancelLoyaltyRedeemInvoice(order);
        verify(invoiceService, times(1)).cancelLoyaltyRewardInvoice(order);
    }

    // old status is completed, new status between completed and change to retail
    @Test
    public void changeStatusOnlineOrder_CompletedToNotChangeToRetail_hasRedeemAndNotReward() {
        order.setRedeemAmount(1d);
        order.setType(OrderType.ONLINE.toString());
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.COMPLETED, OrderStatus.CUSTOMER_CANCEL);
        parameter.setCancelText("cancel");
        strategy.changeStatusOrder(parameter);
        verify(orderService).save(order);
        verify(orderHistoryService).save(any(OrderHistoryModel.class));
        verify(loyaltyService, times(1)).refund(order, null, 1d);
        verify(loyaltyService, times(0)).revert(order, null, 1d);
        verify(invoiceService, times(1)).cancelLoyaltyRedeemInvoice(order);
        verify(invoiceService, times(0)).cancelLoyaltyRewardInvoice(order);
    }

    // old status is completed, new status smaller than completed
    @Test
    public void changeStatusOnlineOrder_CompletedToNotChangeToRetail_hasRedeemCase2() {
        order.setRedeemAmount(1d);
        order.setType(OrderType.ONLINE.toString());
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.COMPLETED, OrderStatus.RETURNING);
        strategy.changeStatusOrder(parameter);
        verify(orderService).save(order);
        verify(orderHistoryService).save(any(OrderHistoryModel.class));
        verify(loyaltyService, times(1)).refund(order, null, 1d);
        verify(loyaltyService, times(0)).revert(order, null, 1d);
        verify(invoiceService, times(1)).cancelLoyaltyRedeemInvoice(order);
        verify(invoiceService, times(0)).cancelLoyaltyRewardInvoice(order);
        assertNull(order.getRedeemAmount());
    }

    // old status between completed and change to retail
    @Test
    public void changeStatusOnlineOrder_CancelStatusToOtherStatus_hasRedeem() {
        order.setRedeemAmount(1d);
        order.setType(OrderType.ONLINE.toString());
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.CUSTOMER_CANCEL, OrderStatus.RETURNING);
        PaymentTransactionModel paymentTransactionModel = new PaymentTransactionModel();
        when(paymentTransactionService.findLoyaltyRedeem(order)).thenReturn(paymentTransactionModel);
        strategy.changeStatusOrder(parameter);
        verify(orderService).save(order);
        verify(orderHistoryService).save(any(OrderHistoryModel.class));
        verify(loyaltyService, times(0)).refund(order, null, 1d);
        verify(loyaltyService, times(0)).revert(order, null, 1d);
        verify(invoiceService, times(0)).cancelLoyaltyRedeemInvoice(order);
        verify(invoiceService, times(0)).cancelLoyaltyRewardInvoice(order);
        verify(paymentTransactionService, times(1)).findLoyaltyRedeem(order);
        verify(paymentTransactionService, times(1)).save(paymentTransactionModel);
        assertNull(order.getRedeemAmount());
    }

    // old status between completed and confirmed, new status between completed and change to retail
    @Test
    public void changeStatusOnlineOrder_OldStatusBetweenConfirmedAndCompleted_To_CancelStatus_hasRedeem() {
        order.setRedeemAmount(1d);
        order.setType(OrderType.ONLINE.toString());
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.RETURNING, OrderStatus.CUSTOMER_CANCEL);
        parameter.setCancelText("cancel");
        strategy.changeStatusOrder(parameter);
        verify(orderService).save(order);
        verify(orderHistoryService).save(any(OrderHistoryModel.class));
        verify(loyaltyService, times(0)).refund(order, null, 1d);
        verify(loyaltyService, times(0)).revert(order, null, 1d);
        verify(invoiceService, times(0)).cancelLoyaltyRedeemInvoice(order);
        verify(invoiceService, times(0)).cancelLoyaltyRewardInvoice(order);
        verify(loyaltyService, times(1)).cancelPendingRedeemForCancelOrder(order);
    }

    // old status between completed and change to retail, new status between completed and change to retail
    @Test
    public void changeStatusOnlineOrder_CancelStatusToCancelStatus_hasRedeem() {
        order.setRedeemAmount(1d);
        order.setType(OrderType.ONLINE.toString());
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.CUSTOMER_CANCEL, OrderStatus.SYSTEM_CANCEL);
        parameter.setCancelText("cancel");
        strategy.changeStatusOrder(parameter);
        verify(orderService).save(order);
        verify(orderHistoryService).save(any(OrderHistoryModel.class));
        verify(loyaltyService, times(0)).refund(order, null, 1d);
        verify(loyaltyService, times(0)).revert(order, null, 1d);
        verify(invoiceService, times(0)).cancelLoyaltyRedeemInvoice(order);
        verify(invoiceService, times(0)).cancelLoyaltyRewardInvoice(order);
        verify(loyaltyService, times(0)).cancelPendingRedeem(order);
        assertNotNull(order.getRedeemAmount());
    }


    // old status between completed and change to retail
    @Test
    public void changeStatusOnlineOrder_CancelStatusToOtherStatus_hasNotRedeem() {
        order.setType(OrderType.ONLINE.toString());
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.CUSTOMER_CANCEL, OrderStatus.RETURNING);
        strategy.changeStatusOrder(parameter);
        verify(orderService).save(order);
        verify(orderHistoryService).save(any(OrderHistoryModel.class));
        verify(loyaltyService, times(0)).refund(order, null, 1d);
        verify(loyaltyService, times(0)).revert(order, null, 1d);
        verify(invoiceService, times(0)).cancelLoyaltyRedeemInvoice(order);
        verify(invoiceService, times(0)).cancelLoyaltyRewardInvoice(order);
        assertNull(order.getRedeemAmount());
    }

    // old status between completed and confirmed, new status between completed and change to retail
    @Test
    public void changeStatusOnlineOrder_OldStatusBetweenConfirmedAndCompleted_To_CancelStatus_hasNotRedeem() {
        order.setType(OrderType.ONLINE.toString());
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.RETURNING, OrderStatus.CUSTOMER_CANCEL);
        parameter.setCancelText("cancel");
        strategy.changeStatusOrder(parameter);
        verify(orderService).save(order);
        verify(orderHistoryService).save(any(OrderHistoryModel.class));
        verify(loyaltyService, times(0)).refund(order, null, 1d);
        verify(loyaltyService, times(0)).revert(order, null, 1d);
        verify(invoiceService, times(0)).cancelLoyaltyRedeemInvoice(order);
        verify(invoiceService, times(0)).cancelLoyaltyRewardInvoice(order);
        verify(loyaltyService, times(0)).cancelPendingRedeem(order);
    }

    // old status between completed and change to retail, new status between completed and change to retail
    @Test
    public void changeStatusOnlineOrder_CancelStatusToCancelStatus_hasNotRedeem() {
        order.setType(OrderType.ONLINE.toString());
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.CUSTOMER_CANCEL, OrderStatus.SYSTEM_CANCEL);
        parameter.setCancelText("cancel");
        strategy.changeStatusOrder(parameter);
        verify(orderService).save(order);
        verify(orderHistoryService).save(any(OrderHistoryModel.class));
        verify(loyaltyService, times(0)).refund(order, null, 1d);
        verify(loyaltyService, times(0)).revert(order, null, 1d);
        verify(invoiceService, times(0)).cancelLoyaltyRedeemInvoice(order);
        verify(invoiceService, times(0)).cancelLoyaltyRewardInvoice(order);
        verify(loyaltyService, times(0)).cancelPendingRedeem(order);
        assertNull(order.getRedeemAmount());
    }

    @Test
    public void importChangeStatusOrder_validateChangeStatusOrder_oldStatusEquals_COMPLETED() {
        try {
            order.setType(OrderType.ONLINE.toString());
            when(orderEntryRepository.findAllByOrderCode(anyString())).thenReturn(Arrays.asList(entry1, entry2));
            parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.CUSTOMER_CANCEL, OrderStatus.CONFIRMED);
            parameter.setCancelText("cancel");
            strategy.importChangeStatusOrder(parameter, 1L);
            fail("new throw Exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CANNOT_CHANGE_WITH_CURRENT_STATUS_OVER_COMPLETED.message(), e.getMessage());
        }
    }

    @Test
    public void importChangeStatusOrder_validateChangeStatusOrder_NOT_ACCEPT_CHANGE_STATUS_BEFORE_SHIPPING() {
        try {
            order.setType(OrderType.ONLINE.toString());
            when(orderEntryRepository.findAllByOrderCode(anyString())).thenReturn(Arrays.asList(entry1, entry2));
            parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.CONFIRMED, OrderStatus.ORDER_RETURN);
            parameter.setCancelText("cancel");
            strategy.importChangeStatusOrder(parameter, 1L);
            fail("new throw Exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.NOT_ACCEPT_CHANGE_STATUS_BEFORE_SHIPPING.message(), e.getMessage());
        }
    }

    @Test
    public void importChangeStatusOrder_validateChangeStatusOrder_ExistFoodOrBeverage() {
        try {
            order.setType(OrderType.ONLINE.toString());
            entry1.setProductId(2L);
            entry2.setProductId(23L);
            ProductSearchModel productSearchModel = new ProductSearchModel();
            productSearchModel.setId(2L);
            when(orderEntryRepository.findAllByOrderCode(anyString())).thenReturn(Arrays.asList(entry1, entry2));
            productSearchModel.setProductType(ProductType.FOOD.code());
            when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productSearchModel));
            parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.NEW, OrderStatus.PRE_ORDER);
            parameter.setCancelText("cancel");
            strategy.importChangeStatusOrder(parameter, 1L);
            fail("new throw Exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CANNOT_PRE_ORDER_CONTAIN_FOOD_BEVERAGE_ENTRY.message(), e.getMessage());
        }
    }

    @Test
    /*
    oldStatus: NEW
    newStatus:CONFIRMING
     */
    public void importChangeStatusOrder_case1() {
        order.setType(OrderType.ONLINE.toString());
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.NEW, OrderStatus.CONFIRMING);
        when(orderEntryRepository.findAllByOrderCode(anyString())).thenReturn(Arrays.asList(entry1, entry2));
        strategy.importChangeStatusOrder(parameter, 1L);
        verify(orderService, times(1)).save(order);
        verify(orderHistoryService, times(1)).save(any(OrderHistoryModel.class));
        verify(orderService, times(0)).holdingStockAndResetPreStockOf(any());
        verify(loyaltyService, times(0)).resetPaymentForLoyalty(any());
        verify(orderService, times(0)).resetPreAndHoldingStockOfEntries(any());
    }

    @Test
    /*
    oldStatus: NEW
    newStatus:PRE_ORDER
     */
    public void importChangeStatusOrder_case2() {
        order.setType(OrderType.ONLINE.toString());
        entry1.setProductId(2L);
        entry2.setProductId(3L);
        when(orderEntryRepository.findAllByOrderCode(anyString())).thenReturn(Arrays.asList(entry1, entry2));
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(new ProductSearchModel()));
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.NEW, OrderStatus.PRE_ORDER);
        strategy.importChangeStatusOrder(parameter, 1L);
        verify(orderService, times(1)).save(order);
        verify(orderHistoryService, times(1)).save(any(OrderHistoryModel.class));
        verify(orderService, times(1)).holdingStockAndResetPreStockOf(any());
        verify(loyaltyService, times(0)).resetPaymentForLoyalty(any());
        verify(orderService, times(0)).resetPreAndHoldingStockOfEntries(any());
    }

    @Test
    /*
    oldStatus: NEW
    newStatus:CONFIRMED
     */
    public void importChangeStatusOrder_case3() {
        when(orderEntryRepository.findAllByOrderCode(anyString())).thenReturn(Arrays.asList(entry1, entry2));
        order.setType(OrderType.ONLINE.toString());
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.NEW, OrderStatus.CONFIRMED);
        strategy.importChangeStatusOrder(parameter, 1L);
        verify(orderService, times(1)).save(order);
        verify(orderHistoryService, times(1)).save(any(OrderHistoryModel.class));
        verify(orderService, times(0)).holdingStockAndResetPreStockOf(any());
        verify(loyaltyService, times(0)).resetPaymentForLoyalty(any());
        verify(orderService, times(1)).resetPreAndHoldingStockOfEntries(any());
    }

    @Test
    /*
    oldStatus: PRE_ORDER
    newStatus:CONFIRMED
     */
    public void importChangeStatusOrder_case4() {
        order.setType(OrderType.ONLINE.toString());
        when(orderEntryRepository.findAllByOrderCode(anyString())).thenReturn(Arrays.asList(entry1, entry2));
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.PRE_ORDER, OrderStatus.CONFIRMED);
        strategy.importChangeStatusOrder(parameter, 1L);
        verify(orderService, times(1)).save(order);
        verify(orderHistoryService, times(1)).save(any(OrderHistoryModel.class));
        verify(orderService, times(0)).holdingStockAndResetPreStockOf(any());
        verify(loyaltyService, times(0)).resetPaymentForLoyalty(any());
        verify(orderService, times(1)).resetPreAndHoldingStockOfEntries(any());
    }

    @Test
    /*
    oldStatus: CONFIRMED
    newStatus:SHIPPING
     */
    public void importChangeStatusOrder_case5() {
        order.setType(OrderType.ONLINE.toString());
        when(orderEntryRepository.findAllByOrderCode(anyString())).thenReturn(Arrays.asList(entry1, entry2));
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.CONFIRMED, OrderStatus.SHIPPING);
        strategy.importChangeStatusOrder(parameter, 1L);
        verify(orderService, times(1)).save(order);
        verify(orderHistoryService, times(1)).save(any(OrderHistoryModel.class));
        verify(orderService, times(0)).holdingStockAndResetPreStockOf(any());
        verify(loyaltyService, times(0)).resetPaymentForLoyalty(any());
        verify(orderService, times(0)).resetPreAndHoldingStockOfEntries(any());
    }

    @Test
    /*
    oldStatus: SHIPPING
    newStatus:ORDER_RETURN
     */
    public void importChangeStatusOrder_case6() {
        order.setType(OrderType.ONLINE.toString());
        order.setBillId(12L);
        when(orderEntryRepository.findAllByOrderCode(anyString())).thenReturn(Arrays.asList(entry1, entry2));
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.SHIPPING, OrderStatus.ORDER_RETURN);
        strategy.importChangeStatusOrder(parameter, 1L);
        verify(orderService, times(1)).save(order);
        verify(orderHistoryService, times(1)).save(any(OrderHistoryModel.class));
        verify(orderService, times(0)).holdingStockAndResetPreStockOf(any());
        verify(loyaltyService, times(0)).resetPaymentForLoyalty(any());
        verify(orderService, times(0)).resetPreAndHoldingStockOfEntries(any());
        assertNull(order.getBillId());
    }

    @Test
    /*
    oldStatus: SHIPPING
    newStatus:COMPLETED
     */
    public void importChangeStatusOrder_case7() {
        order.setType(OrderType.ONLINE.toString());
        when(orderEntryRepository.findAllByOrderCode(anyString())).thenReturn(Arrays.asList(entry1, entry2));
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.SHIPPING, OrderStatus.COMPLETED);
        strategy.importChangeStatusOrder(parameter, 1L);
        verify(orderService, times(1)).save(order);
        verify(orderHistoryService, times(1)).save(any(OrderHistoryModel.class));
        verify(orderService, times(0)).holdingStockAndResetPreStockOf(any());
        verify(loyaltyService, times(0)).resetPaymentForLoyalty(any());
        verify(orderService, times(0)).resetPreAndHoldingStockOfEntries(any());
    }

    @Test
    /*
    oldStatus: SHIPPING
    newStatus:CUSTOMER_CANCEL
     */
    public void importChangeStatusOrder_case8() {
        order.setType(OrderType.ONLINE.toString());
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.SHIPPING, OrderStatus.CUSTOMER_CANCEL);
        strategy.importChangeStatusOrder(parameter, 1L);
        verify(orderService, times(1)).save(order);
        verify(orderHistoryService, times(1)).save(any(OrderHistoryModel.class));
        verify(orderService, times(0)).holdingStockAndResetPreStockOf(any());
        verify(loyaltyService, times(0)).resetPaymentForLoyalty(any());
        verify(orderService, times(0)).resetPreAndHoldingStockOfEntries(any());
    }

    @Test
    /*
    oldStatus: CONFIRMED
    newStatus:CUSTOMER_CANCEL
     */
    public void importChangeStatusOrder_case9() {
        order.setType(OrderType.ONLINE.toString());
        when(orderEntryRepository.findAllByOrderCode(anyString())).thenReturn(Arrays.asList(entry1, entry2));
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.CONFIRMED, OrderStatus.CUSTOMER_CANCEL);
        strategy.importChangeStatusOrder(parameter, 1L);
        verify(orderService, times(1)).save(order);
        verify(orderHistoryService, times(1)).save(any(OrderHistoryModel.class));
        verify(orderService, times(0)).holdingStockAndResetPreStockOf(any());
        verify(loyaltyService, times(0)).resetPaymentForLoyalty(any());
        verify(orderService, times(1)).resetPreAndHoldingStockOfEntries(any());
    }

    @Test
    /*
    oldStatus: SHIPPING
    newStatus:CHANGE_TO_RETAIL
     */
    public void importChangeStatusOrder_case10() {
        order.setType(OrderType.ONLINE.toString());
        order.setBillId(12L);
        when(orderEntryRepository.findAllByOrderCode(anyString())).thenReturn(Arrays.asList(entry1, entry2));
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.SHIPPING, OrderStatus.CHANGE_TO_RETAIL);
        strategy.importChangeStatusOrder(parameter, 1L);
        verify(orderService, times(1)).save(order);
        verify(orderHistoryService, times(1)).save(any(OrderHistoryModel.class));
        verify(orderService, times(0)).holdingStockAndResetPreStockOf(any());
        verify(paymentTransactionService, times(1)).resetPaymentForLoyaltyRedeem(any());
        verify(commercePlaceOrderStrategy, times(1)).changeBillToRetailForKafkaImportOrderStatus(any());
        verify(orderService, times(0)).resetPreAndHoldingStockOfEntries(any());
    }

    @Test
    /*
    oldStatus: CONFIRMED
    newStatus:CHANGE_TO_RETAIL
     */
    public void importChangeStatusOrder_case11() {
        order.setType(OrderType.ONLINE.toString());
        when(orderEntryRepository.findAllByOrderCode(anyString())).thenReturn(Arrays.asList(entry1, entry2));
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.CONFIRMED, OrderStatus.CHANGE_TO_RETAIL);
        strategy.importChangeStatusOrder(parameter, 1L);
        verify(orderService, times(1)).save(order);
        verify(orderHistoryService, times(1)).save(any(OrderHistoryModel.class));
        verify(orderService, times(0)).holdingStockAndResetPreStockOf(any());
        verify(paymentTransactionService, times(1)).resetPaymentForLoyaltyRedeem(any());
        verify(commercePlaceOrderStrategy, times(1)).changeBillToRetailForKafkaImportOrderStatus(any());
        verify(orderService, times(1)).resetPreAndHoldingStockOfEntries(any());
    }

    @Test
    /*
    oldStatus: PACKING
    newStatus:CONFIRMED
     */
    public void importChangeStatusOrder_changeLowerStatus_case12() {
        order.setType(OrderType.ONLINE.toString());
        when(orderEntryRepository.findAllByOrderCode(anyString())).thenReturn(Arrays.asList(entry1, entry2));
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.PACKING, OrderStatus.CONFIRMED);
        strategy.importChangeStatusOrder(parameter, 1L);
        verify(orderService, times(1)).save(order);
        verify(orderHistoryService, times(1)).save(any(OrderHistoryModel.class));
        verify(orderService, times(0)).holdingStockAndResetPreStockOf(any());
        verify(loyaltyService, times(0)).resetPaymentForLoyalty(any());
        verify(commercePlaceOrderStrategy, times(0)).changeBillToRetail(any());
        verify(orderService, times(0)).resetPreAndHoldingStockOfEntries(any());
    }

    @Test
    /*
    oldStatus: SHIPPING
    newStatus:CONFIRMING_CHANGE
     */
    public void importChangeStatusOrder_changeLowerStatus_case13() {
        order.setBillId(2L);
        order.setType(OrderType.ONLINE.toString());
        when(orderEntryRepository.findAllByOrderCode(anyString())).thenReturn(Arrays.asList(entry1, entry2));
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.SHIPPING, OrderStatus.CONFIRMING_CHANGE);
        strategy.importChangeStatusOrder(parameter, 1L);
        verify(orderService, times(1)).save(order);
        verify(orderHistoryService, times(1)).save(any(OrderHistoryModel.class));
        verify(orderService, times(0)).holdingStockAndResetPreStockOf(any());
        verify(loyaltyService, times(0)).resetPaymentForLoyalty(any());
        verify(commercePlaceOrderStrategy, times(0)).changeBillToRetail(any());
        verify(orderService, times(0)).resetPreAndHoldingStockOfEntries(any());
        assertNull(order.getBillId());
    }

    @Test
    /*
    oldStatus: SHIPPING
    newStatus:PRE_ORDER
     */
    public void importChangeStatusOrder_changeLowerStatus_case14() {
        order.setBillId(2L);
        order.setType(OrderType.ONLINE.toString());
        entry1.setProductId(2L);
        entry2.setProductId(3L);
        when(orderEntryRepository.findAllByOrderCode(anyString())).thenReturn(Arrays.asList(entry1, entry2));
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(new ProductSearchModel()));
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.SHIPPING, OrderStatus.PRE_ORDER);
        strategy.importChangeStatusOrder(parameter, 1L);
        verify(orderService, times(1)).save(order);
        verify(orderHistoryService, times(1)).save(any(OrderHistoryModel.class));
        verify(orderService, times(1)).holdingStockAndResetPreStockOf(any());
        verify(loyaltyService, times(0)).resetPaymentForLoyalty(any());
        verify(commercePlaceOrderStrategy, times(0)).changeBillToRetail(any());
        verify(orderService, times(0)).resetPreAndHoldingStockOfEntries(any());
        assertNull(order.getBillId());
    }

    @Test
    /*
    oldStatus: CONFIRMED
    newStatus:PRE_ORDER
     */
    public void importChangeStatusOrder_changeLowerStatus_case15() {
        order.setType(OrderType.ONLINE.toString());
        entry1.setProductId(2L);
        entry2.setProductId(3L);
        when(orderEntryRepository.findAllByOrderCode(anyString())).thenReturn(Arrays.asList(entry1, entry2));
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(new ProductSearchModel()));
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.CONFIRMED, OrderStatus.PRE_ORDER);
        strategy.importChangeStatusOrder(parameter, 1L);
        verify(orderService, times(1)).save(order);
        verify(orderHistoryService, times(1)).save(any(OrderHistoryModel.class));
        verify(orderService, times(1)).holdingStockAndResetPreStockOf(any());
        verify(loyaltyService, times(0)).resetPaymentForLoyalty(any());
        verify(commercePlaceOrderStrategy, times(0)).changeBillToRetail(any());
        verify(orderService, times(0)).resetPreAndHoldingStockOfEntries(any());
    }

    @Test
    /*
    oldStatus: CONFIRMED
    newStatus:CONFIRMING
     */
    public void importChangeStatusOrder_changeLowerStatus_case16() {
        order.setType(OrderType.ONLINE.toString());
        when(orderEntryRepository.findAllByOrderCode(anyString())).thenReturn(Arrays.asList(entry1, entry2));
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.CONFIRMED, OrderStatus.CONFIRMING);
        strategy.importChangeStatusOrder(parameter, 1L);
        verify(orderService, times(1)).save(order);
        verify(orderHistoryService, times(1)).save(any(OrderHistoryModel.class));
        verify(orderService, times(0)).holdingStockAndResetPreStockOf(any());
        verify(loyaltyService, times(0)).resetPaymentForLoyalty(any());
        verify(commercePlaceOrderStrategy, times(0)).changeBillToRetail(any());
        verify(orderService, times(1)).resetPreAndHoldingStockOfEntries(any());
    }

    @Test
    /*
    oldStatus: PRE_ORDER
    newStatus:NEW
     */
    public void importChangeStatusOrder_changeLowerStatus_case17() {
        order.setType(OrderType.ONLINE.toString());
        parameter = new CommerceChangeOrderStatusParameter(order, OrderStatus.PRE_ORDER, OrderStatus.NEW);
        strategy.importChangeStatusOrder(parameter, 1L);
        verify(orderService, times(1)).save(order);
        verify(orderHistoryService, times(1)).save(any(OrderHistoryModel.class));
        verify(orderService, times(0)).holdingStockAndResetPreStockOf(any());
        verify(loyaltyService, times(0)).resetPaymentForLoyalty(any());
        verify(commercePlaceOrderStrategy, times(0)).changeBillToRetail(any());
        verify(orderService, times(1)).resetPreAndHoldingStockOfEntries(any());
    }

}
