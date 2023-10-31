package com.vctek.orderservice.service.impl;

import com.vctek.kafka.data.loyalty.LoyaltyInvoiceData;
import com.vctek.orderservice.dto.PaymentMethodData;
import com.vctek.orderservice.event.ReturnOrderEvent;
import com.vctek.orderservice.event.ReturnOrderEventType;
import com.vctek.orderservice.feignclient.dto.BillRequest;
import com.vctek.orderservice.feignclient.dto.CustomerData;
import com.vctek.orderservice.feignclient.dto.InvoiceOrderData;
import com.vctek.orderservice.feignclient.dto.InvoiceRequest;
import com.vctek.orderservice.kafka.producer.LoyaltyInvoiceProducerService;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.PaymentTransactionModel;
import com.vctek.orderservice.model.ReturnOrderModel;
import com.vctek.orderservice.service.FinanceService;
import com.vctek.orderservice.service.PaymentTransactionService;
import com.vctek.util.*;
import org.apache.commons.collections.map.HashedMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

public class InvoiceServiceTest {
    private Long REDEEM_PAYMENT_ID = 12l;
    @Mock
    private FinanceService financeService;
    private InvoiceServiceImpl invoiceService;
    private OrderModel order = new OrderModel();
    private CustomerData customer = new CustomerData();
    private Set<PaymentTransactionModel> payments = new HashSet<>();
    private ReturnOrderEvent returnOrderEvent;

    @Mock
    private ReturnOrderModel returnOrder;
    @Mock
    private BillRequest billRequest;
    @Mock
    private OrderModel exchangeOrder;
    @Mock
    private PaymentTransactionService paymentTransactionService;
    @Mock
    private PaymentTransactionModel bidvMock;
    @Mock
    private PaymentTransactionModel cashMock;
    @Mock
    private PaymentTransactionModel paymentModel;
    @Mock
    private PaymentTransactionModel paymentRefund;
    @Mock
    private PaymentMethodData paymentMethodData;
    @Mock
    private LoyaltyInvoiceProducerService loyaltyInvoiceProducerService;

    private List<PaymentTransactionModel> paymentModels = new ArrayList<>();
    private ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    private ArgumentCaptor<List<InvoiceRequest>> invoiceCaptor = ArgumentCaptor.forClass(List.class);
    private ArgumentCaptor<List<Long>> captorInvoiceIds = ArgumentCaptor.forClass(List.class);
    ;
    @Mock
    private OrderModel originOrder;
    @Mock
    private InvoiceOrderData invoiceOrderData;
    private Map<Long, Long> invoiceMap = new HashMap<>();
    private Long CASH_ID = 111l;
    private Long BIDV_ID = 112l;
    @Mock
    private PaymentTransactionModel redeemMock;
    private Long LOYALTY_PAYMENT_METHOD_ID = 222222l;
    @Mock
    private PaymentMethodData paymentDataMock;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(cashMock.getMoneySourceType()).thenReturn(MoneySourceType.CASH.toString());
        when(bidvMock.getMoneySourceType()).thenReturn(MoneySourceType.BANK_ACCOUNT.toString());
        invoiceService = new InvoiceServiceImpl();
        invoiceService.setPaymentTransactionService(paymentTransactionService);
        invoiceService.setFinanceService(financeService);
        invoiceService.setLoyaltyInvoiceProducerService(loyaltyInvoiceProducerService);

