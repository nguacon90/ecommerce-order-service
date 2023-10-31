package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.dto.CommerceCartModification;
import com.vctek.orderservice.dto.request.ComboOrToppingOrderRequest;
import com.vctek.orderservice.feignclient.BillClient;
import com.vctek.orderservice.feignclient.dto.BillDetailRequest;
import com.vctek.orderservice.feignclient.dto.BillRequest;
import com.vctek.orderservice.feignclient.dto.OrderBillRequest;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.service.CalculationService;
import com.vctek.orderservice.service.OrderService;
import com.vctek.util.ComboType;
import com.vctek.util.OrderStatus;
import com.vctek.util.OrderType;
import com.vctek.util.ReceiptDeliveryReason;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class BillServiceTest {
    private BillServiceImpl service;

    @Mock
    private BillClient billClient;
    @Mock
    private CalculationService calculationService;
    @Mock
    private OrderModel order;
    @Mock
    private AbstractOrderEntryModel normalEntry;

    @Mock
    private AbstractOrderEntryModel comboEntry;
    @Mock
    private CommerceCartModification commerceCartModification;
    @Mock
    private OrderService orderService;
    private ToppingItemModel toppingItem1 = new ToppingItemModel();
    private ToppingItemModel toppingItem2 = new ToppingItemModel();

    private ArgumentCaptor<BillRequest> captor = ArgumentCaptor.forClass(BillRequest.class);


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new BillServiceImpl(billClient, calculationService);
        service.setOrderService(orderService);
        when(order.getCompanyId()).thenReturn(1L);
        when(order.getCode()).thenReturn("orderCode");
        when(commerceCartModification.getEntry()).thenReturn(normalEntry);
        when(normalEntry.getComboType()).thenReturn(null);
        toppingItem1.setBasePrice(10000d);
        toppingItem1.setQuantity(2);
        toppingItem1.setProductId(1l);
        toppingItem1.setDiscountOrderToItem(100d);

        toppingItem2.setBasePrice(10000d);
        toppingItem2.setQuantity(2);
        toppingItem2.setProductId(2l);
        toppingItem2.setDiscountOrderToItem(100d);
    }

    @Test
    public void createBillForOrder_online() {
        when(order.getType()).thenReturn(OrderType.ONLINE.toString());
        service.createBillForOrder(order);
        verify(billClient).revertReturnBillWithOrder(any(OrderBillRequest.class));
        verify(billClient).createReturnBillWithOrder(captor.capture());
        BillRequest billRequest = captor.getValue();
        assertEquals(ReceiptDeliveryReason.RETURN_BILL_ONLINE_REASON.toString(), billRequest.getReasonCode());
    }

    @Test
    public void createBillForOrder_retail() {
        when(order.getType()).thenReturn(OrderType.RETAIL.toString());
        service.createBillForOrder(order);
        verify(billClient).revertReturnBillWithOrder(any(OrderBillRequest.class));
        verify(billClient).createReturnBillWithOrder(captor.capture());
        BillRequest billRequest = captor.getValue();
        assertEquals(ReceiptDeliveryReason.RETURN_BILL_RETAIL_REASON.toString(), billRequest.getReasonCode());
    }

    @Test
    public void createBillForOrder_wholesale() {
        when(order.getType()).thenReturn(OrderType.WHOLESALE.toString());
        service.createBillForOrder(order);
        verify(billClient).revertReturnBillWithOrder(any(OrderBillRequest.class));
        verify(billClient).createReturnBillWithOrder(captor.capture());
        BillRequest billRequest = captor.getValue();
        assertEquals(ReceiptDeliveryReason.RETURN_BILL_WHOLESALE_REASON.toString(), billRequest.getReasonCode());
    }

    @Test
    public void updateProductInReturnBillWithOrder() {
        service.updateProductInReturnBillWithOrder(order, commerceCartModification);

        verify(calculationService).calculateFinalDiscountOfEntry(normalEntry);
        verify(billClient).updateProductInReturnBillWithOrder(any(OrderBillRequest.class));
    }

    @Test
    public void updateComboInReturnBillWithOrder() {
        Set<SubOrderEntryModel> subOrderEntryModels = new HashSet<>();
        subOrderEntryModels.add(new SubOrderEntryModel());
        when(normalEntry.getSubOrderEntries()).thenReturn(subOrderEntryModels);
        when(orderService.isComboEntry(any(AbstractOrderEntryModel.class))).thenReturn(true);
        service.updateProductInReturnBillWithOrder(order, commerceCartModification);

        verify(billClient).updateComBoInReturnBillWithOrder(any(ComboOrToppingOrderRequest.class));
    }

    @Test
    public void deleteProductInReturnBillWithOrder() {
        when(commerceCartModification.getEntry()).thenReturn(new AbstractOrderEntryModel());
        service.deleteProductInReturnBillWithOrder(order, commerceCartModification);

        verify(billClient).deleteProductInReturnBillWithOrder(any(OrderBillRequest.class));
    }

    @Test
    public void deleteComboInReturnBillWithOrder() {
        AbstractOrderEntryModel entry = new AbstractOrderEntryModel();
        Set<SubOrderEntryModel> subOrderEntryModels = new HashSet<>();
        subOrderEntryModels.add(new SubOrderEntryModel());
        entry.setSubOrderEntries(subOrderEntryModels);
        entry.setQuantity(1L);
        when(commerceCartModification.getEntry()).thenReturn(entry);
        when(orderService.isComboEntry(any(AbstractOrderEntryModel.class))).thenReturn(true);
        service.deleteProductInReturnBillWithOrder(order, commerceCartModification);

        verify(billClient).deleteComboInReturnBillWithOrder(any(ComboOrToppingOrderRequest.class));
    }

    @Test
    public void createBillForReturnOrder() {
        service.createBillForReturnOrder(new BillRequest());
        verify(billClient).createReceiptBillForOrder(any(BillRequest.class));
    }

    @Test
    public void populateBillRequest() {
        when(orderService.isComboEntry(any(AbstractOrderEntryModel.class))).thenReturn(false);
        List<AbstractOrderEntryModel> entries = new ArrayList<>();
        AbstractOrderEntryModel orderEntry = new AbstractOrderEntryModel();
        entries.add(orderEntry);
        when(order.getEntries()).thenReturn(entries);
        BillRequest request = service.populateBillRequest(order);
        verify(calculationService).calculateFinalDiscountOfEntry(orderEntry);
        assertEquals(1, request.getBillDetails().size());
    }

    @Test
    public void populateBillRequest_Combo() {
        when(orderService.isComboEntry(any(AbstractOrderEntryModel.class))).thenReturn(true);
        List<AbstractOrderEntryModel> entries = new ArrayList<>();
        AbstractOrderEntryModel orderEntry = new AbstractOrderEntryModel();
        orderEntry.setSubOrderEntries(Collections.singleton(new SubOrderEntryModel()));
        entries.add(orderEntry);
        when(order.getEntries()).thenReturn(entries);
        BillRequest request = service.populateBillRequest(order);
        assertEquals(1, request.getBillDetails().size());
    }

    @Test
    public void deleteProductOfComboInReturnBillWithOrder() {
        SubOrderEntryModel subOrderEntryModel = new SubOrderEntryModel();
        subOrderEntryModel.setOrderEntry(new OrderEntryModel());
        service.deleteProductOfComboInReturnBillWithOrder(order, subOrderEntryModel);
        verify(billClient).deleteProductInReturnBillWithOrder(any(OrderBillRequest.class));
    }

    @Test
    public void populateBillRequest_Topping() {
        List<AbstractOrderEntryModel> entries = new ArrayList<>();
        AbstractOrderEntryModel orderEntry = new AbstractOrderEntryModel();
        orderEntry.setProductId(1l);
        orderEntry.setQuantity(2l);
        orderEntry.setBasePrice(20000d);
        orderEntry.setId(2323l);
        ToppingOptionModel toppingOptionModel = new ToppingOptionModel();
        toppingOptionModel.setId(1l);
        toppingOptionModel.setQuantity(2);
        toppingOptionModel.setToppingItemModels(Collections.singleton(toppingItem1));
        orderEntry.setToppingOptionModels(Collections.singleton(toppingOptionModel));
        entries.add(orderEntry);
        when(order.getEntries()).thenReturn(entries);
        BillRequest request = service.populateBillRequest(order);
        assertEquals(2, request.getBillDetails().size());
        for (BillDetailRequest billDetailRequest : request.getBillDetails()) {
            if (billDetailRequest.getOrderEntryId().equals(2323l) && billDetailRequest.getToppingOptionId() == null) {
                assertEquals(2, billDetailRequest.getQuantity(), 0);
                assertEquals(20000d, billDetailRequest.getPrice(), 0);
            } else if (billDetailRequest.getOrderEntryId().equals(2323l)
                    && billDetailRequest.getToppingOptionId().equals(1l)
                    && billDetailRequest.getProductId().equals(1l)) {
                assertEquals(4, billDetailRequest.getQuantity(), 0);
                assertEquals(10000d, billDetailRequest.getPrice(), 0);
            } else {
                fail("Not existed verified");
            }
        }
    }

    @Test
    public void populateBillRequest_Duplicate_Topping() {
        List<AbstractOrderEntryModel> entries = new ArrayList<>();
        AbstractOrderEntryModel orderEntry = new AbstractOrderEntryModel();
        orderEntry.setProductId(1l);
        orderEntry.setQuantity(1l);
        orderEntry.setBasePrice(20000d);
        orderEntry.setId(1111l);
        ToppingOptionModel toppingOptionModel = new ToppingOptionModel();
        toppingOptionModel.setId(1l);
        toppingOptionModel.setQuantity(1);
        Set<ToppingItemModel> toppingItemModels = new HashSet<>();
        toppingItemModels.add(toppingItem1);
        toppingItemModels.add(toppingItem2);
        toppingOptionModel.setToppingItemModels(toppingItemModels);
        orderEntry.setToppingOptionModels(Collections.singleton(toppingOptionModel));
        entries.add(orderEntry);
        when(order.getEntries()).thenReturn(entries);
        BillRequest request = service.populateBillRequest(order);
        assertEquals(3, request.getBillDetails().size());
        for (BillDetailRequest billDetailRequest : request.getBillDetails()) {
            if (billDetailRequest.getOrderEntryId().equals(1111l) && billDetailRequest.getToppingOptionId() == null) {
                assertEquals(1, billDetailRequest.getQuantity(), 0);
                assertEquals(20000d, billDetailRequest.getPrice(), 0);
                assertEquals(1111l, billDetailRequest.getOrderEntryId(), 0);
            } else if (billDetailRequest.getOrderEntryId().equals(1111l) && billDetailRequest.getToppingOptionId().equals(1l)) {
                if(billDetailRequest.getProductId().equals(1l)) {
                    assertEquals(2, billDetailRequest.getQuantity(), 0);
                    assertEquals(10000d, billDetailRequest.getPrice(), 0);
                } else if(billDetailRequest.getProductId().equals(2l)) {
                    assertEquals(2, billDetailRequest.getQuantity(), 0);
                    assertEquals(10000d, billDetailRequest.getPrice(), 0);
                } else {
                    fail("Not exist verified");
                }

            } else {
                fail("Not exist verified");
            }
        }
    }

    @Test
    public void isOrderHasOnlyDynamicComboEntry_HasNormalEntries() {
        when(order.getEntries()).thenReturn(Arrays.asList(normalEntry));

        boolean orderHasOnlyDynamicComboEntry = service.isOrderHasOnlyDynamicComboEntry(order);
        assertEquals(false, orderHasOnlyDynamicComboEntry);
    }

    @Test
    public void isOrderHasOnlyDynamicComboEntry_HasFixedComboEntry() {
        when(comboEntry.getComboType()).thenReturn(ComboType.FIXED_COMBO.toString());
        when(order.getEntries()).thenReturn(Arrays.asList(comboEntry));

        boolean orderHasOnlyDynamicComboEntry = service.isOrderHasOnlyDynamicComboEntry(order);
        assertEquals(false, orderHasOnlyDynamicComboEntry);
    }

    @Test
    public void isOrderHasOnlyDynamicComboEntry_HasNormalAndDynamicCombo() {
        when(comboEntry.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());
        when(order.getEntries()).thenReturn(Arrays.asList(comboEntry, normalEntry));

        boolean orderHasOnlyDynamicComboEntry = service.isOrderHasOnlyDynamicComboEntry(order);
        assertEquals(false, orderHasOnlyDynamicComboEntry);
    }

    @Test
    public void isOrderHasOnlyDynamicComboEntry_HasOnlyDynamicCombo_notAddProductToCombo() {
        when(comboEntry.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());
        when(order.getEntries()).thenReturn(Arrays.asList(comboEntry));

        boolean orderHasOnlyDynamicComboEntry = service.isOrderHasOnlyDynamicComboEntry(order);
        assertEquals(true, orderHasOnlyDynamicComboEntry);
    }

    @Test
    public void isOrderHasOnlyDynamicComboEntry_HasOnlyDynamicCombo_ExistProductInCombo() {
        when(comboEntry.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());
        when(order.getEntries()).thenReturn(Arrays.asList(comboEntry));
        when(comboEntry.getSubOrderEntries()).thenReturn(new HashSet<>(Arrays.asList(new SubOrderEntryModel())));

        boolean orderHasOnlyDynamicComboEntry = service.isOrderHasOnlyDynamicComboEntry(order);
        assertEquals(false, orderHasOnlyDynamicComboEntry);
    }

    @Test
    public void revertOnlineBillWhenError_newStatusIsNotShipping_Case1() {
        service.revertOnlineBillWhenError(OrderStatus.PACKING, OrderStatus.PACKAGED, order);
        verify(billClient, times(0)).revertReturnBillWithOnlineOrder(any(OrderBillRequest.class));
    }

    @Test
    public void revertOnlineBillWhenError_newStatusIsNotShipping_Case2() {
        service.revertOnlineBillWhenError(OrderStatus.NEW, OrderStatus.CONFIRMED, order);
        verify(billClient, times(0)).revertReturnBillWithOnlineOrder(any(OrderBillRequest.class));
    }

    @Test
    public void revertOnlineBillWhenError_newStatusIsNotShipping_Case3() {
        service.revertOnlineBillWhenError(OrderStatus.SHIPPING, OrderStatus.COMPLETED, order);
        verify(billClient, times(0)).revertReturnBillWithOnlineOrder(any(OrderBillRequest.class));
    }

    @Test
    public void revertOnlineBillWhenError_newStatusIsNotShipping_Case4() {
        service.revertOnlineBillWhenError(OrderStatus.SHIPPING, OrderStatus.NEW, order);
        verify(billClient, times(0)).revertReturnBillWithOnlineOrder(any(OrderBillRequest.class));
    }

    @Test
    public void revertOnlineBillWhenError_newStatusIsShipping_OldStatusHigherShipping_Case1() {
        service.revertOnlineBillWhenError(OrderStatus.COMPLETED, OrderStatus.SHIPPING, order);
        verify(billClient, times(0)).revertReturnBillWithOnlineOrder(any(OrderBillRequest.class));
    }

    @Test
    public void revertOnlineBillWhenError_newStatusIsShipping_OldStatusHigherShipping_Case2() {
        service.revertOnlineBillWhenError(OrderStatus.ORDER_RETURN, OrderStatus.SHIPPING, order);
        verify(billClient, times(0)).revertReturnBillWithOnlineOrder(any(OrderBillRequest.class));
    }

    @Test
    public void revertOnlineBillWhenError_newStatusIsShipping_OldStatusHigherShipping_Case3() {
        service.revertOnlineBillWhenError(OrderStatus.RETURNING, OrderStatus.SHIPPING, order);
        verify(billClient, times(0)).revertReturnBillWithOnlineOrder(any(OrderBillRequest.class));
    }

    @Test
    public void revertOnlineBillWhenError_newStatusIsShipping_OldStatusHigherShipping_Case4() {
        service.revertOnlineBillWhenError(OrderStatus.CUSTOMER_CANCEL, OrderStatus.SHIPPING, order);
        verify(billClient, times(0)).revertReturnBillWithOnlineOrder(any(OrderBillRequest.class));
    }

    @Test
    public void revertOnlineBillWhenError_newStatusIsShipping_OldStatusLowerShipping_Case1() {
        service.revertOnlineBillWhenError(OrderStatus.CONFIRMED, OrderStatus.SHIPPING, order);
        verify(billClient, times(1)).revertReturnBillWithOnlineOrder(any(OrderBillRequest.class));
    }

    @Test
    public void revertOnlineBillWhenError_newStatusIsShipping_OldStatusLowerShipping_Case2() {
        service.revertOnlineBillWhenError(OrderStatus.PACKING, OrderStatus.SHIPPING, order);
        verify(billClient, times(1)).revertReturnBillWithOnlineOrder(any(OrderBillRequest.class));
    }

    @Test
    public void revertOnlineBillWhenError_newStatusIsShipping_OldStatusLowerShipping_Case3() {
        service.revertOnlineBillWhenError(OrderStatus.PACKAGED, OrderStatus.SHIPPING, order);
        verify(billClient, times(1)).revertReturnBillWithOnlineOrder(any(OrderBillRequest.class));
    }
}
