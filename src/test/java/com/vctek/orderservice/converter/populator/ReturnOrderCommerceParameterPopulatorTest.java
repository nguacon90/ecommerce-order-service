package com.vctek.orderservice.converter.populator;

import com.vctek.orderservice.dto.ReturnOrderCommerceParameter;
import com.vctek.orderservice.dto.request.PaymentTransactionRequest;
import com.vctek.orderservice.dto.request.ReturnOrderEntryRequest;
import com.vctek.orderservice.dto.request.ReturnOrderRequest;
import com.vctek.orderservice.feignclient.dto.BillDetailRequest;
import com.vctek.orderservice.feignclient.dto.BillRequest;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.service.AuthService;
import com.vctek.orderservice.service.CartService;
import com.vctek.orderservice.service.LoyaltyService;
import com.vctek.orderservice.service.OrderService;
import com.vctek.util.OrderType;
import com.vctek.util.ReceiptDeliveryReason;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReturnOrderCommerceParameterPopulatorTest {
    private ReturnOrderCommerceParameterPopulator populator;

    @Mock
    private CartService cartService;
    @Mock
    private OrderService orderService;
    @Mock
    private AuthService authService;
    @Mock
    private ReturnOrderRequest request;

    private ReturnOrderCommerceParameter param = new ReturnOrderCommerceParameter();
    @Mock
    private OrderModel order;
    @Mock
    private CartModel cart;
    private List<PaymentTransactionRequest> payments = new ArrayList<>();
    @Mock
    private PaymentTransactionRequest payment;
    private List<AbstractOrderEntryModel> entries = new ArrayList<>();
    @Mock
    private OrderEntryModel entry;

    @Mock
    private OrderEntryModel combo;

    private List<ReturnOrderEntryRequest> returnEntries = new ArrayList<>();
    @Mock
    private ReturnOrderEntryRequest returnEntry;

    @Mock
    private ReturnOrderEntryRequest returnEntry2;
    @Mock
    private LoyaltyService loyaltyService;

    private String exchangeLoyaltyCard = "123456";

    private SubOrderEntryModel comboEntry1;
    private SubOrderEntryModel comboEntry2;
    private ArgumentCaptor<String> captor;
    private SubOrderEntryModel subEntry1;
    private SubOrderEntryModel subEntry2;
    private SubOrderEntryModel subEntry3;
    private SubOrderEntryModel subEntry4;
    private SubOrderEntryModel subEntry5;
    private SubOrderEntryModel subEntry6;
    private SubOrderEntryModel subEntry7;
    private SubOrderEntryModel subEntry8;

    private SubOrderEntryModel generateSubEntry(long id, Long productId, int quantity, Double originPrice, Double price, double discount) {
        SubOrderEntryModel model = new SubOrderEntryModel();
        model.setId(id);
        model.setProductId(productId);
        model.setDiscountValue(discount);
        model.setPrice(price);
        model.setOriginPrice(originPrice);
        model.setQuantity(quantity);
        return model;
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        captor = ArgumentCaptor.forClass(String.class);
        populator = new ReturnOrderCommerceParameterPopulator();
        populator.setAuthService(authService);
        populator.setOrderService(orderService);
        populator.setCartService(cartService);
        when(payment.getAmount()).thenReturn(50000d);
        when(payment.getWarehouseId()).thenReturn(1l);
        when(payment.getMoneySourceId()).thenReturn(22l);
        payments.add(payment);

        when(request.getNote()).thenReturn("note");
        when(request.getCompanyId()).thenReturn(1l);
        when(request.getOriginOrderCode()).thenReturn("originOrderCode");
        when(request.getExchangeCartCode()).thenReturn("exchangeCartCode");
        when(request.getPayments()).thenReturn(payments);
        when(request.isExchange()).thenReturn(true);
        when(request.getExchangeLoyaltyCard()).thenReturn(exchangeLoyaltyCard);
        when(request.getExchangePayments()).thenReturn(payments);

        when(returnEntry.getQuantity()).thenReturn(1);
        when(returnEntry.getEntryNumber()).thenReturn(0);
        when(returnEntry.getOrderEntryId()).thenReturn(0l);
        when(returnEntry.getFinalDiscount()).thenReturn(2000d);
        when(returnEntry.getId()).thenReturn(0l);

        when(returnEntry2.getQuantity()).thenReturn(3);
        when(returnEntry2.getEntryNumber()).thenReturn(1);
        when(returnEntry2.getFinalDiscount()).thenReturn(0d);
        when(returnEntry2.getId()).thenReturn(1l);
        when(returnEntry2.getOrderEntryId()).thenReturn(1l);
        returnEntries.add(returnEntry);

        when(request.getReturnOrderEntries()).thenReturn(returnEntries);

        when(order.getWarehouseId()).thenReturn(1l);
        when(order.getCompanyId()).thenReturn(2l);
        when(entry.getProductId()).thenReturn(222l);
        when(entry.getEntryNumber()).thenReturn(0);
        when(entry.getBasePrice()).thenReturn(100000d);
        when(entry.getQuantity()).thenReturn(1l);
        when(entry.getFinalPrice()).thenReturn(100000d - 2000d);
        when(entry.getId()).thenReturn(0l);

        entries.add(entry);

        when(order.getEntries()).thenReturn(entries);
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(order);
        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
        comboEntry1 = new SubOrderEntryModel();
        comboEntry2 = new SubOrderEntryModel();
    }

    @Test
    public void populate() {
        OrderModel orderModel = new OrderModel();
        List<AbstractOrderEntryModel> abstractOrderEntryModels = new ArrayList<>();
        OrderEntryModel orderEntryModel = new OrderEntryModel();
        orderEntryModel.setId(1l);
        orderEntryModel.setBasePrice(100000d);
        abstractOrderEntryModels.add(orderEntryModel);
        orderModel.setEntries(abstractOrderEntryModels);
        when(request.getReturnOrderId()).thenReturn(null);
        when(returnEntry.getOrderEntryId()).thenReturn(1l);
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(),anyLong(),anyBoolean())).thenReturn(orderModel);
        populator.populate(request, param);
        assertNotNull(param.getExchangeCart());
        assertNotNull(param.getOriginOrder());
        assertNotNull(param.getBillRequest());
        assertEquals(1, param.getPaymentTransactions().size());
        assertEquals(50000d, param.getPaymentTransactions().iterator().next().getAmount(), 0);
        assertEquals(50000d, param.getExchangePaymentTransactions().iterator().next().getAmount(), 0);
        BillRequest billRequest = param.getBillRequest();
        assertEquals(100000d - 2000d, billRequest.getFinalCost(), 0);
        verify(cart).setCardNumber(captor.capture());
        assertEquals(exchangeLoyaltyCard, captor.getValue());
    }

    @Test
    public void getReasonBill_onlineOrder() {
        when(order.getType()).thenReturn(OrderType.ONLINE.toString());
        String reasonBill = populator.getReasonBill(order);
        assertEquals(ReceiptDeliveryReason.RECEIPT_RETURN_ONLINE_ORDER_REASON.code(), reasonBill);
    }

    @Test
    public void getReasonBill_retailOrder() {
        when(order.getType()).thenReturn(OrderType.RETAIL.toString());
        String reasonBill = populator.getReasonBill(order);
        assertEquals(ReceiptDeliveryReason.RECEIPT_RETURN_RETAIL_ORDER_REASON.code(), reasonBill);
    }

    @Test
    public void getReasonBill_wholesaleOrder() {
        when(order.getType()).thenReturn(OrderType.WHOLESALE.toString());
        String reasonBill = populator.getReasonBill(order);
        assertEquals(ReceiptDeliveryReason.RECEIPT_RETURN_WHOLESALE_ORDER_REASON.code(), reasonBill);
    }

    @Test
    public void populate_comboEntries_withoutDiscount() {

        SubOrderEntryModel subentry1 = new SubOrderEntryModel();
        subentry1.setProductId(112l);
        subentry1.setPrice(200000d);
        subentry1.setQuantity(5);
        subentry1.setId(122l);
        subentry1.setFinalPrice(1000000d);

        SubOrderEntryModel subEntry2 = new SubOrderEntryModel();
        subEntry2.setProductId(113l);
        subEntry2.setPrice(50000d);
        subEntry2.setQuantity(5);
        subEntry2.setId(121l);
        subEntry2.setFinalPrice(250000d);
        when(returnEntry.getQuantity()).thenReturn(1);
        when(returnEntry.getOrderEntryId()).thenReturn(1l);
        when(returnEntry.getFinalDiscount()).thenReturn(0d);
        OrderModel orderModel = new OrderModel();
        List<AbstractOrderEntryModel> abstractOrderEntryModels = new ArrayList<>();
        OrderEntryModel orderEntryModel = new OrderEntryModel();
        orderEntryModel.setId(1l);
        orderEntryModel.setQuantity(1l);
        orderEntryModel.setBasePrice(250000d);
        orderEntryModel.setProductId(111l);
        abstractOrderEntryModels.add(orderEntryModel);
        orderEntryModel.setSubOrderEntries(new HashSet<>(Arrays.asList(subentry1, subEntry2)));
        orderModel.setEntries(abstractOrderEntryModels);
        when(request.getReturnOrderId()).thenReturn(null);
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(),anyLong(),anyBoolean())).thenReturn(orderModel);
        populator.populate(request, param);

        assertNotNull(param.getExchangeCart());
        assertNotNull(param.getOriginOrder());
        assertNotNull(param.getBillRequest());
        Set<BillDetailRequest> billDetails = param.getBillRequest().getBillDetails();

        assertEquals(2, billDetails.size());
        Optional<BillDetailRequest> entry1 = billDetails.stream().filter(b -> b.getProductId().equals(subentry1.getProductId()))
                .findFirst();
        Optional<BillDetailRequest> entry2 = billDetails.stream().filter(b -> b.getProductId().equals(subEntry2.getProductId()))
                .findFirst();

        assertEquals(1l, entry1.get().getOrderEntryId(), 0);
        assertEquals(122l, entry1.get().getSubOrderEntryId(), 0);
        assertEquals(111l, entry1.get().getComboId(), 0);
        assertEquals(5, entry1.get().getQuantity(), 0);
        assertEquals(200000d, entry1.get().getPrice(), 0);
        assertEquals(1000000d, entry1.get().getFinalPrice(), 0);

        assertEquals(1l, entry2.get().getOrderEntryId(), 0);
        assertEquals(121l, entry2.get().getSubOrderEntryId(), 0);
        assertEquals(111l, entry2.get().getComboId(), 0);
        assertEquals(5l, entry2.get().getQuantity(), 0);
        assertEquals(50000d, entry2.get().getPrice(), 0);
        assertEquals(250000d, entry2.get().getFinalPrice(), 0);
    }

    @Test
    public void populate_comboEntriesWithDiscount() {

        SubOrderEntryModel subentry1 = new SubOrderEntryModel();
        subentry1.setProductId(112l);
        subentry1.setOriginPrice(10000d);
        subentry1.setPrice(40000d);
        subentry1.setDiscountValue(4000d);
        subentry1.setQuantity(3);
        subentry1.setId(122l);
        subentry1.setFinalPrice(39999d);

        SubOrderEntryModel subEntry2 = new SubOrderEntryModel();
        subEntry2.setProductId(113l);
        subEntry2.setOriginPrice(10000d);
        subEntry2.setPrice(40000d);
        subEntry2.setQuantity(3);
        subEntry2.setId(121l);
        subEntry2.setDiscountValue(3333d);
        subEntry2.setFinalPrice(33333d);

        SubOrderEntryModel subEntry3 = new SubOrderEntryModel();
        subEntry3.setProductId(114l);
        subEntry3.setOriginPrice(40000d);
        subEntry3.setPrice(160000d);
        subEntry3.setQuantity(3);
        subEntry3.setId(123l);
        subEntry3.setFinalPrice(16667d);

        OrderModel orderModel = new OrderModel();
        List<AbstractOrderEntryModel> abstractOrderEntryModels = new ArrayList<>();
        OrderEntryModel orderEntryModel = new OrderEntryModel();
        orderEntryModel.setId(1l);
        orderEntryModel.setQuantity(3l);
        orderEntryModel.setBasePrice(240000d);
        orderEntryModel.setProductId(111l);
        when(returnEntry.getQuantity()).thenReturn(1);
        when(returnEntry.getOrderEntryId()).thenReturn(1l);
        when(returnEntry.getFinalDiscount()).thenReturn(8000d);
        abstractOrderEntryModels.add(orderEntryModel);
        orderEntryModel.setSubOrderEntries(new HashSet<>(Arrays.asList(subentry1, subEntry2, subEntry3)));
        orderModel.setEntries(abstractOrderEntryModels);
        when(request.getReturnOrderId()).thenReturn(null);

        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(),anyLong(),anyBoolean())).thenReturn(orderModel);
        populator.populate(request, param);

        assertNotNull(param.getExchangeCart());
        assertNotNull(param.getOriginOrder());
        assertNotNull(param.getBillRequest());
        Set<BillDetailRequest> billDetails = param.getBillRequest().getBillDetails();

        assertEquals(3, billDetails.size());
        Optional<BillDetailRequest> entry1 = billDetails.stream().filter(b -> b.getProductId().equals(subentry1.getProductId()))
                .findFirst();
        Optional<BillDetailRequest> entry2 = billDetails.stream().filter(b -> b.getProductId().equals(subEntry2.getProductId()))
                .findFirst();

        Optional<BillDetailRequest> entry3 = billDetails.stream().filter(b -> b.getProductId().equals(subEntry3.getProductId()))
                .findFirst();

        assertEquals(112, entry1.get().getProductId(), 0);
        assertEquals(1l, entry1.get().getOrderEntryId(), 0);
        assertEquals(122l, entry1.get().getSubOrderEntryId(), 0);
        assertEquals(111l, entry1.get().getComboId(), 0);
        assertEquals(1, entry1.get().getQuantity(), 0);
        assertEquals(40000d, entry1.get().getPrice(), 0);
        assertEquals(1334d, entry1.get().getDiscount(), 0);
        assertEquals(38666d, entry1.get().getFinalPrice(), 0);

        assertEquals(113, entry2.get().getProductId(), 0);
        assertEquals(1l, entry2.get().getOrderEntryId(), 0);
        assertEquals(121l, entry2.get().getSubOrderEntryId(), 0);
        assertEquals(111l, entry2.get().getComboId(), 0);
        assertEquals(1l, entry2.get().getQuantity(), 0);
        assertEquals(40000d, entry2.get().getPrice(), 0);
        assertEquals(1333d, entry2.get().getDiscount(), 0);
        assertEquals(38667, entry2.get().getFinalPrice(), 0);

        assertEquals(114, entry3.get().getProductId(), 0);
        assertEquals(1l, entry3.get().getOrderEntryId(), 0);
        assertEquals(123l, entry3.get().getSubOrderEntryId(), 0);
        assertEquals(111l, entry3.get().getComboId(), 0);
        assertEquals(1l, entry3.get().getQuantity(), 0);
        assertEquals(160000d, entry3.get().getPrice(), 0);
        assertEquals(5333, entry3.get().getDiscount(), 0);
        assertEquals(154667, entry3.get().getFinalPrice(), 0);
    }

    @Test
    public void populateComboEntries() {
        subEntry1 = generateSubEntry(1l, 13646233l, 2, 20000d, 70423d, 14085);
        subEntry2 = generateSubEntry(2, 13646339l, 2, 20000d, 70423d, 14082);
        subEntry3 = generateSubEntry(3, 13646215l, 2, 20000d, 70423d, 14085);
        subEntry4 = generateSubEntry(4, 13646275l, 2, 4500d, 15845d, 3169);
        subEntry5 = generateSubEntry(5, 13643698l, 2, 17500d, 61620d, 12324);
        subEntry6 = generateSubEntry(6, 13646150l, 2, 20000d, 70423d, 14085);
        subEntry7 = generateSubEntry(7, 13646150l, 2, 20000d, 70423d, 14085);
        subEntry8 = generateSubEntry(8, 13646241l, 2, 20000d, 70423d, 14085);
        when(entry.getSubOrderEntries()).thenReturn(new HashSet<>(Arrays.asList(subEntry1,subEntry2, subEntry3, subEntry4, subEntry5, subEntry6, subEntry7, subEntry8)));
        when(returnEntry.getFinalDiscount()).thenReturn(100000d);
        when(returnEntry.getPrice()).thenReturn(500000d);
        when(returnEntry.getQuantity()).thenReturn(2);
        when(entry.getQuantity()).thenReturn(2l);
        when(entry.getBasePrice()).thenReturn(500000d);
        List<BillDetailRequest> billDetailRequests = populator.populateComboEntries(returnEntry, entry);
        assertEquals(8, billDetailRequests.size());
        BillDetailRequest request1 = billDetailRequests.stream().filter(bd -> bd.getSubOrderEntryId().equals(1l)).findFirst().get();
        BillDetailRequest request2 = billDetailRequests.stream().filter(bd -> bd.getSubOrderEntryId().equals(2l)).findFirst().get();
        BillDetailRequest request3 = billDetailRequests.stream().filter(bd -> bd.getSubOrderEntryId().equals(3l)).findFirst().get();
        BillDetailRequest request4 = billDetailRequests.stream().filter(bd -> bd.getSubOrderEntryId().equals(4l)).findFirst().get();
        BillDetailRequest request5 = billDetailRequests.stream().filter(bd -> bd.getSubOrderEntryId().equals(5l)).findFirst().get();
        BillDetailRequest request6 = billDetailRequests.stream().filter(bd -> bd.getSubOrderEntryId().equals(6l)).findFirst().get();
        BillDetailRequest request7 = billDetailRequests.stream().filter(bd -> bd.getSubOrderEntryId().equals(7l)).findFirst().get();
        BillDetailRequest request8 = billDetailRequests.stream().filter(bd -> bd.getSubOrderEntryId().equals(8l)).findFirst().get();
        assertEquals(14085, request1.getDiscount(), 0);
        assertEquals(14085, request2.getDiscount(), 0);
        assertEquals(14085, request3.getDiscount(), 0);
        assertEquals(3166, request4.getDiscount(), 0);
        assertEquals(12324, request5.getDiscount(), 0);
        assertEquals(14085, request6.getDiscount(), 0);
        assertEquals(14085, request7.getDiscount(), 0);
        assertEquals(14085, request8.getDiscount(), 0);
    }
}