        order.setPaymentTransactions(payments);
        order.setFinalPrice(500000d);
        returnOrderEvent = new ReturnOrderEvent(returnOrder, ReturnOrderEventType.CREATE);
        returnOrderEvent.setBillRequest(billRequest);
        when(returnOrder.getExchangeOrder()).thenReturn(exchangeOrder);
        when(returnOrder.getOriginOrder()).thenReturn(originOrder);
        when(paymentModel.getId()).thenReturn(22l);
        when(cashMock.getMoneySourceId()).thenReturn(CASH_ID);
        when(bidvMock.getMoneySourceId()).thenReturn(BIDV_ID);
        paymentModels.add(paymentModel);
        invoiceMap.put(22l, 23l);
        invoiceMap.put(REDEEM_PAYMENT_ID, 233l);
        when(redeemMock.getId()).thenReturn(REDEEM_PAYMENT_ID);
        when(redeemMock.getPaymentMethodId()).thenReturn(LOYALTY_PAYMENT_METHOD_ID);
        when(redeemMock.getMoneySourceType()).thenReturn(MoneySourceType.BANK_ACCOUNT.toString());
        when(financeService.getPaymentMethodByCode(PaymentMethodType.LOYALTY_POINT.code())).thenReturn(paymentDataMock);
        when(paymentDataMock.getId()).thenReturn(LOYALTY_PAYMENT_METHOD_ID);
    }

    @Test
    public void createInvoicesFor_OrderWithEmptyTransactionsShouldIgnore() {
        invoiceService.saveInvoices(order, 1l);
        verify(financeService, times(0)).createInvoiceOrder(anyList());
    }

    @Test
    public void createInvoice_OnlyCashPaymentMethod_AmountLargerThanPayment() {
        order.setPaymentTransactions(new HashSet<>(Arrays.asList(cashMock)));
        order.setFinalPrice(400000d);
        when(cashMock.getAmount()).thenReturn(600000d);

        when(financeService.createInvoiceOrder(anyList())).thenReturn(invoiceOrderData);
        when(invoiceOrderData.getInvoicePaymentMap()).thenReturn(invoiceMap);
        invoiceService.saveInvoices(order, 1l);
        verify(financeService, times(1)).createInvoiceOrder(invoiceCaptor.capture());
        List<InvoiceRequest> invoiceRequests = invoiceCaptor.getValue();
        assertEquals(1, invoiceRequests.size());
        assertEquals(400000d, invoiceRequests.get(0).getAmount(), 0);
        assertEquals(CASH_ID, invoiceRequests.get(0).getMoneySourceId(), 0);
    }


    @Test
    public void createInvoice_OnlyCashPaymentMethod_OrderOnline() {
        order.setPaymentTransactions(new HashSet<>(Arrays.asList(cashMock)));
        order.setFinalPrice(400000d);
        when(cashMock.getAmount()).thenReturn(600000d);
        order.setType(OrderType.ONLINE.toString());
        when(financeService.createInvoiceOrder(anyList())).thenReturn(invoiceOrderData);
        when(invoiceOrderData.getInvoicePaymentMap()).thenReturn(invoiceMap);
        invoiceService.saveInvoices(order, 1l);
        verify(financeService, times(1)).createInvoiceOrder(invoiceCaptor.capture());
        List<InvoiceRequest> invoiceRequests = invoiceCaptor.getValue();
        assertEquals(1, invoiceRequests.size());
        assertEquals(600000d, invoiceRequests.get(0).getAmount(), 0);
        assertEquals(CASH_ID, invoiceRequests.get(0).getMoneySourceId(), 0);
    }

    @Test
    public void createInvoice_OnlyCashPaymentMethod_Wholesale() {
        order.setPaymentTransactions(new HashSet<>(Arrays.asList(cashMock)));
        order.setFinalPrice(400000d);
        when(cashMock.getAmount()).thenReturn(600000d);
        order.setType(OrderType.WHOLESALE.toString());
        when(financeService.createInvoiceOrder(anyList())).thenReturn(invoiceOrderData);
        when(invoiceOrderData.getInvoicePaymentMap()).thenReturn(invoiceMap);
        invoiceService.saveInvoices(order, 1l);
        verify(financeService, times(1)).createInvoiceOrder(invoiceCaptor.capture());
        List<InvoiceRequest> invoiceRequests = invoiceCaptor.getValue();
        assertEquals(1, invoiceRequests.size());
        assertEquals(600000d, invoiceRequests.get(0).getAmount(), 0);
        assertEquals(CASH_ID, invoiceRequests.get(0).getMoneySourceId(), 0);
    }

    @Test
    public void createInvoice_OnlyCashPaymentMethod_AmountEqualPayment() {
        order.setPaymentTransactions(new HashSet<>(Arrays.asList(cashMock)));
        order.setFinalPrice(600000d);
        when(cashMock.getAmount()).thenReturn(600000d);

        when(financeService.createInvoiceOrder(anyList())).thenReturn(invoiceOrderData);
        when(invoiceOrderData.getInvoicePaymentMap()).thenReturn(invoiceMap);
        invoiceService.saveInvoices(order, 1l);
        verify(financeService, times(1)).createInvoiceOrder(invoiceCaptor.capture());
        List<InvoiceRequest> invoiceRequests = invoiceCaptor.getValue();
        assertEquals(1, invoiceRequests.size());
        assertEquals(600000d, invoiceRequests.get(0).getAmount(), 0);
        assertEquals(CASH_ID, invoiceRequests.get(0).getMoneySourceId(), 0);
    }

    @Test
    public void createInvoice_ManyPaymentMethod_BankAmountEqualsToOrderFinalPrice_HasCashBefore_ShouldUpdateCashToZero() {
        order.setPaymentTransactions(new HashSet<>(Arrays.asList(bidvMock, cashMock)));
        order.setFinalPrice(600000d);
        when(bidvMock.getAmount()).thenReturn(600000d);

        when(financeService.createInvoiceOrder(anyList())).thenReturn(invoiceOrderData);
        when(invoiceOrderData.getInvoicePaymentMap()).thenReturn(invoiceMap);
        invoiceService.saveInvoices(order, 1l);
        verify(financeService, times(1)).createInvoiceOrder(invoiceCaptor.capture());
        List<InvoiceRequest> invoiceRequests = invoiceCaptor.getValue();
        assertEquals(2, invoiceRequests.size());
        assertEquals(600000d, invoiceRequests.get(0).getAmount(), 0);
        assertEquals(BIDV_ID, invoiceRequests.get(0).getMoneySourceId(), 0);
        assertEquals(0, invoiceRequests.get(1).getAmount(), 0);
        assertEquals(CASH_ID, invoiceRequests.get(1).getMoneySourceId(), 0);
    }

    @Test
    public void createInvoice_ManyPaymentMethod_BankAmountSmallerThanOrderFinalPrice() {
        order.setPaymentTransactions(new HashSet<>(Arrays.asList(bidvMock, cashMock)));
        order.setFinalPrice(600000d);
        when(bidvMock.getAmount()).thenReturn(400000d);

        when(financeService.createInvoiceOrder(anyList())).thenReturn(invoiceOrderData);
        when(invoiceOrderData.getInvoicePaymentMap()).thenReturn(invoiceMap);
        invoiceService.saveInvoices(order, 1l);
        verify(financeService, times(1)).createInvoiceOrder(invoiceCaptor.capture());
        List<InvoiceRequest> invoiceRequests = invoiceCaptor.getValue();
        assertEquals(2, invoiceRequests.size());
        assertEquals(400000d, invoiceRequests.get(0).getAmount(), 0);
        assertEquals(BIDV_ID, invoiceRequests.get(0).getMoneySourceId(), 0);

        assertEquals(200000d, invoiceRequests.get(1).getAmount(), 0);
        assertEquals(CASH_ID, invoiceRequests.get(1).getMoneySourceId(), 0);
    }

    @Test
    public void createInvoice_ManyPaymentMethod_BankAmountLargerThanOrderFinalPrice_HasNotCashBefore() {
        order.setPaymentTransactions(new HashSet<>(Arrays.asList(bidvMock)));
        order.setFinalPrice(600000d);
        when(bidvMock.getAmount()).thenReturn(1000000d);

        when(financeService.createInvoiceOrder(anyList())).thenReturn(invoiceOrderData);
        when(invoiceOrderData.getInvoicePaymentMap()).thenReturn(invoiceMap);
        invoiceService.saveInvoices(order, 1l);
        verify(financeService, times(1)).createInvoiceOrder(invoiceCaptor.capture());
        List<InvoiceRequest> invoiceRequests = invoiceCaptor.getValue();
        assertEquals(1, invoiceRequests.size());
        assertEquals(1000000d, invoiceRequests.get(0).getAmount(), 0);
        assertEquals(BIDV_ID, invoiceRequests.get(0).getMoneySourceId(), 0);
    }

    @Test
    public void createInvoiceForReturnOrder_ImbursementInvoice() {
        when(billRequest.getFinalCost()).thenReturn(10000d);
        when(returnOrder.getExchangeOrder()).thenReturn(null);
        when(returnOrder.getRefundAmount()).thenReturn(null);
        when(returnOrder.getCompensateRevert()).thenReturn(null);
        when(paymentTransactionService.findAllByReturnOrder(returnOrder)).thenReturn(paymentModels);
        when(paymentModel.getTransactionDate()).thenReturn(null);
        when(financeService.createInvoiceReturnOrder(anyList(), anyString())).thenReturn(invoiceOrderData);
        when(invoiceOrderData.getInvoicePaymentMap()).thenReturn(invoiceMap);
        when(financeService.getPaymentMethodByCode(PaymentMethodType.LOYALTY_POINT.code())).thenReturn(paymentMethodData);
        when(paymentMethodData.getId()).thenReturn(1L);
        invoiceService.createInvoiceForReturnOrder(returnOrderEvent);
        verify(financeService).createInvoiceReturnOrder(anyList(), captor.capture());
        verify(originOrder).getCompanyId();
        verify(originOrder).getCustomerId();
        verify(paymentModel).getAmount();
        verify(paymentModel).getMoneySourceId();
        verify(paymentModel).getWarehouseId();
        verify(paymentModel).getCreatedTime();
        verify(returnOrder).getCreatedBy();
        verify(returnOrder, times(2)).getId();
        verify(paymentTransactionService, times(1)).saveAll(anyList());

        assertEquals(InvoiceType.IMBURSEMENT.toString(), captor.getValue());
    }

    @Test
    public void createInvoiceForReturnOrder_ReceiptInvoice() {
        when(billRequest.getFinalCost()).thenReturn(10000d);
        when(returnOrder.getExchangeOrder()).thenReturn(exchangeOrder);
        when(exchangeOrder.getFinalPrice()).thenReturn(20000000d);

        when(paymentTransactionService.findAllByReturnOrder(returnOrder)).thenReturn(paymentModels);
        when(paymentModel.getTransactionDate()).thenReturn(Calendar.getInstance().getTime());
        when(financeService.createInvoiceReturnOrder(anyList(), anyString())).thenReturn(invoiceOrderData);
        when(invoiceOrderData.getInvoicePaymentMap()).thenReturn(invoiceMap);
        when(financeService.getPaymentMethodByCode(PaymentMethodType.LOYALTY_POINT.code())).thenReturn(paymentMethodData);
        when(paymentMethodData.getId()).thenReturn(1L);
        invoiceService.createInvoiceForReturnOrder(returnOrderEvent);
        verify(financeService).createInvoiceReturnOrder(anyList(), captor.capture());
        verify(originOrder).getCompanyId();
        verify(originOrder).getCustomerId();
        verify(paymentModel).getAmount();
        verify(paymentModel).getMoneySourceId();
        verify(paymentModel).getWarehouseId();
        verify(paymentModel, times(0)).getCreatedTime();
        verify(returnOrder).getCreatedBy();
        verify(returnOrder, times(2)).getId();
        verify(paymentTransactionService, times(1)).saveAll(anyList());

        assertEquals(InvoiceType.RECEIPT.toString(), captor.getValue());
    }

    @Test
    public void createInvoiceForReturnOrder_ImbursementInvoiceHasRefund() {
        when(billRequest.getFinalCost()).thenReturn(10000d);
        when(returnOrder.getExchangeOrder()).thenReturn(null);
        when(returnOrder.getRefundAmount()).thenReturn(1200d);
        when(returnOrder.getCompensateRevert()).thenReturn(1100d);
        when(returnOrder.getConversionRate()).thenReturn(1000d);
        paymentModels.add(paymentRefund);
        when(paymentTransactionService.findAllByReturnOrder(returnOrder)).thenReturn(paymentModels);
        when(paymentModel.getTransactionDate()).thenReturn(null);
        when(paymentRefund.getPaymentMethodId()).thenReturn(1L);
        when(financeService.createInvoiceReturnOrder(anyList(), anyString())).thenReturn(invoiceOrderData);
        when(invoiceOrderData.getInvoicePaymentMap()).thenReturn(invoiceMap);
        when(financeService.getPaymentMethodByCode(PaymentMethodType.LOYALTY_POINT.code())).thenReturn(paymentMethodData);
        when(paymentMethodData.getId()).thenReturn(1L);
        invoiceService.createInvoiceForReturnOrder(returnOrderEvent);
        verify(financeService, times(2)).createInvoiceReturnOrder(anyList(), eq(InvoiceType.IMBURSEMENT.toString()));
        verify(originOrder, times(2)).getCompanyId();
        verify(originOrder, times(2)).getCustomerId();
        verify(paymentModel).getAmount();
        verify(paymentModel).getMoneySourceId();
        verify(paymentModel).getWarehouseId();
        verify(paymentModel).getCreatedTime();
        verify(paymentRefund).getAmount();
        verify(paymentRefund).getMoneySourceId();
        verify(paymentRefund).getWarehouseId();
        verify(paymentRefund).getCreatedTime();
        verify(paymentRefund).setConversionRate(1000d);
        verify(returnOrder, times(2)).getCreatedBy();
        verify(returnOrder, times(3)).getId();
        verify(paymentTransactionService, times(2)).saveAll(anyList());

    }

    @Test
    public void createInvoiceForReturnOrder_ReceiptInvoice_HasRefund() {
        when(billRequest.getFinalCost()).thenReturn(10000d);
        when(returnOrder.getExchangeOrder()).thenReturn(null);
        when(returnOrder.getRefundAmount()).thenReturn(1200d);
        when(returnOrder.getCompensateRevert()).thenReturn(10000d);
        when(returnOrder.getConversionRate()).thenReturn(1000d);
        paymentModels.add(paymentRefund);
        when(paymentTransactionService.findAllByReturnOrder(returnOrder)).thenReturn(paymentModels);
        when(paymentModel.getTransactionDate()).thenReturn(null);
        when(paymentRefund.getPaymentMethodId()).thenReturn(1L);
        when(financeService.createInvoiceReturnOrder(anyList(), anyString())).thenReturn(invoiceOrderData);
        when(invoiceOrderData.getInvoicePaymentMap()).thenReturn(invoiceMap);
        when(financeService.getPaymentMethodByCode(PaymentMethodType.LOYALTY_POINT.code())).thenReturn(paymentMethodData);
        when(paymentMethodData.getId()).thenReturn(1L);
        invoiceService.createInvoiceForReturnOrder(returnOrderEvent);
        verify(financeService, times(1)).createInvoiceReturnOrder(anyList(), eq(InvoiceType.RECEIPT.toString()));
        verify(financeService, times(1)).createInvoiceReturnOrder(anyList(), eq(InvoiceType.IMBURSEMENT.toString()));
        verify(originOrder, times(2)).getCompanyId();
        verify(originOrder, times(2)).getCustomerId();
        verify(paymentModel).getAmount();
        verify(paymentModel).getMoneySourceId();
        verify(paymentModel).getWarehouseId();
        verify(paymentModel).getCreatedTime();
        verify(paymentRefund).getAmount();
        verify(paymentRefund).getMoneySourceId();
        verify(paymentRefund).getWarehouseId();
        verify(paymentRefund).getCreatedTime();
        verify(paymentRefund).setConversionRate(1000d);
        verify(returnOrder, times(2)).getCreatedBy();
        verify(returnOrder, times(3)).getId();
        verify(paymentTransactionService, times(2)).saveAll(anyList());
    }


    @Test
    public void createInvoiceForReturnOrder_HasNotCreateInvoice() {
        when(billRequest.getFinalCost()).thenReturn(10000d);
        when(returnOrder.getExchangeOrder()).thenReturn(exchangeOrder);
        when(exchangeOrder.getFinalPrice()).thenReturn(5000d);
        when(returnOrder.getRefundAmount()).thenReturn(null);
        when(returnOrder.getCompensateRevert()).thenReturn(5000d);
        when(paymentTransactionService.findAllByReturnOrder(returnOrder)).thenReturn(paymentModels);
        when(paymentModel.getTransactionDate()).thenReturn(null);

        when(invoiceOrderData.getInvoicePaymentMap()).thenReturn(invoiceMap);
        when(financeService.getPaymentMethodByCode(PaymentMethodType.LOYALTY_POINT.code())).thenReturn(paymentMethodData);
        when(paymentMethodData.getId()).thenReturn(1L);
        invoiceService.createInvoiceForReturnOrder(returnOrderEvent);
        verify(financeService, times(0)).createInvoiceReturnOrder(anyList(), anyString());
        verify(originOrder, times(0)).getCompanyId();
        verify(originOrder, times(0)).getCustomerId();
        verify(paymentModel, times(0)).getAmount();
        verify(paymentModel, times(0)).getMoneySourceId();
        verify(paymentModel, times(0)).getWarehouseId();
        verify(paymentModel, times(0)).getCreatedTime();
        verify(returnOrder, times(0)).getCreatedBy();
        verify(returnOrder, times(1)).getId();
        verify(paymentTransactionService, times(0)).save(any());

    }

    @Test
    public void unverifyInvoices() {
        OrderModel orderModel = new OrderModel();
        orderModel.setCode("test");
        PaymentTransactionModel paymentTransactionModel = new PaymentTransactionModel();
        paymentTransactionModel.setInvoiceId(1l);
        payments.add(paymentTransactionModel);
        orderModel.setPaymentTransactions(payments);
        invoiceService.unverifyInvoices(orderModel);
        verify(financeService).unverifyInvoiceOrder(captorInvoiceIds.capture(), captor.capture());
        List<Long> invoiceIds = captorInvoiceIds.getValue();
        assertEquals(1, invoiceIds.size());
        assertEquals(1, invoiceIds.get(0), 0);
        assertEquals("test", captor.getValue());
    }

    @Test
    public void updateRefundInvoice() {
        OrderModel orderModel = new OrderModel();
        orderModel.setCode("test");
        PaymentTransactionModel paymentTransactionModel = new PaymentTransactionModel();
        paymentTransactionModel.setInvoiceId(1L);
        paymentTransactionModel.setPaymentMethodId(1L);
        payments.add(paymentTransactionModel);
        orderModel.setPaymentTransactions(payments);

        ReturnOrderModel returnOrderModel = new ReturnOrderModel();
        returnOrderModel.setId(13L);
        returnOrderModel.setOriginOrder(orderModel);
        returnOrderModel.setPaymentTransactions(payments);
        returnOrderModel.setRefundAmount(10000d);
        PaymentMethodData paymentMethodData = new PaymentMethodData();
        paymentMethodData.setId(1L);
        when(financeService.getPaymentMethodByCode(anyString())).thenReturn(paymentMethodData);
        InvoiceOrderData invoiceOrderData = new InvoiceOrderData();
        Map<Long, Long> invoicePaymentMap = new HashedMap();
        invoicePaymentMap.put(1L, 1L);
        invoiceOrderData.setInvoicePaymentMap(invoicePaymentMap);
        when(financeService.createInvoiceReturnOrder(anyList(), anyString())).thenReturn(invoiceOrderData);

        invoiceService.updateRefundInvoice(returnOrderModel);
        verify(financeService).createInvoiceReturnOrder(anyList(), anyString());
        verify(paymentTransactionService, times(1)).saveAll(anyList());
    }

    @Test
    public void findAllOrderInvoices() {
        invoiceService.findAllOrderInvoices(anyLong(), anyString(), anyLong(), anyString());
        verify(financeService).findAllOrderInvoices(anyLong(), anyString(), anyLong(), anyString());
    }

    @Test
    public void ignoreRedeemPaymentMethodForOnline() {
        when(redeemMock.getAmount()).thenReturn(3000d);
        order.setPaymentTransactions(new HashSet<>(Arrays.asList(cashMock, redeemMock)));
        order.setFinalPrice(400000d);
        when(cashMock.getAmount()).thenReturn(600000d);
        order.setType(OrderType.ONLINE.toString());
        when(financeService.createInvoiceOrder(anyList())).thenReturn(invoiceOrderData);
        when(invoiceOrderData.getInvoicePaymentMap()).thenReturn(invoiceMap);
        invoiceService.saveInvoices(order, 1l);
        verify(financeService, times(1)).createInvoiceOrder(invoiceCaptor.capture());
        List<InvoiceRequest> invoiceRequests = invoiceCaptor.getValue();
        assertEquals(1, invoiceRequests.size());
        assertEquals(600000d, invoiceRequests.get(0).getAmount(), 0);
        assertEquals(CASH_ID, invoiceRequests.get(0).getMoneySourceId(), 0);
    }

    @Test
    public void saveRedeemLoyaltyForOnlineOrChangeToRetailOrder_orderNotOnline_AndChangeToRetailStatus() {
        order.setType(OrderType.RETAIL.toString());
        order.setOrderStatus(OrderStatus.COMPLETED.code());
        invoiceService.saveRedeemLoyaltyForOnlineOrChangeToRetailOrder(order);
        verify(financeService, times(0)).createInvoiceOrder(anyList());
        verify(paymentTransactionService, times(0)).saveAll(anyList());
    }

    @Test
    public void saveRedeemLoyaltyForOnlineOrChangeToRetailOrder_OnlineOrder_hasNotPaymentTransactions() {
        order.setType(OrderType.ONLINE.toString());
        order.setPaymentTransactions(new HashSet<>());
        invoiceService.saveRedeemLoyaltyForOnlineOrChangeToRetailOrder(order);
        verify(financeService, times(0)).createInvoiceOrder(anyList());
        verify(paymentTransactionService, times(0)).saveAll(anyList());
    }

    @Test
    public void cancelLoyaltyRedeemInvoice_OrderHasNotRedeem() {
        when(paymentTransactionService.findLoyaltyRedeem(order)).thenReturn(null);

        invoiceService.cancelLoyaltyRedeemInvoice(order);
        verify(loyaltyInvoiceProducerService, times(0)).produceCancelLoyaltyRedeemInvoice(any(LoyaltyInvoiceData.class));
        verify(paymentTransactionService, times(0)).save(any(PaymentTransactionModel.class));
    }

    @Test
    public void cancelLoyaltyRedeemInvoice_RedeemTransactionHasNotInvoiceId() {
        when(paymentTransactionService.findLoyaltyRedeem(order)).thenReturn(paymentModel);
        when(paymentModel.getInvoiceId()).thenReturn(null);

        invoiceService.cancelLoyaltyRedeemInvoice(order);
        verify(loyaltyInvoiceProducerService, times(0)).produceCancelLoyaltyRedeemInvoice(any(LoyaltyInvoiceData.class));
        verify(paymentTransactionService, times(0)).save(any(PaymentTransactionModel.class));
    }

    @Test
    public void cancelLoyaltyRedeemInvoice_RedeemTransactionHasInvoiceId() {
        when(paymentTransactionService.findLoyaltyRedeem(order)).thenReturn(paymentModel);
        when(paymentModel.getInvoiceId()).thenReturn(1119l);

        invoiceService.cancelLoyaltyRedeemInvoice(order);
        verify(paymentModel).setDeleted(true);
        verify(paymentTransactionService, times(1)).save(any(PaymentTransactionModel.class));
        verify(loyaltyInvoiceProducerService, times(1)).produceCancelLoyaltyRedeemInvoice(any(LoyaltyInvoiceData.class));
    }

    @Test
    public void cancelLoyaltyRewardInvoice() {
        invoiceService.cancelLoyaltyRewardInvoice(order);
        verify(loyaltyInvoiceProducerService, times(1)).produceCancelLoyaltyRewardInvoice(any(LoyaltyInvoiceData.class));
    }
}
