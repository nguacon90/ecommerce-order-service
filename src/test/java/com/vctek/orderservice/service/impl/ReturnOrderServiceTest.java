package com.vctek.orderservice.service.impl;

import com.vctek.exception.ServiceException;
import com.vctek.kafka.data.loyalty.TransactionData;
import com.vctek.orderservice.dto.CommerceCheckoutParameter;
import com.vctek.orderservice.dto.CommerceOrderResult;
import com.vctek.orderservice.dto.ReturnOrderCommerceParameter;
import com.vctek.orderservice.dto.ReturnRewardRedeemData;
import com.vctek.orderservice.dto.request.ReturnOrderEntryRequest;
import com.vctek.orderservice.dto.request.ReturnOrderRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.dto.BillRequest;
import com.vctek.orderservice.feignclient.dto.LoyaltyCardData;
import com.vctek.orderservice.kafka.producer.LoyaltyInvoiceProducerService;
import com.vctek.orderservice.kafka.producer.ReturnOrdersProducerService;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.repository.ReturnOrderRepository;
import com.vctek.orderservice.service.*;
import com.vctek.orderservice.service.event.OrderEvent;
import com.vctek.orderservice.strategy.impl.DefaultCommercePlaceOrderStrategy;
import com.vctek.util.OrderStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ReturnOrderServiceTest {
    private ReturnOrderServiceImpl service;

    @Mock
    private ReturnOrderRepository returnOrderRepository;
    @Mock
    private DefaultCommercePlaceOrderStrategy defaultCommercePlaceOrderStrategy;
    @Mock
    private BillService billService;
    @Mock
    private ReturnOrderCommerceParameter parameter;
    @Mock
    private OrderModel originOrder;
    @Mock
    private CartModel exchangeCart;
    private Set<PaymentTransactionModel> payments = new HashSet<>();
    @Mock
    private PaymentTransactionModel payment;
    @Mock
    private ReturnOrderModel returnOrder;
    @Mock
    private BillRequest billRequest;
    @Mock
    private CommerceOrderResult commerceOrderResult;
    @Mock
    private OrderModel exchangeOrder;
    @Mock
    private ReturnOrdersProducerService returnOrdersProducerService;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private CalculationService calculationService;
    @Mock
    private LoyaltyTransactionService loyaltyTransactionService;
    @Mock
    private LoyaltyService loyaltyService;
    @Mock
    private LoyaltyCardData cardData;
    @Mock
    private OrderService orderService;
    @Mock
    private ReturnOrderRequest requestMock;
    @Mock
    private ReturnOrderEntryRequest entryRequest1;
    @Mock
    private TransactionData transactionData;
    @Mock
    private TransactionData transactionData1;
    @Mock
    private LoyaltyInvoiceProducerService loyaltyInvoiceProducerService;
    private ArgumentCaptor<CommerceCheckoutParameter> commerceParameterArgumentCaptor;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new ReturnOrderServiceImpl();
        service.setBillService(billService);
        service.setDefaultCommercePlaceOrderStrategy(defaultCommercePlaceOrderStrategy);
        service.setReturnOrderRepository(returnOrderRepository);
        service.setReturnOrdersProducerService(returnOrdersProducerService);
        service.setApplicationEventPublisher(applicationEventPublisher);
        service.setCalculationService(calculationService);
        service.setLoyaltyService(loyaltyService);
        service.setLoyaltyTransactionService(loyaltyTransactionService);
        service.setOrderService(orderService);
        service.setLoyaltyInvoiceProducerService(loyaltyInvoiceProducerService);
        payments.add(payment);
        commerceParameterArgumentCaptor = ArgumentCaptor.forClass(CommerceCheckoutParameter.class);
        when(parameter.getOriginOrder()).thenReturn(originOrder);
        when(parameter.getPaymentTransactions()).thenReturn(payments);
        when(returnOrder.getId()).thenReturn(1l);
        when(parameter.getBillRequest()).thenReturn(billRequest);
        when(parameter.getReturnOrderRequest()).thenReturn(requestMock);
        when(commerceOrderResult.getOrderModel()).thenReturn(exchangeOrder);
    }

    @Test
    public void create_notExchange() {
        OrderModel originOrderModel = new OrderModel();
        OrderEntryModel orderEntryModel = new OrderEntryModel();
        orderEntryModel.setEntryNumber(1);
        orderEntryModel.setQuantity(5L);
        orderEntryModel.setReturnQuantity(1L);
        originOrderModel.setEntries(Arrays.asList(orderEntryModel));
        when(parameter.getOriginOrder()).thenReturn(originOrderModel);
        when(requestMock.getReturnOrderEntries()).thenReturn(Arrays.asList(entryRequest1));
        when(entryRequest1.getQuantity()).thenReturn(3);
        when(entryRequest1.getEntryNumber()).thenReturn(1);
        when(returnOrderRepository.save(any(ReturnOrderModel.class))).thenReturn(returnOrder);
        service.create(parameter);
        verify(billRequest).setReturnOrderId(anyLong());
        verify(billService).createBillForReturnOrder(billRequest);
        verify(applicationEventPublisher).publishEvent(any(OrderEvent.class));
        verify(orderService).save(originOrderModel);
        assertEquals(1, originOrderModel.getEntries().size());
        assertEquals(4, orderEntryModel.getReturnQuantity(), 0);
    }

    @Test
    public void create_hasExchangeCart() {
        OrderModel originOrderModel = new OrderModel();
        OrderEntryModel orderEntryModel = new OrderEntryModel();
        orderEntryModel.setEntryNumber(1);
        orderEntryModel.setQuantity(5L);
        originOrderModel.setEntries(Arrays.asList(orderEntryModel));
        when(parameter.getOriginOrder()).thenReturn(originOrderModel);
        when(parameter.getReturnOrderRequest()).thenReturn(requestMock);
        when(requestMock.getReturnOrderEntries()).thenReturn(Arrays.asList(entryRequest1));
        when(entryRequest1.getQuantity()).thenReturn(3);
        when(entryRequest1.getEntryNumber()).thenReturn(1);
        when(returnOrderRepository.save(any(ReturnOrderModel.class))).thenReturn(returnOrder);
        when(parameter.getExchangeCart()).thenReturn(exchangeCart);
        when(exchangeCart.getCardNumber()).thenReturn("cardNumber");
        when(defaultCommercePlaceOrderStrategy.placeOrder(any(CommerceCheckoutParameter.class)))
                .thenReturn(commerceOrderResult);

        service.create(parameter);
        verify(billRequest).setReturnOrderId(anyLong());
        verify(billService).createBillForReturnOrder(billRequest);
        verify(applicationEventPublisher).publishEvent(any(OrderEvent.class));
        verify(orderService).save(originOrderModel);
        verify(defaultCommercePlaceOrderStrategy).placeOrder(commerceParameterArgumentCaptor.capture());
        assertEquals(1, originOrderModel.getEntries().size());
        assertEquals(2, orderEntryModel.getReturnQuantity(), 3);
        assertEquals("cardNumber", commerceParameterArgumentCaptor.getValue().getCardNumber());
    }


    @Test
    public void createReturnOverRemainMoney_InvalidRefundAmount() {
        when(returnOrderRepository.save(any(ReturnOrderModel.class))).thenReturn(returnOrder);
        when(parameter.getOriginOrder()).thenReturn(originOrder);
        when(requestMock.getRefundAmount()).thenReturn(1000d);
        when(originOrder.getCode()).thenReturn("code");
        when(originOrder.getCompanyId()).thenReturn(1L);
        when(originOrder.getCardNumber()).thenReturn("cardNumber");
        when(originOrder.getRedeemAmount()).thenReturn(5000d);
        when(originOrder.getRefundAmount()).thenReturn(2000d);
        when(billRequest.getFinalCost()).thenReturn(15000d);
        LoyaltyTransactionModel loyaltyTransactionModel = new LoyaltyTransactionModel();
        loyaltyTransactionModel.setConversionRate(1000d);
        when(loyaltyTransactionService.findLastByOrderCode("code")).thenReturn(loyaltyTransactionModel);
        when(loyaltyService.findByCardNumber("cardNumber", 1L)).thenReturn(cardData);
        when(calculationService.calculateRemainCashAmount(parameter)).thenReturn(12000d);
        when(calculationService.calculateMaxRefundAmount(parameter)).thenReturn(3000d);
        try {
            service.create(parameter);
            fail("Must throw exception");
        } catch (ServiceException err) {
            assertEquals(ErrorCodes.INVALID_REFUND_AMOUNT.code(), err.getCode());
        }

    }

    @Test
    public void createReturnInvalidRefundGreaterMaxRefund() {
        when(returnOrderRepository.save(any(ReturnOrderModel.class))).thenReturn(returnOrder);
        when(parameter.getOriginOrder()).thenReturn(originOrder);
        when(requestMock.getRefundAmount()).thenReturn(1100d);
        when(originOrder.getCode()).thenReturn("code");
        when(originOrder.getCompanyId()).thenReturn(1L);
        when(originOrder.getCardNumber()).thenReturn("cardNumber");
        when(originOrder.getRedeemAmount()).thenReturn(3000d);
        when(originOrder.getRefundAmount()).thenReturn(2000d);
        when(billRequest.getFinalCost()).thenReturn(11000d);
        LoyaltyTransactionModel loyaltyTransactionModel = new LoyaltyTransactionModel();
        loyaltyTransactionModel.setConversionRate(1000d);
        when(loyaltyTransactionService.findLastByOrderCode("code")).thenReturn(loyaltyTransactionModel);
        when(loyaltyService.findByCardNumber("cardNumber", 1L)).thenReturn(cardData);
        when(calculationService.calculateRemainCashAmount(parameter)).thenReturn(12000d);
        when(calculationService.calculateMaxRefundAmount(parameter)).thenReturn(1000d);
        try {
            service.create(parameter);
            fail("Must throw exception");
        } catch (ServiceException err) {
            assertEquals(ErrorCodes.INVALID_REFUND_AMOUNT.code(), err.getCode());
        }

    }

    @Test
    public void createHasRefund() {
        when(originOrder.getCode()).thenReturn("code");
        when(originOrder.getCompanyId()).thenReturn(1L);
        when(originOrder.getCardNumber()).thenReturn("cardNumber");
        when(originOrder.getRedeemAmount()).thenReturn(5000d);
        when(originOrder.getRefundAmount()).thenReturn(2000d);
        when(originOrder.getTotalRewardAmount()).thenReturn(0d);
        OrderEntryModel orderEntryModel = new OrderEntryModel();
        orderEntryModel.setEntryNumber(1);
        orderEntryModel.setQuantity(5L);
        when(originOrder.getEntries()).thenReturn(Arrays.asList(orderEntryModel));

        when(requestMock.getRefundAmount()).thenReturn(2000d);
        when(requestMock.getReturnOrderEntries()).thenReturn(Arrays.asList(entryRequest1));
        when(entryRequest1.getQuantity()).thenReturn(3);
        when(entryRequest1.getEntryNumber()).thenReturn(1);
        when(billRequest.getFinalCost()).thenReturn(11000d);
        LoyaltyTransactionModel loyaltyTransactionModel = new LoyaltyTransactionModel();
        loyaltyTransactionModel.setConversionRate(1000d);
        when(returnOrderRepository.save(any(ReturnOrderModel.class))).thenReturn(returnOrder);
        when(loyaltyTransactionService.findLastByOrderCode("code")).thenReturn(loyaltyTransactionModel);
        when(loyaltyService.findByCardNumber("cardNumber", 1L)).thenReturn(cardData);
        when(calculationService.calculateMaxRefundAmount(parameter)).thenReturn(3000d);
        when(calculationService.calculateRemainCashAmount(parameter)).thenReturn(12000d);
        when(loyaltyService.refund(eq(originOrder), eq(returnOrder), eq(2000d))).thenReturn(transactionData);
        when(transactionData.getRefundAmount()).thenReturn(2000d);
        service.create(parameter);
        verify(billRequest).setReturnOrderId(anyLong());
        verify(billService).createBillForReturnOrder(billRequest);
        verify(applicationEventPublisher).publishEvent(any(OrderEvent.class));
        verify(loyaltyService).refund(eq(originOrder), eq(returnOrder), eq(2000d));
        verify(returnOrder).setConversionRate(1000d);
        verify(originOrder).setRefundAmount(4000d);
        verify(returnOrder).setRefundAmount(2000d);
        verify(returnOrder).setRedeemAmount(3000d);
        verify(orderService).save(originOrder);
        assertEquals(2, orderEntryModel.getReturnQuantity(), 3);

    }


    @Test
    public void createHasRefundFloorCalculateMaxRefundAmount() {
        when(originOrder.getCode()).thenReturn("code");
        when(originOrder.getCompanyId()).thenReturn(1L);
        when(originOrder.getCardNumber()).thenReturn("cardNumber");
        when(originOrder.getRedeemAmount()).thenReturn(5000d);
        when(originOrder.getRefundAmount()).thenReturn(2000d);
        OrderEntryModel orderEntryModel = new OrderEntryModel();
        orderEntryModel.setEntryNumber(1);
        orderEntryModel.setQuantity(5L);
        when(originOrder.getEntries()).thenReturn(Arrays.asList(orderEntryModel));

        when(requestMock.getRefundAmount()).thenReturn(2000d);
        when(requestMock.getReturnOrderEntries()).thenReturn(Arrays.asList(entryRequest1));
        when(entryRequest1.getQuantity()).thenReturn(3);
        when(entryRequest1.getEntryNumber()).thenReturn(1);
        when(billRequest.getFinalCost()).thenReturn(11000d);
        LoyaltyTransactionModel loyaltyTransactionModel = new LoyaltyTransactionModel();
        loyaltyTransactionModel.setConversionRate(1000d);
        when(returnOrderRepository.save(any(ReturnOrderModel.class))).thenReturn(returnOrder);
        when(loyaltyTransactionService.findLastByOrderCode("code")).thenReturn(loyaltyTransactionModel);
        when(loyaltyService.findByCardNumber("cardNumber", 1L)).thenReturn(cardData);
        when(calculationService.calculateMaxRefundAmount(parameter)).thenReturn(2500d);
        when(calculationService.calculateRemainCashAmount(parameter)).thenReturn(12000d);
        when(loyaltyService.refund(eq(originOrder), eq(returnOrder), eq(2000d))).thenReturn(transactionData);
        when(transactionData.getRefundAmount()).thenReturn(2000d);
        service.create(parameter);
        verify(billRequest).setReturnOrderId(anyLong());
        verify(billService).createBillForReturnOrder(billRequest);
        verify(applicationEventPublisher).publishEvent(any(OrderEvent.class));
        verify(loyaltyService).refund(eq(originOrder), eq(returnOrder), eq(2000d));
        verify(returnOrder).setConversionRate(1000d);
        verify(originOrder).setRefundAmount(4000d);
        verify(returnOrder).setRefundAmount(2000d);
        verify(returnOrder).setRedeemAmount(3000d);
        verify(orderService).save(originOrder);
        assertEquals(2, orderEntryModel.getReturnQuantity(), 3);

    }

    @Test
    public void createHasRefundCeilCalculateMaxRefundAmount() {
        when(originOrder.getCode()).thenReturn("code");
        when(originOrder.getCompanyId()).thenReturn(1L);
        when(originOrder.getCardNumber()).thenReturn("cardNumber");
        when(originOrder.getRedeemAmount()).thenReturn(5000d);
        when(originOrder.getRefundAmount()).thenReturn(2000d);
        OrderEntryModel orderEntryModel = new OrderEntryModel();
        orderEntryModel.setEntryNumber(1);
        orderEntryModel.setQuantity(5L);
        when(originOrder.getEntries()).thenReturn(Arrays.asList(orderEntryModel));

        when(requestMock.getRefundAmount()).thenReturn(2000d);
        when(requestMock.getReturnOrderEntries()).thenReturn(Arrays.asList(entryRequest1));
        when(entryRequest1.getQuantity()).thenReturn(3);
        when(entryRequest1.getEntryNumber()).thenReturn(1);
        when(billRequest.getFinalCost()).thenReturn(1500d);
        LoyaltyTransactionModel loyaltyTransactionModel = new LoyaltyTransactionModel();
        loyaltyTransactionModel.setConversionRate(1000d);
        when(returnOrderRepository.save(any(ReturnOrderModel.class))).thenReturn(returnOrder);
        when(loyaltyTransactionService.findLastByOrderCode("code")).thenReturn(loyaltyTransactionModel);
        when(loyaltyService.findByCardNumber("cardNumber", 1L)).thenReturn(cardData);
        when(calculationService.calculateMaxRefundAmount(parameter)).thenReturn(1500d);
        when(calculationService.calculateRemainCashAmount(parameter)).thenReturn(0d);
        when(loyaltyService.refund(eq(originOrder), eq(returnOrder), eq(2000d))).thenReturn(transactionData);
        when(transactionData.getRefundAmount()).thenReturn(2000d);
        service.create(parameter);
        verify(billRequest).setReturnOrderId(anyLong());
        verify(billService).createBillForReturnOrder(billRequest);
        verify(applicationEventPublisher).publishEvent(any(OrderEvent.class));
        verify(loyaltyService).refund(eq(originOrder), eq(returnOrder), eq(2000d));
        verify(returnOrder).setConversionRate(1000d);
        verify(originOrder).setRefundAmount(4000d);
        verify(returnOrder).setRefundAmount(2000d);
        verify(returnOrder).setRedeemAmount(3000d);
        verify(orderService).save(originOrder);
        assertEquals(2, orderEntryModel.getReturnQuantity(), 3);

    }

    @Test
    public void createHasRefund_RemainMoneyZero_RefundAmountLessFinal() {
        when(originOrder.getCode()).thenReturn("code");
        when(originOrder.getCompanyId()).thenReturn(1L);
        when(originOrder.getCardNumber()).thenReturn("cardNumber");
        when(originOrder.getRedeemAmount()).thenReturn(5000d);
        when(originOrder.getRefundAmount()).thenReturn(2000d);
        OrderEntryModel orderEntryModel = new OrderEntryModel();
        orderEntryModel.setEntryNumber(1);
        orderEntryModel.setQuantity(5L);
        when(originOrder.getEntries()).thenReturn(Arrays.asList(orderEntryModel));

        when(requestMock.getRefundAmount()).thenReturn(2000d);
        when(requestMock.getReturnOrderEntries()).thenReturn(Arrays.asList(entryRequest1));
        when(entryRequest1.getQuantity()).thenReturn(3);
        when(entryRequest1.getEntryNumber()).thenReturn(1);
        when(billRequest.getFinalCost()).thenReturn(2500d);
        LoyaltyTransactionModel loyaltyTransactionModel = new LoyaltyTransactionModel();
        loyaltyTransactionModel.setConversionRate(1000d);
        when(returnOrderRepository.save(any(ReturnOrderModel.class))).thenReturn(returnOrder);
        when(loyaltyTransactionService.findLastByOrderCode("code")).thenReturn(loyaltyTransactionModel);
        when(loyaltyService.findByCardNumber("cardNumber", 1L)).thenReturn(cardData);
        when(calculationService.calculateMaxRefundAmount(parameter)).thenReturn(1500d);
        when(calculationService.calculateRemainCashAmount(parameter)).thenReturn(0d);
        when(loyaltyService.refund(eq(originOrder), eq(returnOrder), eq(2000d))).thenReturn(transactionData);
        when(transactionData.getRefundAmount()).thenReturn(2000d);
        service.create(parameter);
        verify(billRequest).setReturnOrderId(anyLong());
        verify(billService).createBillForReturnOrder(billRequest);
        verify(applicationEventPublisher).publishEvent(any(OrderEvent.class));
        verify(loyaltyService).refund(eq(originOrder), eq(returnOrder), eq(2000d));
        verify(returnOrder).setConversionRate(1000d);
        verify(originOrder).setRefundAmount(4000d);
        verify(returnOrder).setRefundAmount(2000d);
        verify(returnOrder).setRedeemAmount(3000d);
        verify(orderService).save(originOrder);
        assertEquals(2, orderEntryModel.getReturnQuantity(), 3);

    }


    @Test
    public void createHasRefund_RemainMoneyNegative_RefundAmountLessFinal() {
        when(originOrder.getCode()).thenReturn("code");
        when(originOrder.getCompanyId()).thenReturn(1L);
        when(originOrder.getCardNumber()).thenReturn("cardNumber");
        when(originOrder.getRedeemAmount()).thenReturn(5000d);
        when(originOrder.getRefundAmount()).thenReturn(2000d);
        OrderEntryModel orderEntryModel = new OrderEntryModel();
        orderEntryModel.setEntryNumber(1);
        orderEntryModel.setQuantity(5L);
        when(originOrder.getEntries()).thenReturn(Arrays.asList(orderEntryModel));

        when(requestMock.getRefundAmount()).thenReturn(2000d);
        when(requestMock.getReturnOrderEntries()).thenReturn(Arrays.asList(entryRequest1));
        when(entryRequest1.getQuantity()).thenReturn(3);
        when(entryRequest1.getEntryNumber()).thenReturn(1);
        when(billRequest.getFinalCost()).thenReturn(2500d);
        LoyaltyTransactionModel loyaltyTransactionModel = new LoyaltyTransactionModel();
        loyaltyTransactionModel.setConversionRate(1000d);
        when(returnOrderRepository.save(any(ReturnOrderModel.class))).thenReturn(returnOrder);
        when(loyaltyTransactionService.findLastByOrderCode("code")).thenReturn(loyaltyTransactionModel);
        when(loyaltyService.findByCardNumber("cardNumber", 1L)).thenReturn(cardData);
        when(calculationService.calculateMaxRefundAmount(parameter)).thenReturn(1500d);
        when(calculationService.calculateRemainCashAmount(parameter)).thenReturn(-5d);
        when(loyaltyService.refund(eq(originOrder), eq(returnOrder), eq(2000d))).thenReturn(transactionData);
        when(transactionData.getRefundAmount()).thenReturn(2000d);
        service.create(parameter);
        verify(billRequest).setReturnOrderId(anyLong());
        verify(billService).createBillForReturnOrder(billRequest);
        verify(applicationEventPublisher).publishEvent(any(OrderEvent.class));
        verify(loyaltyService).refund(eq(originOrder), eq(returnOrder), eq(2000d));
        verify(returnOrder).setConversionRate(1000d);
        verify(originOrder).setRefundAmount(4000d);
        verify(returnOrder).setRefundAmount(2000d);
        verify(returnOrder).setRedeemAmount(3000d);
        verify(orderService).save(originOrder);
        assertEquals(2, orderEntryModel.getReturnQuantity(), 3);

    }


    @Test
    public void createReturnRevert_HasNotCompensateRevert() {
        when(originOrder.getCode()).thenReturn("code");
        when(originOrder.getCompanyId()).thenReturn(1L);
        when(originOrder.getCardNumber()).thenReturn("cardNumber");
        when(originOrder.getTotalRewardAmount()).thenReturn(5000d);
        OrderEntryModel orderEntryModel = new OrderEntryModel();
        orderEntryModel.setEntryNumber(1);
        orderEntryModel.setQuantity(5L);
        when(originOrder.getEntries()).thenReturn(Arrays.asList(orderEntryModel));
        when(requestMock.getReturnOrderEntries()).thenReturn(Arrays.asList(entryRequest1));
        when(entryRequest1.getQuantity()).thenReturn(3);
        when(entryRequest1.getEntryNumber()).thenReturn(1);
        when(billRequest.getFinalCost()).thenReturn(11000d);
        LoyaltyTransactionModel loyaltyTransactionModel = new LoyaltyTransactionModel();
        loyaltyTransactionModel.setConversionRate(1000d);
        when(returnOrderRepository.save(any(ReturnOrderModel.class))).thenReturn(returnOrder);
        when(loyaltyTransactionService.findLastByOrderCode("code")).thenReturn(loyaltyTransactionModel);
        when(loyaltyService.findByCardNumber("cardNumber", 1L)).thenReturn(cardData);
        when(cardData.getPointAmount()).thenReturn(5.55d);
        when(cardData.getPendingAmount()).thenReturn(4.55);
        when(calculationService.calculateMaxRevertAmount(requestMock, originOrder)).thenReturn(3000d);
        when(calculationService.calculateRemainCashAmount(parameter)).thenReturn(12000d);
        when(loyaltyService.revert(eq(originOrder), eq(returnOrder), eq(3000d))).thenReturn(transactionData);
        when(transactionData.getRevertAmount()).thenReturn(3000d);

        service.create(parameter);

        verify(billRequest).setReturnOrderId(anyLong());
        verify(billService).createBillForReturnOrder(billRequest);
        verify(applicationEventPublisher).publishEvent(any(OrderEvent.class));
        verify(loyaltyService).revert(eq(originOrder), eq(returnOrder), eq(3000d));
        verify(returnOrder).setConversionRate(1000d);
        verify(returnOrder).setRevertAmount(3000d);
        verify(orderService).save(originOrder);
        assertEquals(2, orderEntryModel.getReturnQuantity(), 3);
    }

    @Test
    public void createReturnRevert_HasCompensateRevert() {
        when(originOrder.getCode()).thenReturn("code");
        when(originOrder.getCompanyId()).thenReturn(1L);
        when(originOrder.getCardNumber()).thenReturn("cardNumber");
        when(originOrder.getTotalRewardAmount()).thenReturn(15000d);
        OrderEntryModel orderEntryModel = new OrderEntryModel();
        orderEntryModel.setEntryNumber(1);
        orderEntryModel.setQuantity(5L);
        when(originOrder.getEntries()).thenReturn(Arrays.asList(orderEntryModel));
        when(requestMock.getReturnOrderEntries()).thenReturn(Arrays.asList(entryRequest1));
        when(entryRequest1.getQuantity()).thenReturn(3);
        when(entryRequest1.getEntryNumber()).thenReturn(1);
        when(billRequest.getFinalCost()).thenReturn(11000d);
        LoyaltyTransactionModel loyaltyTransactionModel = new LoyaltyTransactionModel();
        loyaltyTransactionModel.setConversionRate(1000d);
        when(returnOrderRepository.save(any(ReturnOrderModel.class))).thenReturn(returnOrder);
        when(loyaltyTransactionService.findLastByOrderCode("code")).thenReturn(loyaltyTransactionModel);
        when(loyaltyService.findByCardNumber("cardNumber", 1L)).thenReturn(cardData);
        when(cardData.getPointAmount()).thenReturn(5.55d);
        when(cardData.getPendingAmount()).thenReturn(4.45);
        when(calculationService.calculateMaxRevertAmount(requestMock, originOrder)).thenReturn(12000d);
        when(calculationService.calculateRemainCashAmount(parameter)).thenReturn(12000d);
        when(loyaltyService.revert(eq(originOrder), eq(returnOrder), eq(10000d))).thenReturn(transactionData);
        when(transactionData.getRevertAmount()).thenReturn(8000d);

        service.create(parameter);

        verify(billRequest).setReturnOrderId(anyLong());
        verify(billService).createBillForReturnOrder(billRequest);
        verify(applicationEventPublisher).publishEvent(any(OrderEvent.class));
        verify(loyaltyService).revert(eq(originOrder), eq(returnOrder), eq(10000d));
        verify(returnOrder).setRevertAmount(8000d);
        verify(returnOrder).setCompensateRevert(2000d);
        verify(orderService).save(originOrder);
        assertEquals(2, orderEntryModel.getReturnQuantity(), 3);
    }

    @Test
    public void createReturnRevert_HasRefundHasCompensateRevert() {
        when(originOrder.getCode()).thenReturn("code");
        when(originOrder.getCompanyId()).thenReturn(1L);
        when(originOrder.getCardNumber()).thenReturn("cardNumber");
        when(originOrder.getTotalRewardAmount()).thenReturn(15000d);
        when(originOrder.getRedeemAmount()).thenReturn(5000d);
        when(originOrder.getRefundAmount()).thenReturn(3000d);
        OrderEntryModel orderEntryModel = new OrderEntryModel();
        orderEntryModel.setEntryNumber(1);
        orderEntryModel.setQuantity(5L);
        when(originOrder.getEntries()).thenReturn(Arrays.asList(orderEntryModel));
        when(requestMock.getRefundAmount()).thenReturn(2000d);
        when(requestMock.getReturnOrderEntries()).thenReturn(Arrays.asList(entryRequest1));
        when(entryRequest1.getQuantity()).thenReturn(3);
        when(entryRequest1.getEntryNumber()).thenReturn(1);
        when(billRequest.getFinalCost()).thenReturn(11000d);
        LoyaltyTransactionModel loyaltyTransactionModel = new LoyaltyTransactionModel();
        loyaltyTransactionModel.setConversionRate(1000d);
        when(returnOrderRepository.save(any(ReturnOrderModel.class))).thenReturn(returnOrder);
        when(loyaltyTransactionService.findLastByOrderCode("code")).thenReturn(loyaltyTransactionModel);
        when(loyaltyService.findByCardNumber("cardNumber", 1L)).thenReturn(cardData);
        when(cardData.getPointAmount()).thenReturn(5.55d);
        when(cardData.getPendingAmount()).thenReturn(4.45);
        when(calculationService.calculateMaxRevertAmount(requestMock, originOrder)).thenReturn(13000d);
        when(calculationService.calculateMaxRefundAmount(parameter)).thenReturn(2000d);
        when(calculationService.calculateRemainCashAmount(parameter)).thenReturn(12000d);
        when(loyaltyService.revert(eq(originOrder), eq(returnOrder), eq(12000d))).thenReturn(transactionData);
        when(loyaltyService.refund(eq(originOrder), eq(returnOrder), eq(2000d))).thenReturn(transactionData1);
        when(transactionData.getRevertAmount()).thenReturn(8000d);
        when(transactionData1.getRefundAmount()).thenReturn(2000d);
        service.create(parameter);

        verify(billRequest).setReturnOrderId(anyLong());
        verify(billService).createBillForReturnOrder(billRequest);
        verify(applicationEventPublisher).publishEvent(any(OrderEvent.class));
        verify(loyaltyService).revert(eq(originOrder), eq(returnOrder), eq(12000d));
        verify(loyaltyService).refund(eq(originOrder), eq(returnOrder), eq(2000d));
        verify(returnOrder).setRevertAmount(8000d);
        verify(returnOrder).setCompensateRevert(1000d);
        verify(returnOrder).setRefundAmount(2000d);
        verify(originOrder).setRefundAmount(5000d);
        verify(orderService).save(originOrder);
        assertEquals(2, orderEntryModel.getReturnQuantity(), 3);
    }


    @Test
    public void findAll() {
        service.findAllByCompanyId(1l, PageRequest.of(0, 10));
        verify(returnOrderRepository).findAllByCompanyId(anyLong(), any(Pageable.class));
    }

    @Test
    public void findByIdAndCompanyId() {
        service.findByIdAndCompanyId(99l, 1l);
        verify(returnOrderRepository).findByIdAndCompanyId(99l, 1l);
    }

    @Test
    public void getReturnRewardRedeem_HasNotLoyaltyTransaction() {
        when(originOrder.getCode()).thenReturn("code");
        when(originOrder.getCompanyId()).thenReturn(1L);
        when(originOrder.getCardNumber()).thenReturn("card");
        when(loyaltyTransactionService.findLastByOrderCode("code")).thenReturn(null);
        LoyaltyCardData loyaltyCardData = new LoyaltyCardData();
        loyaltyCardData.setPointAmount(20000d);
        loyaltyCardData.setPendingAmount(0d);
        when(loyaltyService.findByCardNumber(anyString(), anyLong())).thenReturn(loyaltyCardData);
        ReturnRewardRedeemData returnRewardRedeemData = service.getReturnRewardRedeem(parameter);
        assertEquals(20000d, returnRewardRedeemData.getAvailablePoint(), 0);
        assertEquals(0d, returnRewardRedeemData.getPendingPoint(), 0);
        assertNull(returnRewardRedeemData.getRemainRedeemPoint());
        assertNull(returnRewardRedeemData.getRefundPoint());
        assertNull(returnRewardRedeemData.getRevertPoint());
        assertNull(returnRewardRedeemData.getRemainMoney());
        assertNull(returnRewardRedeemData.getConversionRate());
    }

    @Test
    public void getReturnRewardRedeem_HasNotCardNumber() {
        when(originOrder.getCode()).thenReturn("code");
        when(originOrder.getCompanyId()).thenReturn(1L);
        when(originOrder.getCardNumber()).thenReturn(null);
        when(loyaltyTransactionService.findLastByOrderCode("code")).thenReturn(new LoyaltyTransactionModel());
        ReturnRewardRedeemData returnRewardRedeemData = service.getReturnRewardRedeem(parameter);
        assertNull(returnRewardRedeemData.getAvailablePoint());
        assertNull(returnRewardRedeemData.getPendingPoint());
        assertNull(returnRewardRedeemData.getRemainRedeemPoint());
        assertNull(returnRewardRedeemData.getRefundPoint());
        assertNull(returnRewardRedeemData.getRevertPoint());
        assertNull(returnRewardRedeemData.getRemainMoney());
        assertNull(returnRewardRedeemData.getConversionRate());
    }

    @Test
    public void getReturnRewardRedeem_HasNotRedeem() {
        when(originOrder.getCode()).thenReturn("code");
        when(originOrder.getCompanyId()).thenReturn(1L);
        when(originOrder.getCardNumber()).thenReturn("cardNumber");
        LoyaltyTransactionModel loyaltyTransactionModel = new LoyaltyTransactionModel();
        loyaltyTransactionModel.setConversionRate(1000d);
        when(loyaltyTransactionService.findLastByOrderCode("code")).thenReturn(loyaltyTransactionModel);
        when(loyaltyService.findByCardNumber("cardNumber", 1L)).thenReturn(cardData);
        when(cardData.getPointAmount()).thenReturn(5.55d);
        when(cardData.getPendingAmount()).thenReturn(4.55);
        when(calculationService.calculateRemainCashAmount(parameter)).thenReturn(12000d);
        ReturnRewardRedeemData returnRewardRedeemData = service.getReturnRewardRedeem(parameter);
        verify(loyaltyTransactionService, times(2)).findLastByOrderCode("code");
        verify(loyaltyService).findByCardNumber("cardNumber", 1L);
        assertEquals(5.55, returnRewardRedeemData.getAvailablePoint(), 0);
        assertEquals(4.55, returnRewardRedeemData.getPendingPoint(), 0);
        assertEquals(0, returnRewardRedeemData.getRefundPoint(), 0);
        assertEquals(0, returnRewardRedeemData.getRevertPoint(), 0);
        assertEquals(12000, returnRewardRedeemData.getRemainMoney(), 0);
        assertEquals(1000, returnRewardRedeemData.getConversionRate(), 0);
    }

    @Test
    public void getReturnRewardRedeem_HasRedeem() {
        when(originOrder.getCode()).thenReturn("code");
        when(originOrder.getCompanyId()).thenReturn(1L);
        when(originOrder.getCardNumber()).thenReturn("cardNumber");
        when(originOrder.getRedeemAmount()).thenReturn(3000d);
        when(originOrder.getRefundAmount()).thenReturn(1000d);
        LoyaltyTransactionModel loyaltyTransactionModel = new LoyaltyTransactionModel();
        loyaltyTransactionModel.setConversionRate(1000d);
        when(loyaltyTransactionService.findLastByOrderCode("code")).thenReturn(loyaltyTransactionModel);
        when(loyaltyService.findByCardNumber("cardNumber", 1L)).thenReturn(cardData);
        when(cardData.getPointAmount()).thenReturn(5.55d);
        when(cardData.getPendingAmount()).thenReturn(4.55);
        when(calculationService.calculateRemainCashAmount(parameter)).thenReturn(12000d);
        when(calculationService.calculateMaxRefundAmount(parameter)).thenReturn(1000d);
        ReturnRewardRedeemData returnRewardRedeemData = service.getReturnRewardRedeem(parameter);
        verify(loyaltyTransactionService, times(2)).findLastByOrderCode("code");
        verify(loyaltyService).findByCardNumber("cardNumber", 1L);
        assertEquals(5.55, returnRewardRedeemData.getAvailablePoint(), 0);
        assertEquals(4.55, returnRewardRedeemData.getPendingPoint(), 0);
        assertEquals(2, returnRewardRedeemData.getRemainRedeemPoint(), 0);
        assertEquals(1, returnRewardRedeemData.getRefundPoint(), 0);
        assertEquals(0, returnRewardRedeemData.getRevertPoint(), 0);
        assertEquals(12000, returnRewardRedeemData.getRemainMoney(), 0);
        assertEquals(1000, returnRewardRedeemData.getConversionRate(), 0);
    }

    @Test
    public void getReturnRewardRedeem_HasRevert() {
        when(originOrder.getCode()).thenReturn("code");
        when(originOrder.getCompanyId()).thenReturn(1L);
        when(originOrder.getCardNumber()).thenReturn("cardNumber");
        when(originOrder.getTotalRewardAmount()).thenReturn(10000d);
        LoyaltyTransactionModel loyaltyTransactionModel = new LoyaltyTransactionModel();
        loyaltyTransactionModel.setConversionRate(1000d);
        when(loyaltyTransactionService.findLastByOrderCode("code")).thenReturn(loyaltyTransactionModel);
        when(loyaltyService.findByCardNumber("cardNumber", 1L)).thenReturn(cardData);
        when(cardData.getPointAmount()).thenReturn(5.55d);
        when(cardData.getPendingAmount()).thenReturn(4.55);
        when(calculationService.calculateRemainCashAmount(parameter)).thenReturn(12000d);
        when(calculationService.calculateMaxRevertAmount(requestMock, originOrder)).thenReturn(5220d);
        ReturnRewardRedeemData returnRewardRedeemData = service.getReturnRewardRedeem(parameter);
        verify(loyaltyTransactionService, times(2)).findLastByOrderCode("code");
        verify(loyaltyService).findByCardNumber("cardNumber", 1L);
        assertEquals(5.55, returnRewardRedeemData.getAvailablePoint(), 0);
        assertEquals(4.55, returnRewardRedeemData.getPendingPoint(), 0);
        assertEquals(0, returnRewardRedeemData.getRefundPoint(), 0);
        assertEquals(5.22, returnRewardRedeemData.getRevertPoint(), 0);
        assertEquals(12000, returnRewardRedeemData.getRemainMoney(), 0);
        assertEquals(1000, returnRewardRedeemData.getConversionRate(), 0);
    }

    @Test
    public void getReturnRewardRedeem_HasBothRedeemAndHasRefund() {
        when(originOrder.getCode()).thenReturn("code");
        when(originOrder.getCompanyId()).thenReturn(1L);
        when(originOrder.getCardNumber()).thenReturn("cardNumber");
        when(originOrder.getRedeemAmount()).thenReturn(3000d);
        when(originOrder.getRefundAmount()).thenReturn(1000d);
        when(originOrder.getTotalRewardAmount()).thenReturn(10000d);
        LoyaltyTransactionModel loyaltyTransactionModel = new LoyaltyTransactionModel();
        loyaltyTransactionModel.setConversionRate(1000d);
        when(loyaltyTransactionService.findLastByOrderCode("code")).thenReturn(loyaltyTransactionModel);
        when(loyaltyService.findByCardNumber("cardNumber", 1L)).thenReturn(cardData);
        when(cardData.getPointAmount()).thenReturn(5.55d);
        when(cardData.getPendingAmount()).thenReturn(4.55);
        when(calculationService.calculateRemainCashAmount(parameter)).thenReturn(12000d);
        when(calculationService.calculateMaxRefundAmount(parameter)).thenReturn(1000d);
        when(calculationService.calculateMaxRevertAmount(requestMock, originOrder)).thenReturn(5220d);
        ReturnRewardRedeemData returnRewardRedeemData = service.getReturnRewardRedeem(parameter);
        verify(loyaltyTransactionService, times(2)).findLastByOrderCode("code");
        verify(loyaltyService).findByCardNumber("cardNumber", 1L);
        verify(calculationService).calculateRemainCashAmount(parameter);
        verify(calculationService).calculateMaxRefundAmount(parameter);
        verify(calculationService).calculateMaxRevertAmount(requestMock, originOrder);
        assertEquals(5.55, returnRewardRedeemData.getAvailablePoint(), 0);
        assertEquals(4.55, returnRewardRedeemData.getPendingPoint(), 0);
        assertEquals(2, returnRewardRedeemData.getRemainRedeemPoint(), 0);
        assertEquals(1, returnRewardRedeemData.getRefundPoint(), 0);
        assertEquals(5.22, returnRewardRedeemData.getRevertPoint(), 0);
        assertEquals(12000, returnRewardRedeemData.getRemainMoney(), 0);
        assertEquals(1000, returnRewardRedeemData.getConversionRate(), 0);
    }

    @Test
    public void getReturnRewardRedeem_newAvailablePoint_exist_advancePoints_pendingAward() {
        when(originOrder.getCode()).thenReturn("code");
        when(originOrder.getCompanyId()).thenReturn(1L);
        when(originOrder.getCardNumber()).thenReturn("cardNumber");
        when(originOrder.getRedeemAmount()).thenReturn(14000d);
        when(originOrder.getRefundAmount()).thenReturn(60000d);
        when(originOrder.getTotalRewardAmount()).thenReturn(10000d);
        LoyaltyTransactionModel loyaltyTransactionModel = new LoyaltyTransactionModel();
        loyaltyTransactionModel.setConversionRate(1000d);
        when(loyaltyTransactionService.findLastByOrderCode("code")).thenReturn(loyaltyTransactionModel);
        when(loyaltyService.findByCardNumber("cardNumber", 1L)).thenReturn(cardData);
        when(cardData.getPointAmount()).thenReturn(10d);
        when(cardData.getPendingAmount()).thenReturn(0d);
        when(calculationService.calculateRemainCashAmount(parameter)).thenReturn(36000d);
        when(calculationService.calculateMaxRefundAmount(parameter)).thenReturn(14000d);
        when(calculationService.calculateMaxRevertAmount(requestMock, originOrder)).thenReturn(60000d);
        when(loyaltyTransactionService.findLastByOrderCodeAndListType(anyString(), anyList()))
                .thenReturn(loyaltyTransactionModel);
        when(transactionData.getStatus()).thenReturn("Status");
        when(loyaltyService.findByInvoiceNumberAndCompanyIdAndType(any())).thenReturn(transactionData);
        ReturnRewardRedeemData data = service.getReturnRewardRedeem(parameter);
        verify(loyaltyTransactionService, times(2)).findLastByOrderCode("code");
        verify(loyaltyService).findByCardNumber("cardNumber", 1L);
        verify(calculationService).calculateRemainCashAmount(parameter);
        verify(calculationService).calculateMaxRefundAmount(parameter);
        verify(calculationService).calculateMaxRevertAmount(requestMock, originOrder);
        assertEquals(10, data.getAvailablePoint(), 0);
        assertEquals(0, data.getPendingPoint(), 0);
        assertEquals(14, data.getRefundPoint(), 0);
        assertEquals(60, data.getRevertPoint(), 0);
        assertEquals(36000d, data.getRemainMoney(), 0);
        assertEquals(1000, data.getConversionRate(), 0);
        assertEquals(24, data.getNewAvailablePoint(), 0);
    }

    @Test
    public void getReturnRewardRedeem_newAvailablePoint_not_exist_advancePoints() {
        when(originOrder.getCode()).thenReturn("code");
        when(originOrder.getCompanyId()).thenReturn(1L);
        when(originOrder.getCardNumber()).thenReturn("cardNumber");
        when(originOrder.getRedeemAmount()).thenReturn(20000d);
        when(originOrder.getRefundAmount()).thenReturn(12000d);
        when(originOrder.getTotalRewardAmount()).thenReturn(12000d);
        LoyaltyTransactionModel loyaltyTransactionModel = new LoyaltyTransactionModel();
        loyaltyTransactionModel.setConversionRate(1000d);
        when(loyaltyTransactionService.findLastByOrderCode("code")).thenReturn(loyaltyTransactionModel);
        when(loyaltyService.findByCardNumber("cardNumber", 1L)).thenReturn(cardData);
        when(cardData.getPointAmount()).thenReturn(26d);
        when(cardData.getPendingAmount()).thenReturn(0d);
        when(calculationService.calculateRemainCashAmount(parameter)).thenReturn(0d);
        when(calculationService.calculateMaxRefundAmount(parameter)).thenReturn(20000d);
        when(calculationService.calculateMaxRevertAmount(requestMock, originOrder)).thenReturn(12000d);
        when(loyaltyTransactionService.findLastByOrderCodeAndListType(anyString(), anyList())).thenReturn(loyaltyTransactionModel);
        when(transactionData.getStatus()).thenReturn(OrderStatus.COMPLETED.code());
        when(loyaltyService.findByInvoiceNumberAndCompanyIdAndType(any())).thenReturn(transactionData);
        ReturnRewardRedeemData data = service.getReturnRewardRedeem(parameter);
        verify(loyaltyTransactionService, times(2)).findLastByOrderCode("code");
        verify(loyaltyService).findByCardNumber("cardNumber", 1L);
        verify(calculationService).calculateRemainCashAmount(parameter);
        verify(calculationService).calculateMaxRefundAmount(parameter);
        verify(calculationService).calculateMaxRevertAmount(requestMock, originOrder);
        assertEquals(26, data.getAvailablePoint(), 0);
        assertEquals(0, data.getPendingPoint(), 0);
        assertEquals(20, data.getRefundPoint(), 0);
        assertEquals(12, data.getRevertPoint(), 0);
        assertEquals(0d, data.getRemainMoney(), 0);
        assertEquals(1000, data.getConversionRate(), 0);
        assertEquals(34, data.getNewAvailablePoint(), 0);
    }

    @Test
    public void findAllByCompanyIdAndCreatedTimeGreaterThanEqual() {
        service.findAllByCompanyIdAndCreatedTimeGreaterThanEqual(anyLong(), any());
        verify(returnOrderRepository).findAllByCompanyIdAndCreatedTimeGreaterThanEqual(anyLong(), any());
    }

    @Test
    public void findAllByCompanyId() {
        service.findAllByCompanyId(anyLong());
        verify(returnOrderRepository).findAllByCompanyId(anyLong());
    }

}
