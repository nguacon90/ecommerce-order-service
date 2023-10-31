package com.vctek.orderservice.facade.impl;

import com.vctek.converter.Converter;
import com.vctek.converter.Populator;
import com.vctek.exception.ServiceException;
import com.vctek.kafka.data.loyalty.TransactionData;
import com.vctek.orderservice.dto.*;
import com.vctek.orderservice.dto.request.PaymentTransactionRequest;
import com.vctek.orderservice.dto.request.ReturnOrderRequest;
import com.vctek.orderservice.dto.request.ReturnOrderSearchRequest;
import com.vctek.orderservice.dto.request.ReturnOrderUpdateParameter;
import com.vctek.orderservice.event.ReturnOrderEvent;
import com.vctek.orderservice.event.ReturnOrderEventType;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.kafka.producer.LoyaltyInvoiceProducerService;
import com.vctek.orderservice.kafka.producer.ReturnOrdersProducerService;
import com.vctek.orderservice.kafka.producer.UpdateReturnOrderProducer;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.OrderEntryModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.ReturnOrderModel;
import com.vctek.orderservice.service.*;
import com.vctek.orderservice.util.DiscountType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.cluster.lock.support.DelegatingDistributedLock;
import org.springframework.cloud.cluster.redis.lock.RedisLockService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class ReturnOrderFacadeTest {
    private ReturnOrderFacadeImpl facade;

    @Mock
    private ReturnOrderService returnOrderService;
    @Mock
    private Converter<ReturnOrderRequest, ReturnOrderCommerceParameter> returnOrderCommerceParameterConverter;
    @Mock
    private Converter<ReturnOrderModel, ReturnOrderData> basicReturnOrderConverter;
    @Mock
    private Converter<OrderModel, OrderData> orderConverter;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private ReturnOrderRequest returnOrderRequest;
    @Mock
    private ReturnOrderCommerceParameter parameter;
    @Mock
    private ReturnOrderModel returnOrderModel;
    private ArgumentCaptor<ReturnOrderEvent> captor = ArgumentCaptor.forClass(ReturnOrderEvent.class);
    @Mock
    private ReturnOrderData returnOrderData;
    @Mock
    private Populator<ReturnOrderModel, ReturnOrderData> returnOrderDetailPopulator;
    @Mock
    private ReturnOrderUpdateParameter returnOrderUpdateParameter;
    @Mock
    private OrderModel exchangeOrder;
    @Mock
    private OrderService orderService;
    @Mock
    private OrderModel originOrder;
    @Mock
    private GenerateCartCodeService generateCartCodeService;
    @Mock
    private CommerceCartService commerceCartService;
    @Mock
    private AuthService authService;
    @Mock
    private UpdateReturnOrderProducer updateReturnOrderProducer;
    @Mock
    private ReturnOrderSearchRequest searchRequestMock;
    @Mock
    private Page<ReturnOrderModel> page1Mock;
    @Mock
    private Page<ReturnOrderModel> page2Mock;
    @Mock
    private LoyaltyService loyaltyService;
    @Mock
    private InvoiceService invoiceService;
    @Mock
    private Populator<List<PaymentTransactionRequest>, OrderModel> orderPaymentTransactionRequestPopulator;
    @Mock
    private LoyaltyInvoiceProducerService loyaltyInvoiceProducerService;
    @Mock
    private RedisLockService redisLockService;
    @Mock
    private DelegatingDistributedLock lockMock;
    @Mock
    private ReturnOrdersProducerService returnOrdersProducerService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        facade = new ReturnOrderFacadeImpl();
        facade.setApplicationEventPublisher(applicationEventPublisher);
        facade.setBasicReturnOrderConverter(basicReturnOrderConverter);
        facade.setReturnOrderCommerceParameterConverter(returnOrderCommerceParameterConverter);
        facade.setReturnOrderService(returnOrderService);
        facade.setReturnOrderDetailPopulator(returnOrderDetailPopulator);
        facade.setOrderConverter(orderConverter);
        facade.setOrderService(orderService);
        facade.setGenerateCartCodeService(generateCartCodeService);
        facade.setCommerceCartService(commerceCartService);
        facade.setAuthService(authService);
        facade.setUpdateReturnOrderProducer(updateReturnOrderProducer);
        facade.setLoyaltyService(loyaltyService);
        facade.setInvoiceService(invoiceService);
        facade.setOrderPaymentTransactionRequestPopulator(orderPaymentTransactionRequestPopulator);
        facade.setLoyaltyInvoiceProducerService(loyaltyInvoiceProducerService);
        facade.setRedisLockService(redisLockService);
        facade.setReturnOrdersProducerService(returnOrdersProducerService);
        when(redisLockService.obtain(anyString())).thenReturn(lockMock);
        when(lockMock.tryLock()).thenReturn(true);
        when(authService.getCurrentUserId()).thenReturn(1l);
    }

    @Test
    public void create() {
        when(returnOrderCommerceParameterConverter.convert(returnOrderRequest))
                .thenReturn(parameter);
        when(returnOrderService.create(parameter)).thenReturn(returnOrderModel);

        facade.create(returnOrderRequest);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        ReturnOrderEvent event = captor.getValue();
        assertEquals(ReturnOrderEventType.CREATE, event.getEventType());
        assertNotNull(event.getReturnOrder());
        verify(basicReturnOrderConverter).convert(returnOrderModel);
    }

    @Test
    public void getDetail_invalidReturnOrderId() {
        try {
            facade.getDetail(1l, 1l);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_RETURN_ORDER_ID.code(), e.getCode());
        }
    }

    @Test
    public void getDetail() {
        when(returnOrderService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(returnOrderModel);
        when(basicReturnOrderConverter.convert(returnOrderModel)).thenReturn(returnOrderData);
        facade.getDetail(1l, 1l);
        verify(basicReturnOrderConverter).convert(returnOrderModel);
        verify(returnOrderDetailPopulator).populate(returnOrderModel, returnOrderData);

    }

    @Test
    public void createOrGetExchangeOrder_InvalidReturnOrderId() {
        try {
            when(returnOrderUpdateParameter.getCompanyId()).thenReturn(1l);
            when(returnOrderUpdateParameter.getReturnOrderId()).thenReturn(11l);
            when(returnOrderService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(null);

            facade.createOrGetExchangeOrder(returnOrderUpdateParameter);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_RETURN_ORDER_ID.code(), e.getCode());
        }
    }

    @Test
    public void createOrGetExchangeOrder_ExchangeOrderHasExisted() {
        when(returnOrderUpdateParameter.getCompanyId()).thenReturn(1l);
        when(returnOrderUpdateParameter.getReturnOrderId()).thenReturn(11l);
        when(returnOrderService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(returnOrderModel);
        when(returnOrderModel.getExchangeOrder()).thenReturn(exchangeOrder);

        facade.createOrGetExchangeOrder(returnOrderUpdateParameter);
        verify(orderConverter).convert(exchangeOrder);
    }

    @Test
    public void createOrGetExchangeOrder_InvalidReturnOrderId_HasNotOriginOrder() {
        try {
            when(returnOrderUpdateParameter.getCompanyId()).thenReturn(1l);
            when(returnOrderUpdateParameter.getReturnOrderId()).thenReturn(11l);
            when(returnOrderService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(returnOrderModel);
            when(returnOrderModel.getExchangeOrder()).thenReturn(null);
            when(returnOrderModel.getOriginOrder()).thenReturn(null);

            facade.createOrGetExchangeOrder(returnOrderUpdateParameter);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.RETURN_ORDER_HAS_NOT_ORIGIN_ORDER.code(), e.getCode());
        }
    }

    @Test
    public void createOrGetExchangeOrder_ShouldCreateNew() {
        when(returnOrderUpdateParameter.getCompanyId()).thenReturn(1l);
        when(returnOrderUpdateParameter.getReturnOrderId()).thenReturn(11l);
        when(returnOrderService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(returnOrderModel);
        when(returnOrderModel.getExchangeOrder()).thenReturn(null);
        when(returnOrderModel.getOriginOrder()).thenReturn(originOrder);
        when(orderService.save(any(OrderModel.class))).thenReturn(exchangeOrder);

        facade.createOrGetExchangeOrder(returnOrderUpdateParameter);
        verify(orderService, times(2)).save(any(OrderModel.class));
        verify(generateCartCodeService).generateCartCode(exchangeOrder);
        verify(orderConverter).convert(exchangeOrder);
    }

    @Test
    public void doChangeWarehouse_theSameWarehouse() {
        when(returnOrderUpdateParameter.getCompanyId()).thenReturn(1l);
        when(returnOrderUpdateParameter.getReturnOrderId()).thenReturn(11l);
        when(returnOrderService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(returnOrderModel);
        when(returnOrderModel.getExchangeOrder()).thenReturn(exchangeOrder);
        when(exchangeOrder.getWarehouseId()).thenReturn(17l);
        when(returnOrderUpdateParameter.getWarehouseId()).thenReturn(17l);

        facade.doChangeWarehouse(returnOrderUpdateParameter);
        verify(orderService, times(0)).save(exchangeOrder);
        verify(orderConverter).convert(exchangeOrder);
    }

    @Test
    public void doChangeWarehouse_WithEmptyOrderEntries() {
        when(returnOrderUpdateParameter.getCompanyId()).thenReturn(1l);
        when(returnOrderUpdateParameter.getReturnOrderId()).thenReturn(11l);
        when(returnOrderService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(returnOrderModel);
        when(returnOrderModel.getExchangeOrder()).thenReturn(exchangeOrder);
        when(exchangeOrder.getWarehouseId()).thenReturn(null);
        when(returnOrderUpdateParameter.getWarehouseId()).thenReturn(17l);
        when(exchangeOrder.getEntries()).thenReturn(null);

        facade.doChangeWarehouse(returnOrderUpdateParameter);
        verify(orderService, times(1)).save(exchangeOrder);
        verify(commerceCartService, times(0)).removeAllEntries(any(CommerceAbstractOrderParameter.class));
        verify(orderConverter).convert(exchangeOrder);
    }

    @Test
    public void doChangeWarehouse_WithNotEmptyOrderEntries_ShouldNotAccepted() {
        try {
            when(returnOrderUpdateParameter.getCompanyId()).thenReturn(1l);
            when(returnOrderUpdateParameter.getReturnOrderId()).thenReturn(11l);
            when(returnOrderService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(returnOrderModel);
            when(returnOrderModel.getExchangeOrder()).thenReturn(exchangeOrder);
            when(exchangeOrder.getWarehouseId()).thenReturn(12l);
            when(returnOrderUpdateParameter.getWarehouseId()).thenReturn(17l);
            when(exchangeOrder.getEntries()).thenReturn(Arrays.asList(new OrderEntryModel()));

            facade.doChangeWarehouse(returnOrderUpdateParameter);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CAN_NOT_CHANGE_TO_OTHER_WAREHOUSE.code(), e.getCode());
        }
    }

    @Test
    public void updateReport() {
        when(page1Mock.getContent()).thenReturn(Arrays.asList(new ReturnOrderModel()));
        when(page2Mock.getContent()).thenReturn(Collections.emptyList());
        when(returnOrderService.search(any(), any())).thenReturn(page1Mock, page2Mock);

        facade.updateReport(searchRequestMock);
        verify(updateReturnOrderProducer).process(any(ReturnOrderModel.class));
    }

    @Test
    public void updateInfo() {
        ReturnOrderRequest returnOrderRequest = new ReturnOrderRequest();
        returnOrderRequest.setId(2l);
        returnOrderRequest.setNote("note");
        returnOrderRequest.setVatNumber("123");
        returnOrderRequest.setOriginOrderCode("1233");
        returnOrderRequest.setCompanyId(1l);
        returnOrderRequest.setRefundAmount(1000d);
        OrderModel orderModel = new OrderModel();
        orderModel.setId(1l);
        orderModel.setCode("123");
        ReturnOrderModel returnOrder = new ReturnOrderModel();
        returnOrder.setId(1l);
        returnOrder.setExchangeOrder(orderModel);
        returnOrder.setRefundAmount(10000d);

        OrderModel originOrderModel = new OrderModel();
        originOrderModel.setId(2l);
        originOrderModel.setCode("1233");
        originOrderModel.setRefundAmount(10000d);
        AbstractOrderEntryModel entryModel = new AbstractOrderEntryModel();
        entryModel.setEntryNumber(0);
        entryModel.setQuantity(11L);
        entryModel.setReturnQuantity(10l);
        originOrderModel.setEntries(Arrays.asList(entryModel));

        returnOrder.setOriginOrder(originOrderModel);

        when(returnOrderService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(returnOrder);
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderModel);
        facade.updateInfo(returnOrderRequest);
        verify(returnOrderService).save(returnOrder);
        verify(orderService).save(orderModel);
        verify(basicReturnOrderConverter).convert(returnOrder);
        verify(loyaltyService).updateRefund(any(ReturnOrderModel.class));
        verify(invoiceService).updateRefundInvoice(any(ReturnOrderModel.class));
        assertEquals("note", returnOrder.getNote());
        assertEquals("123", orderModel.getVatNumber());
        assertEquals(1000d, returnOrder.getRefundAmount(), 0);
        assertEquals(1000d, returnOrder.getOriginOrder().getRefundAmount(), 0);

    }

    @Test
    public void getReturnRewardRedeem_HasNotLoyaltyTransaction() {
        ReturnOrderRequest returnOrderRequest = new ReturnOrderRequest();
        returnOrderRequest.setOriginOrderCode("1233");
        returnOrderRequest.setCompanyId(1l);
        when(returnOrderCommerceParameterConverter.convert(returnOrderRequest))
                .thenReturn(parameter);
        facade.getReturnRewardRedeem(returnOrderRequest);
        verify(returnOrderCommerceParameterConverter).convert(returnOrderRequest);
        verify(returnOrderService).getReturnRewardRedeem(parameter);
    }

    @Test
    public void updateInfo_cannot_change_refund_points() {
        try {
            ReturnOrderRequest returnOrderRequest = new ReturnOrderRequest();
            returnOrderRequest.setId(2l);
            returnOrderRequest.setNote("note");
            returnOrderRequest.setVatNumber("123");
            returnOrderRequest.setOriginOrderCode("1233");
            returnOrderRequest.setCompanyId(1l);
            returnOrderRequest.setRefundAmount(1000d);
            OrderModel orderModel = new OrderModel();
            orderModel.setId(1l);
            orderModel.setCode("123");
            AbstractOrderEntryModel entryModel = new AbstractOrderEntryModel();
            entryModel.setEntryNumber(0);
            entryModel.setQuantity(10L);
            entryModel.setReturnQuantity(10l);
            orderModel.setEntries(Arrays.asList(entryModel));
            ReturnOrderModel returnOrder = new ReturnOrderModel();
            returnOrder.setId(1l);
            returnOrder.setExchangeOrder(orderModel);
            returnOrder.setRefundAmount(10000d);
            returnOrder.setOriginOrder(orderModel);

            when(returnOrderService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(returnOrder);
            when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderModel);
            facade.updateInfo(returnOrderRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CAN_NOT_CHANGE_REFUND_POINTS.code(), e.getCode());
        }
    }

    @Test
    public void updateInfo_updateExchangeOrder() {
        ReturnOrderRequest returnOrderRequest = new ReturnOrderRequest();
        returnOrderRequest.setId(2l);
        returnOrderRequest.setNote("note");
        returnOrderRequest.setVatNumber("123");
        returnOrderRequest.setOriginOrderCode("1233");
        returnOrderRequest.setCompanyId(1l);
        returnOrderRequest.setRefundAmount(1000d);

        List<PaymentTransactionRequest> exchangePayments = new ArrayList<>();
        PaymentTransactionRequest payment = new PaymentTransactionRequest();
        payment.setAmount(20d);
        exchangePayments.add(payment);
        returnOrderRequest.setExchangePayments(exchangePayments);
        OrderModel orderModel = new OrderModel();
        orderModel.setId(1l);
        orderModel.setCode("123");
        orderModel.setCustomerId(2l);
        AbstractOrderEntryModel entryModel = new AbstractOrderEntryModel();
        entryModel.setEntryNumber(0);
        entryModel.setQuantity(10L);
        entryModel.setReturnQuantity(10l);
        orderModel.setEntries(Arrays.asList(entryModel));
        ReturnOrderModel returnOrder = new ReturnOrderModel();
        returnOrder.setId(1l);
        returnOrder.setExchangeOrder(orderModel);
        returnOrder.setOriginOrder(orderModel);

        when(returnOrderService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(returnOrder);
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderModel);
        when(loyaltyService.updateRedeem(any())).thenReturn(new TransactionData());
        facade.updateInfo(returnOrderRequest);
        verify(loyaltyService).updateRedeem(any(OrderModel.class));
        verify(invoiceService).saveInvoices(any(OrderModel.class), any());
        verify(orderService, times(2)).save(any(OrderModel.class));
    }

    @Test
    public void createRevenueReturnOrder_invalidOrderReturnModel() {
        try {
            ReturnOrderSearchRequest request = new ReturnOrderSearchRequest();
            request.setCompanyId(2l);
            request.setId(1234l);
            when(returnOrderService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(null);
            facade.createRevenueReturnOrder(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_RETURN_ORDER_ID.code(), e.getCode());
        }
    }

    @Test
    public void createRevenueReturnOrder_oneModel() {
        ReturnOrderSearchRequest request = new ReturnOrderSearchRequest();
        request.setCompanyId(2l);
        request.setId(1234l);
        ReturnOrderModel model = new ReturnOrderModel();
        when(returnOrderService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(model);
        facade.createRevenueReturnOrder(request);
        verify(returnOrdersProducerService).sendReturnOrdersKafka(model);
    }

    @Test
    public void createRevenueReturnOrder_allModel() {
        ReturnOrderSearchRequest request = new ReturnOrderSearchRequest();
        request.setCompanyId(2l);
        ReturnOrderModel model = new ReturnOrderModel();
        when(returnOrderService.findAllByCompanyId(anyLong())).thenReturn(Arrays.asList(model));
        facade.createRevenueReturnOrder(request);
        verify(returnOrderService).findAllByCompanyId(anyLong());
        verify(returnOrdersProducerService, times(1)).sendReturnOrdersKafka(model);
    }


    @Test
    public void getInfoVatOfReturnOrderWithOriginOrderCode() {
        OrderModel order = new OrderModel();
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(),anyLong(),anyBoolean())).thenReturn(order);
        order.setVat(30000d);
        order.setVatType(DiscountType.CASH.toString());
        order.setId(1l);
        when(returnOrderService.sumVatReturnOrderForOriginOrder(any())).thenReturn(20000d);
        ReturnOrderVatData vatData = facade.getInfoVatOfReturnOrderWithOriginOrderCode("123", 2l);
        assertEquals(30000d, vatData.getOriginOrderVat(), 0);
        assertEquals(10000d, vatData.getReturnOrderVat(), 0);
    }

    @Test
    public void updateRefundPoint() {
        ReturnOrderRequest returnOrderRequest = new ReturnOrderRequest();
        returnOrderRequest.setId(2l);
        returnOrderRequest.setOriginOrderCode("1233");
        returnOrderRequest.setCompanyId(1l);
        returnOrderRequest.setRefundAmount(15000d);
        OrderModel orderModel = new OrderModel();
        orderModel.setId(1l);
        orderModel.setCode("123");
        ReturnOrderModel returnOrder = new ReturnOrderModel();
        returnOrder.setId(1l);
        returnOrder.setExchangeOrder(orderModel);
        returnOrder.setRefundAmount(10000d);

        OrderModel originOrderModel = new OrderModel();
        originOrderModel.setId(2l);
        originOrderModel.setCode("1233");
        originOrderModel.setRefundAmount(10000d);
        AbstractOrderEntryModel entryModel = new AbstractOrderEntryModel();
        entryModel.setEntryNumber(0);
        entryModel.setQuantity(11L);
        entryModel.setReturnQuantity(10l);
        originOrderModel.setEntries(Arrays.asList(entryModel));

        returnOrder.setOriginOrder(originOrderModel);

        when(returnOrderService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(returnOrder);
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderModel);
        facade.updateRefundPoint(returnOrderRequest);
        verify(returnOrderService).save(returnOrder);
        verify(basicReturnOrderConverter).convert(returnOrder);
        verify(loyaltyService).updateRefund(any(ReturnOrderModel.class));
        verify(invoiceService).updateRefundInvoice(any(ReturnOrderModel.class));

    }
}
