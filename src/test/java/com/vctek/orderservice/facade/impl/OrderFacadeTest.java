package com.vctek.orderservice.facade.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vctek.converter.Converter;
import com.vctek.converter.Populator;
import com.vctek.exception.ServiceException;
import com.vctek.kafka.data.InvoiceKafkaData;
import com.vctek.orderservice.dto.*;
import com.vctek.orderservice.dto.excel.OrderItemDTO;
import com.vctek.orderservice.dto.request.*;
import com.vctek.orderservice.elasticsearch.service.OrderElasticSearchService;
import com.vctek.orderservice.excel.OrderItemExcelFileReader;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.PermissionFacade;
import com.vctek.orderservice.feignclient.dto.CustomerData;
import com.vctek.orderservice.feignclient.dto.InvoiceData;
import com.vctek.orderservice.kafka.producer.OrderProducerService;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.repository.SubOrderEntryRepository;
import com.vctek.orderservice.service.*;
import com.vctek.orderservice.strategy.CommerceChangeOrderStatusStrategy;
import com.vctek.orderservice.strategy.CommercePlaceOrderStrategy;
import com.vctek.orderservice.util.BillStatus;
import com.vctek.orderservice.util.CurrencyType;
import com.vctek.orderservice.util.PriceType;
import com.vctek.orderservice.util.SellSignal;
import com.vctek.redis.elastic.ProductSearchData;
import com.vctek.sync.MutexFactory;
import com.vctek.sync.XMutexFactoryImpl;
import com.vctek.util.*;
import com.vctek.validate.Validator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.cluster.lock.support.DelegatingDistributedLock;
import org.springframework.cloud.cluster.redis.lock.RedisLockService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class OrderFacadeTest {
    @Mock
    private CommercePlaceOrderStrategy commercePlaceOrderStrategy;
    @Mock
    private Converter<OrderRequest, CommerceCheckoutParameter> commerceCheckoutParameterConverter;
    @Mock
    private Converter<OrderRequest, UpdateOrderParameter> updateOrderParameterConverter;
    @Mock
    private Converter<OrderModel, OrderData> orderConverter;
    @Mock
    protected Converter<AbstractOrderEntryModel, OrderEntryData> orderEntryConverter;
    @Mock
    private Converter<OrderEntryDTO, CommerceAbstractOrderParameter> commerceOrderParameterConverter;
    @Mock
    private OrderService orderService;
    @Mock
    private CommerceCartService commerceCartService;
    @Mock
    private OrderRequest orderRequest;
    @Mock
    private CommerceCheckoutParameter parameter;
    @Mock
    private CommerceOrderResult commerceOrderResult;
    @Mock
    private OrderEntryDTO orderEntryDTO;
    @Mock
    private CommerceAbstractOrderParameter orderParameter;
    @Mock
    private CommerceCartModification commerceCartModification;
    @Mock
    private OrderEntryModel entryModel;
    @Mock
    private CartDiscountRequest cartDiscountRequest;
    @Mock
    private VatRequest vatRequest;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private UpdateOrderParameter updateOrderParameter;

    @Mock
    private OrderHistoryService orderHistoryService;

    @Mock
    private ChangeOrderStatusRequest changeOrderStatusRequest;

    @Mock
    private OrderProducerService orderProducerService;

    @Mock
    private CommerceChangeOrderStatusStrategy changeOrderStatusStrategy;

    @Mock
    private ToppingOptionService toppingOptionService;

    @Mock
    private Validator<List<OrderItemDTO>> importOrderItemValidator;

    private ArgumentCaptor<OrderModel> captor;

    private OrderFacadeImpl facade;

    private OrderModel order;
    @Mock
    private CommerceChangeOrderStatusModification changeStatusModificationMock;
    @Mock
    private PermissionFacade permissionFacade;
    @Mock
    private RedisLockService redisLockService;
    @Mock
    private DelegatingDistributedLock lockMock;
    @Mock
    private AppliedCouponRequest appliedCouponRequest;
    @Mock
    private CouponService couponServiceMock;

    private AddSubOrderEntryRequest addSubOrderEntryRequest;

    @Mock
    private MultipartFile multiplePartFileMock;
    @Mock
    private OrderItemExcelFileReader orderItemExcelFileReader;
    @Mock
    private Populator<AbstractOrderItemImportParameter, AbstractOrderModel> orderEntriesPopulator;

    private MutexFactory<String> mutexFactory = new XMutexFactoryImpl<>();
    @Mock
    private SaleQuantityRequest saleRequestMock;
    @Mock
    private OrderEntryModel entryMock1;

    @Mock
    private OrderEntryModel entryMock2;
    @Mock
    private OrderEntryModel entryMock3;

    private ToppingOptionRequest toppingOptionRequest;

    private ToppingItemRequest toppingItemRequest;
    @Mock
    private ToppingItemService toppingItemService;

    @Mock
    private Populator<ToppingItemRequest, ToppingItemParameter> toppingItemOrderParameterPopulator;
    @Mock
    private ToppingItemModification toppingItemModificationMock;
    @Mock
    private ToppingOptionModification toppingOptionModificationMock;
    @Mock
    private OrderElasticSearchService orderElasticSearchService;
    private RefreshCartRequest refreshCartRequest;
    @Mock
    private SaleQuantity saleEntryMock1;
    @Mock
    private SaleQuantity saleEntryMock2;
    @Mock
    private SaleQuantity saleEntryMock3;

    @Mock
    private InvoiceService invoiceService;

    private Set<PaymentTransactionModel> payments = new HashSet<>();

    @Mock
    private ProductService productService;

    @Mock
    private UpdateOrderSequenceCacheService updateOrderSequenceCacheService;
    @Mock
    private SubOrderEntryRepository subOrderEntryRepository;
    @Mock
    private InventoryService inventoryService;
    @Mock
    private AuthService authService;
    @Mock
    private PaymentTransactionService paymentTransactionService;
    @Mock
    private LoyaltyService loyaltyService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private Validator<CommerceAbstractOrderParameter> saleOffOrderEntryValidator;
    @Mock
    private Validator<CommerceAbstractOrderParameter> saleOffUpdateQuantityOrderEntryValidator;
    @Mock
    private CustomerService customerService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        order = new OrderModel();
        order.setCompanyId(1l);
        captor = ArgumentCaptor.forClass(OrderModel.class);
        addSubOrderEntryRequest = new AddSubOrderEntryRequest();
        refreshCartRequest = new RefreshCartRequest();
        facade = new OrderFacadeImpl();
        facade.setCommerceCartService(commerceCartService);
        facade.setCommerceCheckoutParameterConverter(commerceCheckoutParameterConverter);
        facade.setCommerceOrderParameterConverter(commerceOrderParameterConverter);
        facade.setCommercePlaceOrderStrategy(commercePlaceOrderStrategy);
        facade.setOrderConverter(orderConverter);
        facade.setUpdateOrderParameterConverter(updateOrderParameterConverter);
        facade.setOrderService(orderService);
        facade.setApplicationEventPublisher(applicationEventPublisher);
        facade.setOrderHistoryService(orderHistoryService);
        facade.setProducerService(orderProducerService);
        facade.setCommerceChangeOrderStatusStrategy(changeOrderStatusStrategy);
        facade.setPermissionFacade(permissionFacade);
        facade.setRedisLockService(redisLockService);
        facade.setCouponService(couponServiceMock);
        facade.setOrderItemExcelFileReader(orderItemExcelFileReader);
        facade.setOrderEntriesPopulator(orderEntriesPopulator);
        facade.setMutexFactory(mutexFactory);
        facade.setToppingOptionService(toppingOptionService);
        facade.setOrderEntryConverter(orderEntryConverter);
        facade.setToppingOptionService(toppingOptionService);
        facade.setImportOrderItemValidator(importOrderItemValidator);
        facade.setToppingItemOrderParameterPopulator(toppingItemOrderParameterPopulator);
        facade.setOrderElasticSearchService(orderElasticSearchService);
        facade.setInvoiceService(invoiceService);
        facade.setProductService(productService);
        facade.setUpdateOrderSequenceCacheService(updateOrderSequenceCacheService);
        facade.setSubOrderEntryService(subOrderEntryRepository);
        facade.setInventoryService(inventoryService);
        facade.setAuthService(authService);
        facade.setPaymentTransactionService(paymentTransactionService);
        facade.setLoyaltyService(loyaltyService);
        facade.setObjectMapper(objectMapper);
        facade.setSaleOffOrderEntryValidator(saleOffOrderEntryValidator);
        facade.setSaleOffUpdateQuantityOrderEntryValidator(saleOffUpdateQuantityOrderEntryValidator);
        facade.setCustomerService(customerService);

        when(redisLockService.obtain(anyString())).thenReturn(lockMock);
        when(lockMock.tryLock()).thenReturn(true);
        when(updateOrderSequenceCacheService.isValidTimeRequest(any(), any(), any(), any())).thenReturn(true);
        when(orderEntryDTO.getEntryId()).thenReturn(1l);
        addSubOrderEntryRequest.setOrderCode("123");
        addSubOrderEntryRequest.setComboId(1l);
        addSubOrderEntryRequest.setCompanyId(1l);
        addSubOrderEntryRequest.setEntryId(0l);
        addSubOrderEntryRequest.setProductId(1l);

        toppingOptionRequest = new ToppingOptionRequest();
        toppingOptionRequest.setQuantity(1);
        toppingOptionRequest.setSugar(100);
        toppingOptionRequest.setIce(100);
        toppingOptionRequest.setEntryId(1l);
        toppingOptionRequest.setCompanyId(1l);

        toppingItemRequest = new ToppingItemRequest();
        toppingItemRequest.setId(1l);
        toppingItemRequest.setPrice(20000d);
        toppingItemRequest.setQuantity(1);
        toppingItemRequest.setCompanyId(1l);
        toppingItemRequest.setDiscount(10000d);
        toppingItemRequest.setDiscountType(CurrencyType.CASH.toString());
        toppingItemRequest.setOrderCode("123");
        toppingItemRequest.setEntryId(1l);
        toppingItemRequest.setToppingOptionId(1l);

        addSubOrderEntryRequest.setProductId(1l);
        addSubOrderEntryRequest.setOrderCode("123");
        addSubOrderEntryRequest.setComboId(1l);
        addSubOrderEntryRequest.setEntryId(1l);
        addSubOrderEntryRequest.setCompanyId(1l);

        refreshCartRequest.setCode("orderCode");
        refreshCartRequest.setCompanyId(1l);
        refreshCartRequest.setOldCompanyId(1l);
        refreshCartRequest.setWarehouseId(17l);
    }

    @Test
    public void placeOrderWidthTypeRetail() {
        order.setType(OrderType.RETAIL.toString());
        List<AbstractOrderEntryModel> entryModels = new ArrayList<>();
        AbstractOrderEntryModel entryModel = new AbstractOrderEntryModel();
        entryModel.setProductId(1L);
        entryModel.setQuantity(2L);
        entryModels.add(entryModel);
        order.setEntries(entryModels);

        when(commerceCheckoutParameterConverter.convert(orderRequest)).thenReturn(parameter);
        when(commercePlaceOrderStrategy.placeOrder(parameter)).thenReturn(commerceOrderResult);
        when(commerceOrderResult.getOrderModel()).thenReturn(order);

        facade.placeOrder(orderRequest);
        verify(orderConverter).convert(order);
    }

    @Test
    public void placeOrder() {
        when(commerceCheckoutParameterConverter.convert(orderRequest)).thenReturn(parameter);
        when(commercePlaceOrderStrategy.placeOrder(parameter)).thenReturn(commerceOrderResult);
        when(commerceOrderResult.getOrderModel()).thenReturn(order);

        facade.placeOrder(orderRequest);
        verify(orderConverter).convert(order);
    }

    @Test
    public void findByOrderCodeAndOrderTypeCompanyId() {
        when(orderService.findByCodeAndCompanyIdAndOrderTypeAndDeleted(anyString(), anyLong(), anyString(), anyBoolean())).thenReturn(order);

        facade.findBy("orderCode", 1l, OrderType.RETAIL.toString(), false);
        verify(orderConverter).convert(order);
    }

    @Test
    public void findByOrderCodeAndCompanyId() {
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(order);

        facade.findBy("orderCode", 1l, null, true);
        verify(orderConverter).convert(order);
    }

    @Test
    public void addEntryToOrder() {
        when(commerceOrderParameterConverter.convert(orderEntryDTO)).thenReturn(orderParameter);
        when(commerceCartService.addEntryToOrder(orderParameter)).thenReturn(commerceCartModification);
        when(orderParameter.getOrder()).thenReturn(order);
        when(commerceCartModification.getEntry()).thenReturn(entryModel);
        when(commerceCartModification.getOrder()).thenReturn(order);

        facade.addEntryToOrder(orderEntryDTO);
        verify(orderConverter).convert(order);
    }

    @Test
    public void addEntryToOrderWidthTypeRetail() {
        when(commerceOrderParameterConverter.convert(orderEntryDTO)).thenReturn(orderParameter);
        when(commerceCartService.addEntryToOrder(orderParameter)).thenReturn(commerceCartModification);
        when(orderParameter.getOrder()).thenReturn(order);
        when(commerceCartModification.getEntry()).thenReturn(entryModel);
        when(commerceCartModification.getOrder()).thenReturn(order);

        order.setType(OrderType.RETAIL.toString());
        facade.addEntryToOrder(orderEntryDTO);
        verify(orderConverter).convert(order);
    }

    @Test
    public void updateEntryOrder_qtyIs0() {
        when(orderEntryDTO.getQuantity()).thenReturn(0l);
        when(orderEntryDTO.getOrderCode()).thenReturn("code");
        when(orderEntryDTO.getCompanyId()).thenReturn(1l);
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(order);
        when(commerceCartService.updateQuantityForCartEntry(any(CommerceAbstractOrderParameter.class))).thenReturn(commerceCartModification);
        when(commerceCartModification.getOrder()).thenReturn(order);

        facade.updateEntry(orderEntryDTO);
        verify(commerceCartService).updateOrderEntry(any(CommerceAbstractOrderParameter.class));
        verify(orderConverter).convert(order);
    }

    @Test
    public void updateEntryOrder_qtyIsLargerThan0() {
        when(orderEntryDTO.getQuantity()).thenReturn(10l);
        when(orderEntryDTO.getOrderCode()).thenReturn("code");
        when(orderEntryDTO.getCompanyId()).thenReturn(1l);
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(order);
        when(commerceCartModification.getEntry()).thenReturn(entryModel);
        when(commerceCartService.updateQuantityForCartEntry(any(CommerceAbstractOrderParameter.class))).thenReturn(commerceCartModification);
        when(commerceCartModification.getOrder()).thenReturn(order);

        facade.updateEntry(orderEntryDTO);
        verify(commerceCartService).updateOrderEntry(any(CommerceAbstractOrderParameter.class));
        verify(orderConverter).convert(order);
    }

    @Test
    public void updateEntryOrder_WidthTypeRetail_qtyIsLargerThan0() {
        when(orderEntryDTO.getQuantity()).thenReturn(10l);
        when(orderEntryDTO.getOrderCode()).thenReturn("code");
        when(orderEntryDTO.getCompanyId()).thenReturn(1l);
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(order);
        when(commerceCartModification.getEntry()).thenReturn(entryModel);
        when(commerceCartService.updateQuantityForCartEntry(any(CommerceAbstractOrderParameter.class))).thenReturn(commerceCartModification);
        when(commerceCartModification.getOrder()).thenReturn(order);

        order.setType(OrderType.RETAIL.toString());
        facade.updateEntry(orderEntryDTO);
        verify(commerceCartService).updateOrderEntry(any(CommerceAbstractOrderParameter.class));
        verify(orderConverter).convert(order);
    }

    @Test
    public void updateEntryOrder_Online_OrderStatusLessThan_Shipping() {
        when(orderEntryDTO.getQuantity()).thenReturn(10l);
        when(orderEntryDTO.getOrderCode()).thenReturn("code");
        when(orderEntryDTO.getCompanyId()).thenReturn(1l);
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(order);
        when(commerceCartModification.getEntry()).thenReturn(entryModel);
        when(commerceCartService.updateQuantityForCartEntry(any(CommerceAbstractOrderParameter.class))).thenReturn(commerceCartModification);
        order.setType(OrderType.ONLINE.toString());
        order.setOrderStatus(OrderStatus.NEW.code());
        when(commerceCartModification.getOrder()).thenReturn(order);

        facade.updateEntry(orderEntryDTO);
        verify(commerceCartService).updateOrderEntry(any(CommerceAbstractOrderParameter.class));
        verify(orderConverter).convert(order);
    }

    @Test
    public void updateEntryOrder_Online_OrderStatusGreater_Shipping() {
        when(orderEntryDTO.getQuantity()).thenReturn(10l);
        when(orderEntryDTO.getOrderCode()).thenReturn("code");
        when(orderEntryDTO.getCompanyId()).thenReturn(1l);
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(order);
        when(commerceCartModification.getEntry()).thenReturn(entryModel);
        order.setType(OrderType.ONLINE.toString());
        order.setOrderStatus(OrderStatus.SHIPPING.code());
        when(commerceCartModification.getOrder()).thenReturn(order);

        facade.updateEntry(orderEntryDTO);
        verify(commerceCartService).updateOrderEntry(any(CommerceAbstractOrderParameter.class));
        verify(orderConverter).convert(order);
    }

    @Test
    public void updateDiscountOfEntry_invalidDiscountType() {
        try {
            when(orderEntryDTO.getOrderCode()).thenReturn("code");
            when(orderEntryDTO.getCompanyId()).thenReturn(1l);
            when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(order);

            facade.updateDiscountOfEntry(orderEntryDTO);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_DISCOUNT_TYPE.code(), e.getCode());
        }
    }

    @Test
    public void updateDiscountOfEntry() {
        when(orderEntryDTO.getOrderCode()).thenReturn("code");
        when(orderEntryDTO.getCompanyId()).thenReturn(1l);
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(order);
        when(orderEntryDTO.getDiscountType()).thenReturn(CurrencyType.PERCENT.toString());
        when(commerceCartModification.getOrder()).thenReturn(order);

        facade.updateDiscountOfEntry(orderEntryDTO);
        verify(commerceCartService).updateDiscountForOrderEntry(any(CommerceAbstractOrderParameter.class));
        verify(orderConverter).convert(order);
    }

    @Test
    public void updateDiscountOfOrder_invalidDiscountType() {
        try {
            when(cartDiscountRequest.getCode()).thenReturn("code");
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);

            facade.updateDiscountOfOrder(cartDiscountRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_DISCOUNT_TYPE.code(), e.getCode());
        }
    }

    @Test
    public void updateDiscountOfOrder() {
        when(cartDiscountRequest.getCode()).thenReturn("code");
        when(cartDiscountRequest.getDiscountType()).thenReturn(CurrencyType.PERCENT.toString());
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
        when(commerceCartModification.getOrder()).thenReturn(order);

        facade.updateDiscountOfOrder(cartDiscountRequest);
        verify(commerceCartService).updateDiscountForOrder(any(CommerceAbstractOrderParameter.class));
        verify(orderConverter).convert(order);
    }

    @Test
    public void updateVatOfOrder_invalidVatType() {
        try {
            when(vatRequest.getCode()).thenReturn("code");
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);

            facade.updateVatOfOrder(vatRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_VAT_TYPE.code(), e.getCode());
        }
    }

    @Test
    public void updateVatOfOrder() {
        when(vatRequest.getCode()).thenReturn("code");
        when(vatRequest.getVatType()).thenReturn(CurrencyType.PERCENT.toString());
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);

        facade.updateVatOfOrder(vatRequest);
        verify(commerceCartService).updateVatForCart(any(CommerceAbstractOrderParameter.class));
        verify(orderConverter).convert(order);
    }

    @Test
    public void updateOrderInfo() {
        when(updateOrderParameterConverter.convert(orderRequest)).thenReturn(updateOrderParameter);
        when(commercePlaceOrderStrategy.updateOrder(updateOrderParameter)).thenReturn(commerceOrderResult);
        when(commerceOrderResult.getOrderModel()).thenReturn(order);

        facade.updateOrderInfo(orderRequest);
        verify(commercePlaceOrderStrategy).updateOrder(updateOrderParameter);
    }

    @Test
    public void updateOrderInfo_online() {
        when(orderRequest.getOrderType()).thenReturn(OrderType.ONLINE.name());
        when(updateOrderParameterConverter.convert(orderRequest)).thenReturn(updateOrderParameter);
        when(commercePlaceOrderStrategy.updateCustomerInfoInOnlineOrder(updateOrderParameter)).thenReturn(commerceOrderResult);
        when(commerceOrderResult.getOrderModel()).thenReturn(order);

        facade.updateInfoOnlineOrder(orderRequest);
        verify(commercePlaceOrderStrategy).updateCustomerInfoInOnlineOrder(updateOrderParameter);
    }

    @Test
    public void updatePriceOrderEntry_errorPriceRetail_smallThanWholesale() {
        try {
            order.setType(OrderType.ONLINE.name());
            order.setPriceType(PriceType.RETAIL_PRICE.name());
            ProductSearchData searchData = new ProductSearchData();
            searchData.setWholesalePrice(12d);

            when(orderEntryDTO.getOrderCode()).thenReturn("code");
            when(orderEntryDTO.getProductId()).thenReturn(1l);
            when(orderEntryDTO.getPrice()).thenReturn(10d);
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
            when(productService.search(any())).thenReturn(Arrays.asList(searchData));
            facade.updatePriceOrderEntry(orderEntryDTO);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.RETAIL_PRICE_MUST_BE_LARGE_WHOLESALE_PRICE.code(), e.getCode());
        }
    }

    @Test
    public void updatePriceOrderEntry() {
        when(orderEntryDTO.getOrderCode()).thenReturn("code");
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);

        facade.updatePriceOrderEntry(orderEntryDTO);
        verify(commerceCartService).updatePriceForCartEntry(any(CommerceAbstractOrderParameter.class));
    }

    @Test
    public void changeStatusOrder_InvalidOrder() {
        try {
            when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(null);
            facade.changeStatusOrder(changeOrderStatusRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_ID.code(), e.getCode());
        }
    }

    @Test
    public void changeStatusOrder_EmptyOlStatus_ThrowInvalidOrderStatus() {
        try {
            when(changeOrderStatusRequest.getOrderCode()).thenReturn("code");
            when(changeOrderStatusRequest.getCompanyId()).thenReturn(1l);
            when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(order);
            when(changeOrderStatusRequest.getOrderStatus()).thenReturn(null);
            facade.changeStatusOrder(changeOrderStatusRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_STATUS_CHANGE.code(), e.getCode());
        }
    }

    @Test
    public void changeStatusOrder_OldAndNewStatusTheSame_ThrowInvalidOrderStatus() {
        try {
            when(changeOrderStatusRequest.getOrderCode()).thenReturn("code");
            when(changeOrderStatusRequest.getCompanyId()).thenReturn(1l);
            when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(order);
            when(changeOrderStatusRequest.getOrderStatus()).thenReturn(OrderStatus.NEW.code());
            order.setOrderStatus(OrderStatus.NEW.code());
            facade.changeStatusOrder(changeOrderStatusRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_STATUS_CHANGE.code(), e.getCode());
        }
    }


    @Test
    public void changeStatusOrder_CompletedToNew_NotAcceptedWithNoPermission() {
        try {
            when(changeOrderStatusRequest.getOrderCode()).thenReturn("code");
            when(changeOrderStatusRequest.getCompanyId()).thenReturn(1l);
            when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(order);
            when(changeOrderStatusRequest.getOrderStatus()).thenReturn(OrderStatus.NEW.code());
            order.setOrderStatus(OrderStatus.COMPLETED.code());
            when(permissionFacade.hasPermission(PermissionCodes.CHANGE_ORDER_STATUS_COMPLETED.code(),
                    order.getCompanyId())).thenReturn(false);
            facade.changeStatusOrder(changeOrderStatusRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CAN_NOT_CHANGE_COMPLETED_STATUS.code(), e.getCode());
        }
    }

    @Test
    public void changeStatusOrder_SystemCancelToNew_NotAcceptedWithNoPermission() {
        try {
            when(changeOrderStatusRequest.getOrderCode()).thenReturn("code");
            when(changeOrderStatusRequest.getCompanyId()).thenReturn(1l);
            when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(order);
            when(changeOrderStatusRequest.getOrderStatus()).thenReturn(OrderStatus.NEW.code());
            order.setOrderStatus(OrderStatus.SYSTEM_CANCEL.code());
            when(permissionFacade.hasPermission(PermissionCodes.CHANGE_ORDER_STATUS_COMPLETED.code(),
                    order.getCompanyId())).thenReturn(false);
            when(permissionFacade.hasPermission(PermissionCodes.EDIT_COMPLETED_ONLINE_ORDER.code(),
                    order.getCompanyId())).thenReturn(false);
            facade.changeStatusOrder(changeOrderStatusRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CAN_NOT_CHANGE_COMPLETED_STATUS.code(), e.getCode());
        }
    }

    @Test
    public void changeStatusOrder_CustomerCancelToNew_NotAcceptedWithNoPermission() {
        try {
            when(changeOrderStatusRequest.getOrderCode()).thenReturn("code");
            when(changeOrderStatusRequest.getCompanyId()).thenReturn(1l);
            when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(order);
            when(changeOrderStatusRequest.getOrderStatus()).thenReturn(OrderStatus.NEW.code());
            order.setOrderStatus(OrderStatus.CUSTOMER_CANCEL.code());
            when(permissionFacade.hasPermission(PermissionCodes.CHANGE_ORDER_STATUS_COMPLETED.code(),
                    order.getCompanyId())).thenReturn(false);
            facade.changeStatusOrder(changeOrderStatusRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CAN_NOT_CHANGE_COMPLETED_STATUS.code(), e.getCode());
        }
    }

    @Test
    public void changeStatusOrder_OrderReturnToCompleted_NotAcceptedWithNoPermission() {
        try {
            when(changeOrderStatusRequest.getOrderCode()).thenReturn("code");
            when(changeOrderStatusRequest.getCompanyId()).thenReturn(1l);
            when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(order);
            when(changeOrderStatusRequest.getOrderStatus()).thenReturn(OrderStatus.COMPLETED.code());
            order.setOrderStatus(OrderStatus.ORDER_RETURN.code());
            when(permissionFacade.hasPermission(PermissionCodes.CHANGE_ORDER_STATUS_COMPLETED.code(),
                    order.getCompanyId())).thenReturn(false);
            facade.changeStatusOrder(changeOrderStatusRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CAN_NOT_CHANGE_COMPLETED_STATUS.code(), e.getCode());
        }
    }

    @Test
    public void changeStatusOrder_OrderReturnToCompleted_HasPermissionToChangeCompletedOrder() {
        when(changeOrderStatusRequest.getOrderCode()).thenReturn("code");
        when(changeOrderStatusRequest.getCompanyId()).thenReturn(1l);
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(order);
        when(changeOrderStatusRequest.getOrderStatus()).thenReturn(OrderStatus.COMPLETED.code());
        order.setOrderStatus(OrderStatus.ORDER_RETURN.code());
        when(permissionFacade.hasPermission(PermissionCodes.CHANGE_ORDER_STATUS_COMPLETED.code(),
                order.getCompanyId())).thenReturn(true);
        when(permissionFacade.hasPermission(PermissionCodes.EDIT_COMPLETED_ONLINE_ORDER.code(),
                order.getCompanyId())).thenReturn(false);
        facade.changeStatusOrder(changeOrderStatusRequest);
        verify(changeOrderStatusStrategy).changeStatusOrder(any(CommerceChangeOrderStatusParameter.class));
    }

    @Test
    public void changeStatusOrder_OrderReturnToCompleted_HasPermissionToEditCompletedOrder() {
        when(changeOrderStatusRequest.getOrderCode()).thenReturn("code");
        when(changeOrderStatusRequest.getCompanyId()).thenReturn(1l);
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(order);
        when(changeOrderStatusRequest.getOrderStatus()).thenReturn(OrderStatus.COMPLETED.code());
        order.setOrderStatus(OrderStatus.ORDER_RETURN.code());
        when(permissionFacade.hasPermission(PermissionCodes.CHANGE_ORDER_STATUS_COMPLETED.code(),
                order.getCompanyId())).thenReturn(false);
        when(permissionFacade.hasPermission(PermissionCodes.EDIT_COMPLETED_ONLINE_ORDER.code(),
                order.getCompanyId())).thenReturn(true);
        facade.changeStatusOrder(changeOrderStatusRequest);
        verify(changeOrderStatusStrategy).changeStatusOrder(any(CommerceChangeOrderStatusParameter.class));
    }

    @Test
    public void changeStatusOrder_PreOrder_Contain_FnB() {
        changeOrderStatusRequest.setOrderCode("code");
        changeOrderStatusRequest.setCompanyId(1l);
        when(changeOrderStatusRequest.getOrderCode()).thenReturn("code");
        when(changeOrderStatusRequest.getCompanyId()).thenReturn(1l);
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(order);
        when(changeOrderStatusRequest.getOrderStatus()).thenReturn(OrderStatus.PRE_ORDER.code());
        order.setOrderStatus(OrderStatus.NEW.code());
        order.setEntries(Arrays.asList(entryMock1));
        when(orderService.isComboEntry(entryMock1)).thenReturn(false);
        when(entryMock1.getProductId()).thenReturn(1L);
        when(productService.isFnB(1L)).thenReturn(true);
        try {
            facade.changeStatusOrder(changeOrderStatusRequest);
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CANNOT_PRE_ORDER_CONTAIN_FOOD_BEVERAGE_ENTRY.code(), e.getCode());
        }
    }


    @Test
    public void holdingProduct() {
        HoldingProductRequest request = new HoldingProductRequest();
        request.setOrderCode("2341");
        request.setCompanyId(1l);
        List<HoldingData> holdingDataList = new ArrayList<>();
        HoldingData holdingData = new HoldingData();
        holdingData.setProductId(1l);
        holdingData.setQuantity(1l);
        holdingDataList.add(holdingData);
        request.setHoldingDataList(holdingDataList);
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), eq(false))).thenReturn(order);
        List<AbstractOrderEntryModel> abstractOrderEntryModels = new ArrayList<>();
        AbstractOrderEntryModel abstractOrderEntryModel = new AbstractOrderEntryModel();
        abstractOrderEntryModel.setId(1l);
        abstractOrderEntryModel.setProductId(1l);
        abstractOrderEntryModel.setQuantity(1l);
        abstractOrderEntryModel.setHolding(true);
        abstractOrderEntryModel.setPreOrder(true);
        abstractOrderEntryModel.setHoldingStock(1l);
        abstractOrderEntryModels.add(abstractOrderEntryModel);
        order.setId(1l);
        order.setEntries(abstractOrderEntryModels);
        facade.holdingProductOfOrder(request);
        verify(commerceCartService).holdingProductOfOrder(request, order);
    }

    @Test
    public void remove_invalidCode() {
        try {
            when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(null);
            facade.remove("1234", 1l);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_CODE.code(), e.getCode());
        }
    }

    @Test
    public void remove_success() {
        OrderModel orderModel = new OrderModel();
        orderModel.setId(1l);
        orderModel.setOrderStatus(OrderStatus.NEW.code());
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderModel);
        facade.remove("123412", 1l);
        verify(commerceCartService).removeOrder(captor.capture());
    }

    @Test
    public void remove_success_online_order_has_redeem() {
        OrderModel orderModel = new OrderModel();
        orderModel.setId(1l);
        orderModel.setRedeemAmount(1d);
        orderModel.setType(OrderType.ONLINE.toString());
        orderModel.setOrderStatus(OrderStatus.NEW.code());
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderModel);
        facade.remove("123412", 1l);
        verify(commerceCartService).removeOrder(captor.capture());
        verify(loyaltyService).cancelPendingRedeem(orderModel);
    }

    @Test
    public void removeList_Not_Accept_Modified_Order() {
        try {
            OrderModel orderModel = new OrderModel();
            orderModel.setId(1l);
            orderModel.setCode(OrderStatus.PRE_ORDER.code());
            orderModel.setDeleted(false);

            when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderModel);
            facade.remove("123412", 1l);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.NOT_ACCEPT_MODIFIED_ORDER.code(), e.getCode());
        }
    }

    @Test
    public void removeOrder_withInvoiceVerified() {
        try {
            OrderModel orderModel = new OrderModel();
            orderModel.setId(29L);
            orderModel.setType(OrderType.ONLINE.toString());
            orderModel.setOrderStatus(OrderStatus.NEW.code());

            InvoiceData invoiceData = new InvoiceData();
            invoiceData.setId(1L);
            invoiceData.setStatus(BillStatus.VERIFIED.code());
            invoiceData.setFinalAmount(2000d);
            InvoiceData invoiceData1 = new InvoiceData();
            invoiceData1.setId(2L);
            invoiceData1.setStatus(BillStatus.PENDING_FOR_VERIFIED.code());
            invoiceData1.setFinalAmount(22000d);
            List<InvoiceData> dataList = new ArrayList<>();
            dataList.add(invoiceData);
            dataList.add(invoiceData1);
            when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderModel);
            when(invoiceService.findAllOrderInvoices(anyLong(), anyString(), any(), any())).thenReturn(dataList);
            facade.remove("code", 2L);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_INVOICE_VERIFIED.code(), e.getCode());
        }
    }

    @Test
    public void updateWeightForOrderEntry() {
        when(orderEntryDTO.getOrderCode()).thenReturn("1234");
        when(orderEntryDTO.getEntryId()).thenReturn(1l);
        when(orderEntryDTO.getWeight()).thenReturn(1.1);
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);

        facade.updateWeightForOrderEntry(orderEntryDTO);
        verify(commerceCartService).updateWeightForOrderEntry(any(CommerceAbstractOrderParameter.class));
    }

    @Test
    public void changeOrderToRetail() {
        try {
            when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(null);
            facade.changeOrderToRetail("1234", 1l);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_ID.code(), e.getCode());
        }
    }

    @Test
    public void changeOrderToRetail_sellSignal_ECOMMERCE_WEB() {
        try {
            order.setType(OrderType.ONLINE.toString());
            order.setSellSignal(SellSignal.ECOMMERCE_WEB.name());
            when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(order);
            facade.changeOrderToRetail("1234", 1l);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CAN_NOT_CHANGE_TO_RETAIL_STATUS_ECOMMERCE_WEB.code(), e.getCode());
        }
    }

    @Test
    public void changeOrderToRetail_case() {
        try {
            order.setType(OrderType.RETAIL.toString());
            when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(order);
            facade.changeOrderToRetail("1234", 1l);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.ONLY_ONLINE_TYPE_CAN_CHANGE_TO_RETAIL.code(), e.getCode());
        }
    }

    @Test
    public void changeOrderToRetail_success() {
        order.setType(OrderType.ONLINE.toString());
        order.setOrderStatus(OrderStatus.COMPLETED.code());
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(order);
        when(changeOrderStatusStrategy.changeToHigherStatus(any(CommerceChangeOrderStatusParameter.class)))
                .thenReturn(changeStatusModificationMock);
        when(changeStatusModificationMock.getRetailOrderCode()).thenReturn("10002");
        when(permissionFacade.hasPermission(PermissionCodes.CHANGE_ORDER_STATUS_COMPLETED.code(),
                order.getCompanyId())).thenReturn(true);
        facade.changeOrderToRetail("1234", 1l);
        verify(changeOrderStatusStrategy).changeToHigherStatus(any(CommerceChangeOrderStatusParameter.class));
    }

    @Test
    public void updateNoteInOrder_invalidCode() {
        NoteRequest noteRequest = new NoteRequest();
        noteRequest.setOrderCode("123");
        noteRequest.setCustomerNote("customer");
        noteRequest.setNote("note");
        try {
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(null);
            facade.updateNoteInOrder(noteRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_CODE.code(), e.getCode());
        }
    }

    @Test
    public void updateNoteInOrder() {
        NoteRequest noteRequest = new NoteRequest();
        noteRequest.setOrderCode("123");
        noteRequest.setCustomerNote("customer");
        noteRequest.setNote("note");
        noteRequest.setCompanyId(1l);
        OrderModel orderModel = new OrderModel();
        orderModel.setId(1l);
        orderModel.setOrderStatus(OrderStatus.NEW.code());
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(orderModel);
        facade.updateNoteInOrder(noteRequest);
        verify(commerceCartService).updateNoteInOrder(orderModel, noteRequest);
    }

    @Test
    public void applyCoupon_RetailOrder() {
        when(appliedCouponRequest.getOrderCode()).thenReturn("000022");
        when(appliedCouponRequest.getCompanyId()).thenReturn(1l);
        order.setType(OrderType.RETAIL.toString());
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean()))
                .thenReturn(order);

        facade.applyCoupon(appliedCouponRequest);
        verify(couponServiceMock).redeemCoupon(any(CommerceRedeemCouponParameter.class));
        verify(orderConverter).convert(order);
    }

    @Test
    public void applyCoupon_OnlineOrder_StatusIsNew() {
        when(appliedCouponRequest.getOrderCode()).thenReturn("000022");
        when(appliedCouponRequest.getCompanyId()).thenReturn(1l);
        order.setType(OrderType.ONLINE.toString());
        order.setOrderStatus(OrderStatus.NEW.code());
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean()))
                .thenReturn(order);

        facade.applyCoupon(appliedCouponRequest);
        verify(couponServiceMock).redeemCoupon(any(CommerceRedeemCouponParameter.class));
        verify(orderConverter).convert(order);
    }

    @Test
    public void applyCoupon_OnlineOrder_StatusIsPackaged() {
        try {
            when(appliedCouponRequest.getOrderCode()).thenReturn("000022");
            when(appliedCouponRequest.getCompanyId()).thenReturn(1l);
            order.setType(OrderType.ONLINE.toString());
            order.setOrderStatus(OrderStatus.PACKAGED.code());
            when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean()))
                    .thenReturn(order);

            facade.applyCoupon(appliedCouponRequest);
            fail("Must throw exception");
            verify(orderConverter).convert(order);
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CAN_NOT_CHANGE_COUPON_CODE_OF_ONLINE_ORDER.code(), e.getCode());
        }
    }

    @Test
    public void removeCoupon() {
        when(appliedCouponRequest.getOrderCode()).thenReturn("000022");
        when(appliedCouponRequest.getCompanyId()).thenReturn(1l);
        when(appliedCouponRequest.getCouponCode()).thenReturn("couponCode");
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean()))
                .thenReturn(order);

        facade.removeCoupon(appliedCouponRequest);
        verify(couponServiceMock).releaseCoupon(any(CommerceRedeemCouponParameter.class));
        verify(orderConverter).convert(order);
    }

    @Test
    public void importOrderItem_InvalidOrderType() {
        try {
            order.setType(OrderType.RETAIL.toString());
            when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean()))
                    .thenReturn(order);
            when(orderItemExcelFileReader.read(multiplePartFileMock)).thenReturn(Collections.emptyList());

            facade.importOrderItem("code", 1l, multiplePartFileMock);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CANNOT_IMPORT_FOR_NOT_ONLINE_ORDER.code(), e.getCode());
        }
    }

    @Test
    public void importOrderItem_InvalidOrderStatus() {
        try {
            order.setType(OrderType.ONLINE.toString());
            order.setOrderStatus(OrderStatus.CONFIRMING_CHANGE.code());
            when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean()))
                    .thenReturn(order);
            when(orderItemExcelFileReader.read(multiplePartFileMock)).thenReturn(Collections.emptyList());

            facade.importOrderItem("code", 1l, multiplePartFileMock);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CANNOT_IMPORT_FOR_ONLINE_ORDER_STATUS.code(), e.getCode());
        }
    }

    @Test
    public void importOrderItem_EmptyProductList() {
        try {
            order.setType(OrderType.ONLINE.toString());
            order.setOrderStatus(OrderStatus.NEW.code());
            when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean()))
                    .thenReturn(order);
            when(orderItemExcelFileReader.read(multiplePartFileMock)).thenReturn(Collections.emptyList());
            facade.importOrderItem("code", 1l, multiplePartFileMock);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_IMPORT_ORDER_PRODUCT.code(), e.getCode());
        }
    }

    @Test
    public void importOrderItem() {
        order.setType(OrderType.ONLINE.toString());
        order.setOrderStatus(OrderStatus.NEW.code());
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean()))
                .thenReturn(order);
        when(orderItemExcelFileReader.read(multiplePartFileMock)).thenReturn(Arrays.asList(new OrderItemDTO()));

        facade.importOrderItem("code", 1l, multiplePartFileMock);
        verify(orderEntriesPopulator).populate(any(), any());
        verify(commerceCartService).recalculate(any(), eq(true));
        verify(orderConverter).convert(any());
    }

    @Test
    public void getSaleQuantity_EmptyEntries() {
        when(orderService.findAllSaleEntryBy(saleRequestMock)).thenReturn(Collections.emptyList());
        Map<Long, OrderSaleData> saleQuantity = facade.getSaleQuantity(saleRequestMock);
        assertEquals(0, saleQuantity.size());
    }

    @Test
    public void getSaleQuantity() {
        when(orderService.findAllSaleEntryBy(saleRequestMock)).thenReturn(Arrays.asList(saleEntryMock1, saleEntryMock2, saleEntryMock3));
        OrderModel orderMock1 = Mockito.mock(OrderModel.class);
        OrderModel orderMock2 = Mockito.mock(OrderModel.class);
        AbstractOrderModel orderMock3 = Mockito.mock(OrderModel.class);
        when(saleEntryMock1.getProductId()).thenReturn(1l);
        when(saleEntryMock1.getOrderType()).thenReturn(OrderType.ONLINE.toString());
        when(saleEntryMock1.getQuantity()).thenReturn(100l);

        when(saleEntryMock2.getProductId()).thenReturn(1l);
        when(saleEntryMock2.getOrderType()).thenReturn(OrderType.RETAIL.toString());
        when(saleEntryMock2.getQuantity()).thenReturn(300l);

        when(saleEntryMock3.getProductId()).thenReturn(1l);
        when(saleEntryMock3.getOrderType()).thenReturn(OrderType.WHOLESALE.toString());
        when(saleEntryMock3.getQuantity()).thenReturn(530l);

        when(orderMock1.getType()).thenReturn(OrderType.ONLINE.toString());
        when(orderMock2.getType()).thenReturn(OrderType.RETAIL.toString());
        when(orderMock3.getType()).thenReturn(OrderType.WHOLESALE.toString());

        Map<Long, OrderSaleData> saleQuantity = facade.getSaleQuantity(saleRequestMock);
        assertEquals(1, saleQuantity.size());
        assertEquals(100, saleQuantity.get(1l).getOnline());
        assertEquals(300, saleQuantity.get(1l).getRetail());
        assertEquals(530, saleQuantity.get(1l).getWholesale());
    }

    @Test
    public void updateQuantityOrderEntryTopping() {
        ToppingOptionRequest request = new ToppingOptionRequest();
        request.setQuantity(2);
        request.setEntryId(1l);
        request.setId(1l);
        request.setIce(70);
        request.setSugar(70);
        request.setCompanyId(1l);
        OrderEntryModel orderEntryModel = new OrderEntryModel();
        orderEntryModel.setId(1l);
        orderEntryModel.setQuantity(3l);
        ToppingOptionModel toppingOptionModel = new ToppingOptionModel();
        toppingOptionModel.setId(1l);
        toppingOptionModel.setQuantity(1);
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(new OrderModel());
        when(orderService.findEntryBy(anyLong(), any())).thenReturn(orderEntryModel);
        when(toppingOptionService.findById(any())).thenReturn(toppingOptionModel);
        when(toppingOptionModificationMock.getProductIds()).thenReturn(Collections.singleton(1L));
        when(commerceCartService.updateToppingOption(any())).thenReturn(toppingOptionModificationMock);
        facade.updateToppingOption(request, "122");
        verify(commerceCartService).updateOrderToppingOption(any());
    }

    @Test
    public void addEntryToppingToOrder() {
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(new OrderModel());
        OrderEntryModel orderEntryModel = new OrderEntryModel();
        orderEntryModel.setId(1L);
        orderEntryModel.setQuantity(1L);
        when(orderService.findEntryBy(anyLong(), any())).thenReturn(orderEntryModel);
        facade.addToppingOptionsToOrder(toppingOptionRequest, "113");
        verify(commerceCartService).addToppingOption(any());
    }

    @Test
    public void addSubEntryToppingToOrder() {
        OrderModel orderModel = new OrderModel();
        orderModel.setFinalPrice(1000d);
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderModel);
        when(toppingOptionService.findById(anyLong())).thenReturn(new ToppingOptionModel());
        facade.addToppingItems("113", toppingItemRequest);
        verify(commerceCartService).addToppingItem(any());
    }

    @Test
    public void removeSubOrderEntryTopping() {
        OrderModel orderModel = new OrderModel();
        orderModel.setFinalPrice(1000d);
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderModel);
        when(orderService.findEntryBy(anyLong(), any(OrderModel.class))).thenReturn(new OrderEntryModel());
        when(toppingOptionService.findByIdAndOrderEntry(anyLong(), any(OrderEntryModel.class))).thenReturn(new ToppingOptionModel());
        ToppingItemModel toppingItemModel = new ToppingItemModel();
        toppingItemModel.setBasePrice(1000d);
        toppingItemModel.setProductId(1L);
        when(toppingItemService.findByIdAndToppingOption(anyLong(), any())).thenReturn(toppingItemModel);
        when(commerceCartService.shouldUpdateOrderBill(orderModel)).thenReturn(true);
        when(toppingItemModificationMock.getProductId()).thenReturn(1l);
        when(commerceCartService.updateToppingItem(any())).thenReturn(toppingItemModificationMock);
        facade.removeToppingItems("113", 1l, 1L, 1L, 1L);
        verify(commerceCartService, times(1)).updateOrderToppingItem(any());
    }

    @Test
    public void removeOrderEntryTopping() {
        OrderModel orderModel = new OrderModel();
        orderModel.setFinalPrice(1000d);
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderModel);
        when(orderService.findEntryBy(anyLong(), any(OrderModel.class))).thenReturn(new OrderEntryModel());
        ToppingOptionModel toppingOptionModel = new ToppingOptionModel();
        Set<ToppingItemModel> toppingItemModelSet = new HashSet<>();
        ToppingItemModel toppingItemModel = new ToppingItemModel();
        toppingItemModel.setBasePrice(1000d);
        toppingItemModel.setProductId(1L);
        toppingItemModelSet.add(toppingItemModel);
        ToppingItemModel toppingItemModel1 = new ToppingItemModel();
        toppingItemModel1.setBasePrice(11000d);
        toppingItemModel1.setProductId(11L);
        toppingItemModelSet.add(toppingItemModel1);
        toppingOptionModel.setToppingItemModels(toppingItemModelSet);
        when(toppingOptionService.findByIdAndOrderEntry(anyLong(), any(OrderEntryModel.class))).thenReturn(toppingOptionModel);
        when(commerceCartService.shouldUpdateOrderBill(orderModel)).thenReturn(true);
        facade.removeToppingOptions("113", 1l, 1L, 2l);
        verify(commerceCartService, times(1)).deleteToppingOptionInOrder(any());
    }

    @Test
    public void updateQuantitySubOrderEntryTopping_quantityZeroOrNull() {
        OrderModel model = new OrderModel();
        model.setCode("122");
        model.setFinalPrice(30000d);
        ToppingItemRequest request = new ToppingItemRequest();
        request.setCompanyId(1l);
        request.setEntryId(0l);
        request.setOrderCode("122");
        ToppingItemModel toppingItemModel = new ToppingItemModel();
        toppingItemModel.setId(1l);
        toppingItemModel.setQuantity(1);
        toppingItemModel.setBasePrice(10000d);
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(model);
        when(orderService.findEntryBy(0l, model)).thenReturn(entryMock1);
        when(toppingOptionService.findByIdAndOrderEntry(any(), any())).thenReturn(new ToppingOptionModel());
        facade.updateQuantityToppingItems(request, "122");
        verify(commerceCartService).updateOrderToppingItem(any());
    }

    @Test
    public void updateQuantitySubOrderEntryTopping() {
        OrderModel model = new OrderModel();
        model.setFinalPrice(30000d);
        ToppingItemRequest request = new ToppingItemRequest();
        request.setQuantity(5);
        request.setId(1l);
        request.setCompanyId(1l);
        ToppingItemModel toppingItemModel = new ToppingItemModel();
        toppingItemModel.setId(1l);
        toppingItemModel.setQuantity(1);
        toppingItemModel.setBasePrice(10000d);
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(model);
        when(toppingOptionService.findByIdAndOrderEntry(any(), any())).thenReturn(new ToppingOptionModel());
        facade.updateQuantityToppingItems(request, "122");
        verify(commerceCartService, times(1)).updateOrderToppingItem(any());
    }

    @Test
    public void updateDiscountForToppingItem() {
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(order);
        OrderEntryModel orderEntryModel = new OrderEntryModel();
        orderEntryModel.setId(1l);
        when(orderService.findEntryBy(anyLong(), any())).thenReturn(orderEntryModel);
        when(toppingOptionService.findByIdAndOrderEntry(anyLong(), any())).thenReturn(new ToppingOptionModel());
        facade.updateDiscountForToppingItem(toppingItemRequest);

        verify(commerceCartService).updateDiscountForToppingItem(any(ToppingItemParameter.class));
        verify(orderConverter).convert(order);
    }

    @Test
    public void updatePaidAmountOrder_Null() {
        InvoiceKafkaData invoiceData = new InvoiceKafkaData();
        invoiceData.setAmount(10d);
        invoiceData.setReferId("123");
        invoiceData.setCompanyId(2l);
        when(orderService.findByCodeAndCompanyIdAndOrderTypeAndDeleted(anyString(), anyLong(), anyString(), anyBoolean())).thenReturn(null);
        facade.updatePaymentTransactionDataAndPaidAmount(invoiceData);
        verify(paymentTransactionService, times(0)).removePaymentByInvoice(order, 123213l);
        verify(orderService, times(0)).save(any());
    }

    @Test
    public void updatePaidAmountOrder_Success_InovicePendingForVerified() {
        InvoiceKafkaData invoiceData = new InvoiceKafkaData();
        invoiceData.setAmount(10d);
        invoiceData.setReferId("123");
        invoiceData.setCompanyId(2l);
        invoiceData.setInvoiceId(123213l);
        invoiceData.setStatus(BillStatus.PENDING_FOR_VERIFIED.code());
        order.setPaidAmount(10d);
        when(orderService.findByCodeAndCompanyIdAndOrderTypeAndDeleted(anyString(), anyLong(), anyString(), anyBoolean())).thenReturn(order);
        when(orderService.save(order)).thenReturn(order);
        facade.updatePaymentTransactionDataAndPaidAmount(invoiceData);
        verify(paymentTransactionService).removePaymentByInvoice(order, 123213l);
        verify(orderService).save(any());
        verify(orderElasticSearchService).updatePaymentTransactionDataAndPaidAmount(any(), any());
    }

    @Test
    public void updatePaidAmountOrder_Success_VerifiedInvoice() {
        InvoiceKafkaData invoiceData = new InvoiceKafkaData();
        invoiceData.setAmount(10d);
        invoiceData.setReferId("123");
        invoiceData.setCompanyId(2l);
        invoiceData.setInvoiceId(123213l);
        invoiceData.setStatus(BillStatus.VERIFIED.code());
        order.setPaidAmount(10d);
        when(orderService.findByCodeAndCompanyIdAndOrderTypeAndDeleted(anyString(), anyLong(), anyString(), anyBoolean())).thenReturn(order);
        when(orderService.save(order)).thenReturn(order);
        facade.updatePaymentTransactionDataAndPaidAmount(invoiceData);
        verify(paymentTransactionService, times(0)).removePaymentByInvoice(order, 123213l);
        verify(orderService).save(any());
        verify(orderElasticSearchService).updatePaymentTransactionDataAndPaidAmount(any(), any());
    }

    @Test
    public void updatePaidAmountOrder_Success_VerifiedInvoice_withLoyalty() {
        InvoiceKafkaData invoiceData = new InvoiceKafkaData();
        invoiceData.setAmount(10d);
        invoiceData.setReferId("123");
        invoiceData.setCompanyId(2l);
        invoiceData.setInvoiceId(123213l);
        invoiceData.setStatus(BillStatus.VERIFIED.code());
        invoiceData.setPaymentMethodCode(PaymentMethodType.LOYALTY_POINT.code());
        order.setPaidAmount(10d);
        when(orderService.findByCodeAndCompanyIdAndOrderTypeAndDeleted(anyString(), anyLong(), anyString(), anyBoolean())).thenReturn(order);
        when(orderService.save(order)).thenReturn(order);
        facade.updatePaymentTransactionDataAndPaidAmount(invoiceData);
        verify(paymentTransactionService, times(0)).removePaymentByInvoice(order, 123213l);
        verify(paymentTransactionService, times(1)).updatePaymentByInvoice(order, invoiceData);
        verify(orderService).save(any());
        verify(orderElasticSearchService).updatePaymentTransactionDataAndPaidAmount(any(), any());
    }

    @Test
    public void updatePaidAmountAllOrder_widthOrderId() {
        OrderPartialIndexRequest request = new OrderPartialIndexRequest();
        request.setOrderId(1l);
        order.setPaidAmount(10d);
        when(orderService.findById(anyLong())).thenReturn(order);
        facade.updatePaidAmountAllOrder(request);
        verify(orderService).findById(anyLong());
        verify(orderService).updatePaidAmountOrder(any(OrderModel.class));
    }


    @Test
    public void refreshOnlineOrder_NotFound() {
        try {
            when(orderService.findByCodeAndCompanyIdAndOrderTypeAndDeleted(anyString(), anyLong(), anyString(), anyBoolean())).thenReturn(null);
            facade.refresh(refreshCartRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_ID.code(), e.getCode());
        }
    }

    @Test
    public void refreshOnlineOrder_WithNullOrderStatus_NotAccepted() {
        try {
            when(orderService.findByCodeAndCompanyIdAndOrderTypeAndDeleted(anyString(), anyLong(), anyString(), anyBoolean())).thenReturn(order);
            order.setOrderStatus(null);
            facade.refresh(refreshCartRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CAN_NOT_CHANGE_WAREHOUSE_OR_COMPANY.code(), e.getCode());
        }
    }

    @Test
    public void refreshOnlineOrder_WithPREORDER_NotAccepted() {
        try {
            when(orderService.findByCodeAndCompanyIdAndOrderTypeAndDeleted(anyString(), anyLong(), anyString(), anyBoolean())).thenReturn(order);
            order.setOrderStatus(OrderStatus.PRE_ORDER.code());
            facade.refresh(refreshCartRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CAN_NOT_CHANGE_WAREHOUSE_OR_COMPANY.code(), e.getCode());
        }
    }

    @Test
    public void refreshOnlineOrder_WithCONFIRMED_NotAccepted() {
        try {
            when(orderService.findByCodeAndCompanyIdAndOrderTypeAndDeleted(anyString(), anyLong(), anyString(), anyBoolean())).thenReturn(order);
            order.setOrderStatus(OrderStatus.CONFIRMED.code());
            facade.refresh(refreshCartRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CAN_NOT_CHANGE_WAREHOUSE_OR_COMPANY.code(), e.getCode());
        }
    }

    @Test
    public void refreshOnlineOrder_OnlyChangeWarehouse() {
        when(orderService.findByCodeAndCompanyIdAndOrderTypeAndDeleted(anyString(), anyLong(), anyString(), anyBoolean())).thenReturn(order);
        order.setOrderStatus(OrderStatus.NEW.code());
        order.setWarehouseId(10l);
        order.setCompanyId(1l);
        order.setCustomerId(1l);
        PaymentTransactionModel paymentTransactionModel = new PaymentTransactionModel();
        paymentTransactionModel.setWarehouseId(1l);
        payments.add(paymentTransactionModel);
        order.setPaymentTransactions(payments);
        when(orderService.save(any(OrderModel.class))).thenReturn(order);
        facade.refresh(refreshCartRequest);
        verify(orderService).save(captor.capture());
        verify(invoiceService).saveInvoices(captor.getValue(), captor.getValue().getCustomerId());
        payments = captor.getValue().getPaymentTransactions();
        assertEquals(1, payments.size());
        for (PaymentTransactionModel paymentTransaction : payments) {
            assertEquals(17, paymentTransaction.getWarehouseId(), 0);
        }
        assertEquals(17l, captor.getValue().getWarehouseId(), 0);
        verify(commerceCartService, times(0)).removeAllEntries(any(CommerceAbstractOrderParameter.class));
    }


    @Test
    public void refreshOnlineOrder_ChangeCompanyAndWarehouse_EmptyEntry() {
        when(orderService.findByCodeAndCompanyIdAndOrderTypeAndDeleted(anyString(), anyLong(), anyString(), anyBoolean())).thenReturn(order);
        order.setOrderStatus(OrderStatus.NEW.code());
        order.setWarehouseId(10l);
        order.setCompanyId(2l);
        order.setCustomerId(3l);
        PaymentTransactionModel paymentTransactionModel = new PaymentTransactionModel();
        paymentTransactionModel.setWarehouseId(1l);
        payments.add(paymentTransactionModel);
        order.setPaymentTransactions(payments);
        when(orderService.save(order)).thenReturn(order);

        facade.refresh(refreshCartRequest);
        verify(invoiceService).unverifyInvoices(order);
        verify(orderService).save(captor.capture());
        assertEquals(0, captor.getValue().getPaymentTransactions().size());
        assertNull(captor.getValue().getCustomerId());
        assertEquals(17l, captor.getValue().getWarehouseId(), 0);
        assertEquals(1l, captor.getValue().getCompanyId(), 0);
        verify(commerceCartService, times(0)).removeAllEntries(any(CommerceAbstractOrderParameter.class));
    }

    @Test
    public void refreshOnlineOrder_ChangeCompanyAndWarehouse_RemoveAllEntries() {
        when(orderService.findByCodeAndCompanyIdAndOrderTypeAndDeleted(anyString(), anyLong(), anyString(), anyBoolean())).thenReturn(order);
        order.setOrderStatus(OrderStatus.NEW.code());
        order.setWarehouseId(10l);
        order.setCompanyId(2l);
        order.setEntries(Arrays.asList(new OrderEntryModel()));
        when(orderService.save(order)).thenReturn(order);


        facade.refresh(refreshCartRequest);
        verify(orderService).save(captor.capture());
        assertEquals(17l, captor.getValue().getWarehouseId(), 0);
        assertEquals(1l, captor.getValue().getCompanyId(), 0);
        verify(commerceCartService, times(1)).removeAllEntries(any(CommerceAbstractOrderParameter.class));
    }


    @Test
    public void removeListEntry() {
        EntryRequest entryRequest = new EntryRequest();
        entryRequest.setOrderCode("123");
        entryRequest.setCompanyId(1l);
        entryRequest.setEntryIds("123");

        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(order);

        order.setType(OrderType.ONLINE.toString());
        facade.removeListEntry(entryRequest);
        verify(commerceCartService).updateListOrderEntry(any(), any());
        verify(orderConverter).convert(order);
    }

    @Test
    public void updatePriceForOrderEntries_isEmptyEntries() {
        when(orderRequest.getCode()).thenReturn("code");
        when(orderRequest.getCompanyId()).thenReturn(2l);
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);

        facade.updatePriceForOrderEntries(orderRequest);
        verify(orderService).save(any(OrderModel.class));
        verify(orderConverter).convert(any(OrderModel.class));
    }

    @Test
    public void updatePriceForOrderEntries() {
        order.setEntries(Arrays.asList(new OrderEntryModel()));
        when(orderRequest.getCode()).thenReturn("code");
        when(orderRequest.getCompanyId()).thenReturn(2l);
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);

        facade.updatePriceForOrderEntries(orderRequest);
        verify(commerceCartService).updatePriceForCartEntries(any(OrderModel.class));
        verify(orderConverter).convert(any(OrderModel.class));
    }

    @Test
    public void remove_ProductOfCombo_WidthPreOrder() {
        RemoveSubOrderEntryRequest request = new RemoveSubOrderEntryRequest();
        request.setOrderCode("code");
        request.setCompanyId(2l);
        request.setEntryId(1l);
        request.setSubEntryId(123l);
        SubOrderEntryModel subEntry = new SubOrderEntryModel();
        subEntry.setProductId(123l);
        subEntry.setQuantity(10);

        when(entryModel.isPreOrder()).thenReturn(true);
        order.setType(OrderType.ONLINE.name());
        order.setOrderStatus(OrderStatus.PRE_ORDER.toString());
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(order);
        when(orderService.findEntryBy(anyLong(), any())).thenReturn(entryModel);
        when(subOrderEntryRepository.findByOrderEntryAndId(any(), anyLong())).thenReturn(subEntry);
        facade.removeSubEntry(request);
        verify(inventoryService, times(1)).updatePreOrderProductOfList(any(), any(), anyBoolean());
    }

    @Test
    public void remove_ProductOfCombo_WidthHolding() {
        RemoveSubOrderEntryRequest request = new RemoveSubOrderEntryRequest();
        request.setOrderCode("code");
        request.setCompanyId(2l);
        request.setEntryId(1l);
        request.setSubEntryId(123l);
        SubOrderEntryModel subEntry = new SubOrderEntryModel();
        subEntry.setProductId(123l);
        subEntry.setQuantity(10);

        when(entryModel.isHolding()).thenReturn(true);
        order.setType(OrderType.ONLINE.name());
        order.setOrderStatus(OrderStatus.NEW.toString());
        order.setOrderStatus(OrderStatus.NEW.toString());
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(order);
        when(orderService.findEntryBy(anyLong(), any())).thenReturn(entryModel);
        when(subOrderEntryRepository.findByOrderEntryAndId(any(), anyLong())).thenReturn(subEntry);
        facade.removeSubEntry(request);
        verify(inventoryService, times(1)).updateStockHoldingProductOfList(any(), any(), anyBoolean());
    }

    @Test
    public void remove_ProductOfCombo_equalsComboFixed() {
        try {
            RemoveSubOrderEntryRequest request = new RemoveSubOrderEntryRequest();
            request.setOrderCode("code");
            request.setCompanyId(2l);
            request.setEntryId(1l);
            request.setSubEntryId(123l);
            when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(order);
            when(entryModel.getComboType()).thenReturn(ComboType.FIXED_COMBO.toString());
            when(entryModel.getId()).thenReturn(1l);
            when(orderService.findEntryBy(anyLong(), any())).thenReturn(entryModel);
            facade.removeSubEntry(request);
            fail("must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CANNOT_REMOVE_SUB_ORDER_ENTRY.code(), e.getCode());
        }
    }

    @Test
    public void remove_ProductOfCombo_invalidSubOrderEntry() {
        try {
            RemoveSubOrderEntryRequest request = new RemoveSubOrderEntryRequest();
            request.setOrderCode("code");
            request.setCompanyId(2l);
            request.setEntryId(1l);
            request.setSubEntryId(123l);
            when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(order);
            when(orderService.findEntryBy(anyLong(), any())).thenReturn(entryModel);
            when(subOrderEntryRepository.findByOrderEntryAndId(any(), anyLong())).thenReturn(null);
            facade.removeSubEntry(request);
            fail("must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_SUB_ORDER_ENTRY_ID.code(), e.getCode());
        }
    }

    @Test
    public void changeStatusOrder_Exist_Combo_Invalid_Quantity() {
        try {
            changeOrderStatusRequest.setOrderCode("code");
            changeOrderStatusRequest.setCompanyId(1l);
            when(changeOrderStatusRequest.getOrderCode()).thenReturn("code");
            when(changeOrderStatusRequest.getCompanyId()).thenReturn(1l);
            when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(order);
            when(changeOrderStatusRequest.getOrderStatus()).thenReturn(OrderStatus.CONFIRMED.code());
            order.setOrderStatus(OrderStatus.NEW.code());
            OrderEntryModel orderEntryModel = new OrderEntryModel();
            orderEntryModel.setProductId(13672461l);
            orderEntryModel.setQuantity(1l);
            orderEntryModel.setComboType(ComboType.ONE_GROUP.toString());
            SubOrderEntryModel subOrderEntryModel = new SubOrderEntryModel();
            subOrderEntryModel.setQuantity(2);
            orderEntryModel.setSubOrderEntries(Collections.singleton(subOrderEntryModel));
            order.setEntries(Arrays.asList(orderEntryModel));
            ComboData comboData = new ComboData();
            comboData.setTotalItemQuantity(3);
            when(productService.getCombo(anyLong(), anyLong())).thenReturn(comboData);

            when(permissionFacade.hasPermission(PermissionCodes.CHANGE_ORDER_STATUS_COMPLETED.code(),
                    order.getCompanyId())).thenReturn(true);
            facade.changeStatusOrder(changeOrderStatusRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_SUB_ORDER_ENTRY_QUANTITY.code(), e.getCode());
        }
    }

    @Test
    public void updateShippingFee_Invalid_Order() {
        try {
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(null);
            facade.updateShippingFee(orderRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_CODE.code(), e.getCode());
        }
    }

    @Test
    public void updateShippingFee_Invalid_OrderType() {
        try {
            when(orderRequest.getCompanyId()).thenReturn(2l);
            when(orderRequest.getCode()).thenReturn("code");
            order.setType(OrderType.RETAIL.toString());
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
            facade.updateShippingFee(orderRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_TYPE.code(), e.getCode());
        }
    }

    @Test
    public void updateShippingFee() {
        when(orderRequest.getCompanyId()).thenReturn(2l);
        when(orderRequest.getCode()).thenReturn("code");
        when(orderRequest.getDeliveryCost()).thenReturn(10000d);
        when(orderRequest.getCollaboratorShippingFee()).thenReturn(10000d);
        order.setType(OrderType.ONLINE.toString());
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
        facade.updateShippingFee(orderRequest);
        verify(commerceCartService).updateShippingFee(any());
    }

    @Test
    public void updateDefaultSettingCustomer() {
        when(orderRequest.getCompanyId()).thenReturn(2l);
        when(orderRequest.getCode()).thenReturn("code");
        when(orderRequest.getAge()).thenReturn("19");
        when(orderRequest.getGender()).thenReturn("MALE");
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
        facade.updateDefaultSettingCustomer(orderRequest);
        verify(commerceCartService).updateDefaultSettingCustomer(any());
    }

    @Test
    public void updateDefaultSettingCustomer_invalid_Order() {
        try {
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(null);
            facade.updateDefaultSettingCustomer(orderRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_CODE.code(), e.getCode());
        }
    }

    @Test
    public void checkDiscountMaximum_withExitsConfirmDiscount() {
        order.setType(OrderType.ONLINE.toString());
        order.setConfirmDiscountBy(2l);
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
        List<OrderSettingDiscountData> dataList = facade.checkDiscountMaximum(2l, "code");
        assertEquals(0, dataList.size());
    }

    @Test
    public void checkDiscountMaximum_notApply_ONLINE() {
        order.setCompanyId(2l);
        order.setType(OrderType.ONLINE.toString());
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
        List<OrderSettingDiscountData> dataList = facade.checkDiscountMaximum(2l, "code");
        assertEquals(0, dataList.size());
    }

    @Test
    public void checkDiscountMaximum() {
        order.setCompanyId(2l);
        order.setType(OrderType.ONLINE.toString());
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
        when(commerceCartService.checkDiscountMaximumOrder(any(OrderModel.class))).thenReturn(Arrays.asList(new OrderSettingDiscountData()));
        List<OrderSettingDiscountData> dataList = facade.checkDiscountMaximum(2l, "code");
        assertEquals(1, dataList.size());
        verify(commerceCartService).checkDiscountMaximumOrder(any(OrderModel.class));
    }

    @Test
    public void updateAllDiscountForOrder() {
        UpdateAllDiscountRequest request = new UpdateAllDiscountRequest();
        request.setCompanyId(1l);
        request.setProductIds(Arrays.asList(123l));
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(order);
        facade.updateAllDiscountForOrder("123", request);
        verify(commerceCartService).updateAllDiscountForCart(any(CommerceAbstractOrderParameter.class), any());
        verify(orderConverter).convert(order);
    }

    @Test
    public void updateDiscountOfEntry_large_maximum_discount() {
        try {
            when(orderEntryDTO.getOrderCode()).thenReturn("code");
            when(orderEntryDTO.getCompanyId()).thenReturn(1l);
            when(orderEntryDTO.getProductId()).thenReturn(1l);
            when(orderEntryDTO.getDiscount()).thenReturn(200d);
            when(orderEntryDTO.getEntryId()).thenReturn(1l);
            when(orderEntryDTO.getDiscountType()).thenReturn(CurrencyType.PERCENT.toString());

            AbstractOrderEntryModel entryModel = new AbstractOrderEntryModel();
            entryModel.setTotalPrice(2000d);
            entryModel.setProductId(orderEntryDTO.getProductId());
            entryModel.setGiveAway(false);
            entryModel.setId(1l);
            entryModel.setQuantity(1l);
            entryModel.setOriginBasePrice(2000d);
            order.setEntries(Arrays.asList(entryModel));
            Map<Long, OrderSettingDiscountData> discountDataMap = new HashMap<>();
            OrderSettingDiscountData discountData = new OrderSettingDiscountData();
            discountData.setProductId(orderEntryDTO.getProductId());
            discountData.setDiscount(100d);
            discountData.setDiscountType(CurrencyType.CASH.toString());
            discountDataMap.put(discountData.getProductId(), discountData);

            when(permissionFacade.checkPermission(anyString(), anyLong(), anyLong())).thenReturn(false);
            when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(order);
            when(commerceCartService.checkDiscountMaximumProduct(any(OrderModel.class), anyLong())).thenReturn(discountDataMap);
            facade.updateDiscountOfEntry(orderEntryDTO);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.DISCOUNT_MUST_BE_LESS_SETTING_MAXIMUM_DISCOUNT.code(), e.getCode());
        }
    }

    @Test
    public void invalid_uploadImageToOrder_notPermission_update() {
        try {
            OrderImagesRequest request = new OrderImagesRequest();
            request.setCompanyId(1l);
            OrderImageData orderImageData = new OrderImageData();
            orderImageData.setUrl("image");
            request.setOrderImages(Arrays.asList(orderImageData));
            order.setOrderStatus(OrderStatus.CONFIRMED.code());
            when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(order);
            when(permissionFacade.checkPermission(anyString(), anyLong(), anyLong())).thenReturn(false);
            facade.uploadImageToOrder(request, "123");
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.HAS_NOT_PERMISSION_TO_UPDATE_ORDER.code(), e.getCode());
        }
    }

    @Test
    public void uploadImageToOrder() {
        OrderImagesRequest request = new OrderImagesRequest();
        request.setCompanyId(1l);
        OrderImageData orderImageData = new OrderImageData();
        orderImageData.setUrl("image");
        request.setOrderImages(Arrays.asList(orderImageData));
        order.setOrderStatus(OrderStatus.CONFIRMING.code());
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(order);
        when(permissionFacade.checkPermission(anyString(), anyLong(), anyLong())).thenReturn(true);
        facade.uploadImageToOrder(request, "123");
        verify(orderService).save(any(OrderModel.class));
    }

    @Test
    public void updateRecommendedRetailPriceForCartEntry() {
        when(orderEntryDTO.getRecommendedRetailPrice()).thenReturn(12.2);
        when(orderEntryDTO.getOrderCode()).thenReturn("12341234");
        when(orderEntryDTO.getEntryId()).thenReturn(1l);
        when(orderEntryDTO.getCompanyId()).thenReturn(2l);
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
        when(commerceCartService.updateRecommendedRetailPriceForCartEntry(any())).thenReturn(false);
        when(orderConverter.convert(any())).thenReturn(new OrderData());
        facade.updateRecommendedRetailPriceForOrderEntry(orderEntryDTO);
        verify(commerceCartService).updateRecommendedRetailPriceForCartEntry(any(CommerceAbstractOrderParameter.class));
        verify(orderConverter).convert(order);
    }

    @Test
    public void cancelRedeem_redeemAmount_null() {
        order.setCode("code");
        order.setCustomerId(2L);
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
        facade.cancelRedeem("code", 2L);
        verify(orderConverter, times(1)).convert(any());
        verify(commercePlaceOrderStrategy, times(0)).cancelRedeem(any(OrderModel.class));
    }

    @Test
    public void cancelRedeem() {
        order.setCode("code");
        order.setRedeemAmount(10000d);
        order.setCustomerId(2L);
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
        facade.cancelRedeem("code", 2L);
        verify(orderConverter, times(1)).convert(any());
        verify(commercePlaceOrderStrategy, times(1)).cancelRedeem(any(OrderModel.class));
    }

    @Test
    public void updateRedeemOnline_notExistCustomer() {
        try {
            order.setCode("code");
            order.setRedeemAmount(10000d);
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
            facade.updateRedeemOnline("code", 2L, new PaymentTransactionRequest());
            fail("throw new exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_ORDER_CUSTOMER_INFO.message(), e.getMessage());
        }
    }

    @Test
    public void updateRedeemOnline() {
        order.setCode("code");
        order.setRedeemAmount(10000d);
        order.setCustomerId(2L);
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
        facade.updateRedeemOnline("code", 2L, new PaymentTransactionRequest());
        verify(orderService, times(1)).findByCodeAndCompanyId(anyString(), anyLong());
        verify(commercePlaceOrderStrategy, times(1)).updateRedeemOnline(any(OrderModel.class), any());
    }

    @Test
    public void createRedeemOnline() {
        PaymentTransactionRequest request = new PaymentTransactionRequest();
        request.setPaymentMethodId(2L);
        order.setCode("code");
        order.setRedeemAmount(10000d);
        order.setCustomerId(2L);
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
        facade.createRedeemOnline("code", 2L, request);
        verify(orderService, times(1)).findByCodeAndCompanyId(anyString(), anyLong());
        verify(commercePlaceOrderStrategy, times(1)).createRedeemOnline(any(OrderModel.class), any());
    }

    @Test
    public void getLoyaltyPointsFor() {
        order.setCode("code");
        order.setRedeemAmount(10000d);
        order.setCustomerId(2L);
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
        facade.getLoyaltyPointsFor("code", 2L);
        verify(orderService, times(1)).findByCodeAndCompanyId(anyString(), anyLong());
        verify(loyaltyService, times(1)).getLoyaltyPointsOf(any(OrderModel.class));
    }

    @Test
    public void updateSettingCustomerToOrder() {
        when(orderRequest.getCode()).thenReturn("code");
        when(orderRequest.getCompanyId()).thenReturn(2l);
        when(orderRequest.getSettingCustomerOptionIds()).thenReturn(Arrays.asList(2L));
        order.setCode("code");
        order.setRedeemAmount(10000d);
        order.setCustomerId(2L);
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
        facade.updateSettingCustomerToOrder(orderRequest);
        verify(orderService, times(1)).findByCodeAndCompanyId(anyString(), anyLong());
        verify(commercePlaceOrderStrategy, times(1)).updateSettingCustomerToOrder(any(OrderModel.class), anyList());
        verify(orderConverter, times(1)).convert(any(OrderModel.class));
    }

    @Test
    public void markEntrySaleOff_invalidCompany() {
        try {
            EntrySaleOffRequest request = new EntrySaleOffRequest();
            facade.markEntrySaleOff(request);
            fail("throw new exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_COMPANY_ID.message(), e.getMessage());
        }
    }

    @Test
    public void markEntrySaleOff() {
        EntrySaleOffRequest request = new EntrySaleOffRequest();
        request.setCompanyId(2L);
        request.setOrderCode("code");
        request.setSaleOff(true);
        request.setEntryId(2L);
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
        facade.markEntrySaleOff(request);
        verify(commerceCartService).markEntrySaleOff(any(CommerceAbstractOrderParameter.class));
    }

    @Test
    public void isSaleOffEntry() {
        orderService.isSaleOffEntry(new OrderEntryDTO());
        verify(orderService).isSaleOffEntry(any(OrderEntryDTO.class));
    }

    @Test
    public void updateCustomer_invalidCustomer() {
        try {
            CustomerRequest customerRequest = new CustomerRequest();
            customerRequest.setId(2L);
            UpdateCustomerRequest request = new UpdateCustomerRequest();
            request.setCode("code");
            request.setCompanyId(2L);
            request.setCardNumber("card number");
            request.setCustomer(customerRequest);
            when(customerService.getBasicCustomerInfo(anyLong(), anyLong())).thenReturn(null);
            facade.updateCustomer(request);
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_ORDER_CUSTOMER_INFO.message(), e.getMessage());
        }
    }

    @Test
    public void updateCustomer() {
        CustomerRequest customerRequest = new CustomerRequest();
        customerRequest.setId(2L);
        UpdateCustomerRequest request = new UpdateCustomerRequest();
        request.setCode("code");
        request.setCompanyId(2L);
        request.setCardNumber("card number");
        request.setCustomer(customerRequest);
        when(customerService.getBasicCustomerInfo(anyLong(), anyLong())).thenReturn(new CustomerData());
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
        when(commerceCartService.updateCustomer(any(UpdateCustomerRequest.class), any(AbstractOrderModel.class))).thenReturn(order);
        facade.updateCustomer(request);
        verify(commerceCartService).updateCustomer(any(UpdateCustomerRequest.class), any(AbstractOrderModel.class));
        verify(orderConverter).convert(any(OrderModel.class));
    }
}

