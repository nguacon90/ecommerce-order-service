package com.vctek.orderservice.facade.impl;

import com.vctek.converter.Converter;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.*;
import com.vctek.orderservice.dto.request.*;
import com.vctek.orderservice.dto.request.storefront.*;
import com.vctek.orderservice.elasticsearch.model.OrderSearchModel;
import com.vctek.orderservice.elasticsearch.model.ProductSearchModel;
import com.vctek.orderservice.elasticsearch.service.ProductSearchService;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.OrderElasticSearchFacade;
import com.vctek.orderservice.feignclient.dto.ProductSearchRequest;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.service.*;
import com.vctek.orderservice.strategy.CommerceChangeOrderStatusStrategy;
import com.vctek.orderservice.strategy.CommercePlaceOrderStrategy;
import com.vctek.orderservice.util.SellSignal;
import com.vctek.service.UserService;
import com.vctek.sync.MutexFactory;
import com.vctek.sync.XMutexFactoryImpl;
import com.vctek.util.ComboType;
import com.vctek.util.OrderStatus;
import com.vctek.validate.Validator;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.cluster.lock.support.DelegatingDistributedLock;
import org.springframework.cloud.cluster.redis.lock.RedisLockService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

public class CommerceCartFacadeTest {
    private DefaultCommerceCartFacade facade;

    @Mock
    private ProductSearchService productSearchService;
    @Mock
    private CommerceCartService commerceCartService;
    @Mock
    private Converter<CartModel, MiniCartData> storefrontMiniCartConverter;
    @Mock
    private OrderStorefrontSetupService orderStorefrontSetupService;
    @Mock
    private Converter<AbstractOrderModel, CommerceCartData> commerceCartDataConverter;
    @Mock
    private UserService userService;
    @Mock
    private CartService cartService;
    @Mock
    private ProductPromotionRequest request;
    @Mock
    private CartModel cartModel;
    @Mock
    private OrderStorefrontSetupModel setupModel;
    @Mock
    private CommerceCartData commerceCartData;
    @Mock
    private CartModel oldCart;
    @Mock
    private StorefrontOrderEntryDTO entryDTO;
    @Mock
    private CommerceAbstractOrderParameter abstractOrderParameter;
    @Mock
    private Converter<StorefrontOrderEntryDTO, CommerceAbstractOrderParameter> storefrontCommerceCartParameterConverter;
    private MutexFactory<String> mutexFactory = new XMutexFactoryImpl<>();
    @Mock
    private AbstractOrderEntryModel cartEntry;
    @Mock
    private CommerceCartModification modification;
    @Mock
    private ProductService productService;
    @Mock
    private AddSubOrderEntryRequest subentry1;
    @Mock
    private ComboData comboData;
    @Mock
    private ProductInComboData productInComboData;
    @Mock
    private Validator<CommerceAbstractOrderParameter> updateCommerceEntryValidator;
    @Mock
    private ProductPromotion productPromotion;
    @Mock
    private CommercePlaceOrderStrategy commercePlaceOrderStrategy;
    @Mock
    private Validator<StoreFrontSubOrderEntryRequest> changeProductInComboValidator;
    @Mock
    private CouponService couponService;
    @Mock
    private CommerceCartShippingFeeService commerceCartShippingFeeService;
    @Mock
    private OrderElasticSearchFacade orderElasticSearchFacade;
    @Mock
    private Converter<OrderSearchModel, CommerceOrderData> storefrontOrderDataConverter;

    @Mock
    private OrderService orderService;
    @Mock
    private RedisLockService redisLockService;
    @Mock
    private DelegatingDistributedLock lockMock;
    @Mock
    private CommerceChangeOrderStatusStrategy commerceChangeOrderStatusStrategy;
    @Mock
    private BillService billService;
    @Mock
    private CommerceCancelOrderRequest cancelReq;
    @Mock
    private OrderModel orderMock;
    @Mock
    private Validator<StoreFrontCheckoutRequest> storefrontCheckoutValidator;
    @Mock
    private CustomerService customerService;
    private CreateCartParam param;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        param = new CreateCartParam();
        param.setCompanyId(1l);
        param.setOldCartGuid("oldAnonymousCart");
        param.setSellSignal(SellSignal.ECOMMERCE_WEB.toString());
        facade = new DefaultCommerceCartFacade();
        facade.setCartService(cartService);
        facade.setOrderStorefrontSetupService(orderStorefrontSetupService);
        facade.setUserService(userService);
        facade.setCommerceCartDataConverter(commerceCartDataConverter);
        facade.setStorefrontMiniCartConverter(storefrontMiniCartConverter);
        facade.setCommerceCartService(commerceCartService);
        facade.setProductSearchService(productSearchService);
        facade.setStorefrontCommerceCartParameterConverter(storefrontCommerceCartParameterConverter);
        facade.setMutexFactory(mutexFactory);
        facade.setProductService(productService);
        facade.setUpdateCommerceEntryValidator(updateCommerceEntryValidator);
        facade.setCommercePlaceOrderStrategy(commercePlaceOrderStrategy);
        facade.setChangeProductInComboValidator(changeProductInComboValidator);
        facade.setCouponService(couponService);
        facade.setCommerceCartShippingFeeService(commerceCartShippingFeeService);
        facade.setOrderElasticSearchFacade(orderElasticSearchFacade);
        facade.setStorefrontOrderDataConverter(storefrontOrderDataConverter);
        facade.setBillService(billService);
        facade.setCommerceChangeOrderStatusStrategy(commerceChangeOrderStatusStrategy);
        facade.setRedisLockService(redisLockService);
        facade.setOrderService(orderService);
        facade.setStorefrontCheckoutValidator(storefrontCheckoutValidator);
        facade.setCustomerService(customerService);
        when(redisLockService.obtain(anyString())).thenReturn(lockMock);
        when(lockMock.tryLock()).thenReturn(true);
        when(entryDTO.getCompanyId()).thenReturn(1l);
        when(entryDTO.getOrderCode()).thenReturn("orderCode");
        when(setupModel.getWarehouseId()).thenReturn(123l);
        when(orderStorefrontSetupService.findByCompanyId(any())).thenReturn(setupModel);
        when(commerceCartDataConverter.convert(cartModel)).thenReturn(commerceCartData);
        when(cartModel.getId()).thenReturn(11l);
        when(oldCart.getId()).thenReturn(22l);
        when(storefrontCommerceCartParameterConverter.convert(entryDTO)).thenReturn(abstractOrderParameter);
        when(modification.getEntry()).thenReturn(cartEntry);
        when(abstractOrderParameter.getOrder()).thenReturn(cartModel);
        when(productService.getCombo(anyLong(), anyLong())).thenReturn(comboData);
        when(comboData.getTotalItemQuantity()).thenReturn(3);
        when(subentry1.getProductId()).thenReturn(1111l);
        when(subentry1.getComboId()).thenReturn(22l);
        when(productInComboData.getId()).thenReturn(1111l);
        when(productPromotion.getProductId()).thenReturn(11l);
        when(productPromotion.getQuantity()).thenReturn(1);
        when(cancelReq.getCompanyId()).thenReturn(2l);
        when(cancelReq.getOrderCode()).thenReturn("0000111");
        when(cancelReq.getCancelText()).thenReturn("Cancel text");
    }

    @Test
    public void calculateProductPromotionPrice_emptyProductIdList() {
        Map<Long, Double> productPromotionMap = facade.calculateProductPromotionPrice(request);
        assertEquals(0, productPromotionMap.size());
    }

    @Test
    public void calculateProductPromotionPrice_notFoundProduct() {
        when(request.getProductList()).thenReturn(Arrays.asList(productPromotion));
        when(productSearchService.findAllByIdIn(anyList())).thenReturn(new ArrayList<>());
        Map<Long, Double> productPromotionMap = facade.calculateProductPromotionPrice(request);
        assertEquals(0, productPromotionMap.size());
    }

    @Test
    public void calculateProductPromotionPrice() {
        when(request.getProductList()).thenReturn(Arrays.asList(productPromotion));
        when(productSearchService.findAllByCompanyId(any(ProductSearchRequest.class))).thenReturn(Arrays.asList(new ProductSearchModel()));
        facade.calculateProductPromotionPrice(request);
        verify(commerceCartService).getDiscountPriceFor(request);
    }

    @Test
    public void getMiniCart_notFoundCart() {
        when(commerceCartService.getStorefrontCart(anyString(), anyLong())).thenReturn(null);
        facade.getMiniCart(1l, "cartCode");
        verify(storefrontMiniCartConverter, times(0)).convert(any());
    }

    @Test
    public void getMiniCart() {
        when(commerceCartService.getStorefrontCart(anyString(), anyLong())).thenReturn(cartModel);
        facade.getMiniCart(1l, "cartCode");
        verify(storefrontMiniCartConverter, times(1)).convert(cartModel);
    }

    @Test(expected = ServiceException.class)
    public void getOrCreateNewCart_NotSetUpWarehouseForCommerceWeb() {
        when(orderStorefrontSetupService.findByCompanyId(any())).thenReturn(null);
        facade.getOrCreateNewCart(param);
    }

    @Test(expected = ServiceException.class)
    public void getOrCreateNewCart_NotSetUpWarehouseForCommerceWebCase2() {
        when(setupModel.getWarehouseId()).thenReturn(null);
        facade.getOrCreateNewCart(param);
    }

    @Test
    public void getOrCreateNewCart_anonymousUser() {
        when(userService.getCurrentUserId()).thenReturn(null);
        when(commerceCartService.getOrCreateNewStorefrontCart(any())).thenReturn(cartModel);

        facade.getOrCreateNewCart(param);
        verify(commerceCartDataConverter).convert(cartModel);
        verify(commerceCartService, times(0)).getByCompanyIdAndGuid(anyLong(), anyString());
        verify(commerceCartService, times(0)).mergeCarts(any(), any());
        verify(cartService, times(0)).delete(any());
    }

    @Test
    public void getOrCreateNewCart_User_HasNotOldCartGuid() {
        when(userService.getCurrentUserId()).thenReturn(1l);
        when(commerceCartService.getOrCreateNewStorefrontCart(any())).thenReturn(cartModel);
        param.setOldCartGuid(null);
        facade.getOrCreateNewCart(param);
        verify(commerceCartDataConverter).convert(cartModel);
        verify(commerceCartService, times(0)).getByCompanyIdAndGuid(anyLong(), anyString());
        verify(commerceCartService, times(0)).mergeCarts(any(), any());
        verify(cartService, times(0)).delete(any());
    }

    @Test
    public void getOrCreateNewCart_User_HasOldCartGuid_EmptyEntries() {
        when(userService.getCurrentUserId()).thenReturn(1l);
        when(commerceCartService.getOrCreateNewStorefrontCart(any())).thenReturn(cartModel);
        when(commerceCartService.getByCompanyIdAndGuid(any(), anyString())).thenReturn(oldCart);
        when(oldCart.getEntries()).thenReturn(new ArrayList<>());
        param.setOldCartGuid("oldCart");
        facade.getOrCreateNewCart(param);
        verify(commerceCartDataConverter).convert(cartModel);
        verify(commerceCartService, times(1)).getByCompanyIdAndGuid(anyLong(), anyString());
        verify(commerceCartService, times(0)).mergeCarts(any(), any());
        verify(cartService, times(1)).delete(oldCart);
    }

    @Test
    public void getOrCreateNewCart_User_HasOldCartGuid_TheSameCart() {
        when(userService.getCurrentUserId()).thenReturn(1l);
        when(commerceCartService.getOrCreateNewStorefrontCart(any())).thenReturn(cartModel);
        when(commerceCartService.getByCompanyIdAndGuid(any(), anyString())).thenReturn(oldCart);
        when(cartModel.getId()).thenReturn(1l);
        when(oldCart.getId()).thenReturn(1l);
        when(oldCart.getEntries()).thenReturn(Arrays.asList(new CartEntryModel()));
        param.setOldCartGuid("oldCart");
        facade.getOrCreateNewCart(param);
        verify(commerceCartDataConverter).convert(cartModel);
        verify(commerceCartService, times(1)).getByCompanyIdAndGuid(anyLong(), anyString());
        verify(commerceCartService, times(0)).mergeCarts(any(), any());
        verify(cartService, times(0)).delete(oldCart);
    }

    @Test
    public void getOrCreateNewCart_User_mergeCart() {
        when(userService.getCurrentUserId()).thenReturn(1l);
        when(commerceCartService.getOrCreateNewStorefrontCart(any())).thenReturn(cartModel);
        when(commerceCartService.getByCompanyIdAndGuid(any(), anyString())).thenReturn(oldCart);
        when(oldCart.getEntries()).thenReturn(Arrays.asList(new CartEntryModel()));
        param.setOldCartGuid("oldCart");

        facade.getOrCreateNewCart(param);
        verify(commerceCartDataConverter).convert(cartModel);
        verify(commerceCartService, times(1)).getByCompanyIdAndGuid(anyLong(), anyString());
        verify(commerceCartService, times(1)).mergeCarts(any(), any());
        verify(cartService, times(0)).delete(any());
    }

    @Test
    public void addToCart_UpdateEntry() {
        when(abstractOrderParameter.getQuantity()).thenReturn(3l);
        when(cartEntry.getQuantity()).thenReturn(1l);
        when(commerceCartService.getExistedEntry(abstractOrderParameter)).thenReturn(cartEntry);

        facade.addToCart(entryDTO);
        verify(abstractOrderParameter).setQuantity(4l);
        verify(commerceCartService).updateQuantityForCartEntry(abstractOrderParameter);
        verify(commerceCartDataConverter).convert(any(CartModel.class));
    }

    @Test
    public void addToCart_AddNewNormalEntry() {
        when(abstractOrderParameter.getQuantity()).thenReturn(3l);
        when(commerceCartService.getExistedEntry(abstractOrderParameter)).thenReturn(null);
        when(commerceCartService.addToCart(abstractOrderParameter)).thenReturn(modification);
        when(cartEntry.getComboType()).thenReturn(null);

        facade.addToCart(entryDTO);
        verify(commerceCartService, times(0)).updateQuantityForCartEntry(abstractOrderParameter);
        verify(commerceCartService, times(1)).addToCart(abstractOrderParameter);
        verify(productService, times(0)).checkIsCombo(anyLong(), anyLong(), anyInt());
        verify(commerceCartService, times(0)).addProductToCombo(any(CommerceAbstractOrderEntryParameter.class));
        verify(commerceCartDataConverter).convert(any(CartModel.class));
    }

    @Test
    public void addToCart_AddFixedComboEntry() {
        when(abstractOrderParameter.getQuantity()).thenReturn(3l);
        when(commerceCartService.getExistedEntry(abstractOrderParameter)).thenReturn(null);
        when(commerceCartService.addToCart(abstractOrderParameter)).thenReturn(modification);
        when(cartEntry.getComboType()).thenReturn(ComboType.FIXED_COMBO.toString());

        facade.addToCart(entryDTO);
        verify(commerceCartService, times(0)).updateQuantityForCartEntry(abstractOrderParameter);
        verify(commerceCartService, times(1)).addToCart(abstractOrderParameter);
        verify(productService, times(0)).getCombo(anyLong(), anyLong());
        verify(commerceCartService, times(0)).addProductToCombo(any(CommerceAbstractOrderEntryParameter.class));
        verify(commerceCartDataConverter).convert(any(CartModel.class));
    }

    @Test
    public void addToCart_AddDynamicComboEntry() {
        when(abstractOrderParameter.getQuantity()).thenReturn(3l);
        when(commerceCartService.getExistedEntry(abstractOrderParameter)).thenReturn(null);
        when(commerceCartService.addToCart(abstractOrderParameter)).thenReturn(modification);
        when(cartEntry.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());
        when(entryDTO.getSubOrderEntries()).thenReturn(Arrays.asList(subentry1));
        when(productService.getProductInCombo(anyLong(), anyLong(), anyString())).thenReturn(Arrays.asList(productInComboData));
        when(comboData.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());

        facade.addToCart(entryDTO);
        verify(commerceCartService, times(0)).updateQuantityForCartEntry(abstractOrderParameter);
        verify(commerceCartService, times(1)).addToCart(abstractOrderParameter);
        verify(productService, times(1)).getCombo(anyLong(), anyLong());
        verify(commerceCartService, times(1)).addProductToCombo(any(CommerceAbstractOrderEntryParameter.class));
        verify(commerceCartDataConverter).convert(any(CartModel.class));
    }

    @Test(expected = ServiceException.class)
    public void updateCartEntry_invalidQty() {
        when(entryDTO.getQuantity()).thenReturn(null);
        facade.updateCartEntry(entryDTO);

    }

    @Test(expected = ServiceException.class)
    public void updateCartEntry_NotFoundCart() {
        when(entryDTO.getQuantity()).thenReturn(2l);
        when(commerceCartService.getStorefrontCart(anyString(), anyLong())).thenReturn(null);
        facade.updateCartEntry(entryDTO);
    }

    @Test
    public void updateCartEntry_Success() {
        when(entryDTO.getQuantity()).thenReturn(2l);
        when(commerceCartService.getStorefrontCart(anyString(), anyLong())).thenReturn(cartModel);
        when(commerceCartService.validate(any(CommerceCartValidateParam.class))).thenReturn(new CommerceCartValidateData());

        facade.updateCartEntry(entryDTO);
        verify(commerceCartService).updateQuantityForCartEntry(any());
        verify(commerceCartDataConverter).convert(cartModel);
    }

    @Test
    public void placeOrder_emptySetup() {
        try {
            StoreFrontCheckoutRequest request = new StoreFrontCheckoutRequest();
            request.setCompanyId(2L);
            when(orderStorefrontSetupService.findByCompanyId(anyLong())).thenReturn(null);
            facade.placeOrder(request);
            fail("Throw new Exception");
        } catch (ServiceException e) {
            ErrorCodes err = ErrorCodes.CANNOT_CREATE_STORE_FRONT_CART;
            assertEquals(e.getMessage(), err.message());
        }
    }

    @Test
    public void placeOrder_hasChangedPrice() {
        try {
            StoreFrontCheckoutRequest request = new StoreFrontCheckoutRequest();
            request.setCompanyId(2L);
            request.setCode("code");
            OrderStorefrontSetupModel setupModel = new OrderStorefrontSetupModel();
            setupModel.setOrderSourceId(2L);
            setupModel.setCompanyId(2L);
            setupModel.setWarehouseId(2L);
            CommerceCartModification commerceCartModification = new CommerceCartModification();
            commerceCartModification.setUpdatePrice(true);
            when(orderStorefrontSetupService.findByCompanyId(anyLong())).thenReturn(setupModel);
            when(commerceCartService.getStorefrontCart(anyString(), anyLong())).thenReturn(cartModel);
            when(commerceCartService.updateLatestPriceForEntries(any(CartModel.class))).thenReturn(commerceCartModification);
            facade.placeOrder(request);
            fail("Throw new Exception");
        } catch (ServiceException e) {
            ErrorCodes err = ErrorCodes.ORDER_ENTRY_PRICE_CHANGE;
            assertEquals(e.getMessage(), err.message());
        }
    }

    //FIXME remove
    @Ignore
    @Test
    public void placeOrder_hasChangedPricePromotion() {
        try {
            StoreFrontCheckoutRequest request = new StoreFrontCheckoutRequest();
            request.setCompanyId(2L);
            request.setFinalPrice(200000d);
            request.setCode("code");
            request.setCustomer(new CustomerRequest());
            OrderStorefrontSetupModel setupModel = new OrderStorefrontSetupModel();
            setupModel.setOrderSourceId(2L);
            setupModel.setCompanyId(2L);
            setupModel.setWarehouseId(2L);
            CommerceCartModification commerceCartModification = new CommerceCartModification();
            commerceCartModification.setUpdatePrice(false);
            when(cartModel.getFinalPrice()).thenReturn(220000d);
            when(orderStorefrontSetupService.findByCompanyId(anyLong())).thenReturn(setupModel);
            when(commerceCartService.getStorefrontCart(anyString(), anyLong())).thenReturn(cartModel);
            when(commerceCartService.updateLatestPriceForEntries(any(CartModel.class))).thenReturn(commerceCartModification);
            facade.placeOrder(request);
            fail("Must throw Exception");
        } catch (ServiceException e) {
            ErrorCodes err = ErrorCodes.ORDER_ENTRY_PRICE_CHANGE;
            assertEquals(e.getMessage(), err.message());
        }
    }

    @Test
    public void placeOrder() {
        StoreFrontCheckoutRequest request = new StoreFrontCheckoutRequest();
        request.setCompanyId(2L);
        request.setCode("code");
        request.setShippingFeeSettingId(2L);
        request.setDeliveryCost(20000d);
        request.setFinalPrice(200000d);
        CustomerRequest customerRequest = new CustomerRequest();
        AddressRequest addressRequest = new AddressRequest();
        addressRequest.setProvinceId(2L);
        addressRequest.setDistrictId(2L);
        customerRequest.setShippingAddress(addressRequest);
        request.setCustomer(customerRequest);
        OrderStorefrontSetupModel setupModel = new OrderStorefrontSetupModel();
        setupModel.setOrderSourceId(2L);
        setupModel.setCompanyId(2L);
        setupModel.setWarehouseId(2L);
        ShippingFeeData shippingFeeData = new ShippingFeeData();
        shippingFeeData.setShippingFee(20000d);
        shippingFeeData.setShippingCompanyId(2L);
        shippingFeeData.setShippingFeeSettingId(2L);
        CommerceCartModification commerceCartModification = new CommerceCartModification();
        commerceCartModification.setUpdatePrice(false);
        when(cartEntry.getQuantity()).thenReturn(1l);
        when(cartEntry.getProductId()).thenReturn(2l);
        when(cartModel.getEntries()).thenReturn(Arrays.asList(cartEntry));
        when(cartModel.getFinalPrice()).thenReturn(200000d);
        when(cartModel.getId()).thenReturn(1l);
        when(orderStorefrontSetupService.findByCompanyId(anyLong())).thenReturn(setupModel);
        when(commerceCartService.getStorefrontCart(anyString(), anyLong())).thenReturn(cartModel);
        when(commerceCartService.updateLatestPriceForEntries(any(CartModel.class))).thenReturn(commerceCartModification);
        when(commerceCartShippingFeeService.getValidateShippingFee(any(), any())).thenReturn(shippingFeeData);
        when(commercePlaceOrderStrategy.storefrontPlaceOrder(any(CommerceCheckoutParameter.class))).thenReturn(new OrderModel());
        when(cartService.findById(anyLong())).thenReturn(cartModel);

        facade.placeOrder(request);
        verify(commerceCartDataConverter).convert(any(OrderModel.class));
        verify(orderStorefrontSetupService).findByCompanyId(anyLong());
        verify(commerceCartService).getStorefrontCart(anyString(), anyLong());
        verify(commerceCartShippingFeeService).getValidateShippingFee(any(), any());
        verify(commercePlaceOrderStrategy).storefrontPlaceOrder(any());
    }

    @Test
    public void getCartDetail_null() {
        when(commerceCartService.getStorefrontCart(anyString(), anyLong())).thenReturn(null);
        facade.getCartDetail(2L, "code");
        verify(commerceCartDataConverter, times(0)).convert(any(CartModel.class));
    }

    @Test
    public void getCartDetail_UpdatePrice() {
        CommerceCartModification commerceCartModification = new CommerceCartModification();
        commerceCartModification.setUpdatePrice(false);
        when(commerceCartService.getStorefrontCart(anyString(), anyLong())).thenReturn(cartModel);
        when(commerceCartService.updateLatestPriceForEntries(any(CartModel.class))).thenReturn(commerceCartModification);
        when(commerceCartService.validate(any(CommerceCartValidateParam.class))).thenReturn(new CommerceCartValidateData());
        facade.getCartDetail(2L, "code");
        verify(commerceCartDataConverter, times(1)).convert(any(CartModel.class));
        verify(commerceCartService, times(1)).recalculate(any(CartModel.class), anyBoolean());
    }


    @Test
    public void getOrderByUser_emptyData() {
        OrderSearchRequest request = new OrderSearchRequest();
        request.setCompanyId(2L);
        when(userService.getCurrentUserId()).thenReturn(2L);
        PageRequest pageable = PageRequest.of(0, 20);
        Page<OrderSearchModel> modelPage = new PageImpl<>(new ArrayList<>(), pageable, 1);
        when(orderElasticSearchFacade.orderStorefrontSearch(any(), any())).thenReturn(modelPage);
        facade.getOrderByUser(request, PageRequest.of(0, 20));
        verify(orderElasticSearchFacade, times(1)).orderStorefrontSearch(any(OrderSearchRequest.class), any(PageRequest.class));
        verify(storefrontOrderDataConverter, times(0)).convertAll(anyList());
    }

    @Test
    public void getOrderByUser() {
        OrderSearchRequest request = new OrderSearchRequest();
        request.setCompanyId(2L);
        when(userService.getCurrentUserId()).thenReturn(2L);
        PageRequest pageable = PageRequest.of(0, 20);
        Page<OrderSearchModel> modelPage = new PageImpl<>(Arrays.asList(new OrderSearchModel()), pageable, 1);
        when(orderElasticSearchFacade.orderStorefrontSearch(any(), any())).thenReturn(modelPage);
        facade.getOrderByUser(request, PageRequest.of(0, 20));
        verify(orderElasticSearchFacade, times(1)).orderStorefrontSearch(any(OrderSearchRequest.class), any(PageRequest.class));
        verify(storefrontOrderDataConverter, times(1)).convertAll(anyList());
    }

    @Test
    public void cancelOrder_OrderNotValid() {
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(null);
        try {
            facade.cancelOrder(cancelReq);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_ID.message(), e.getMessage());
            verify(lockMock).unlock();
        }
    }

    @Test
    public void cancelOrder_OrderStatusNotConfirmed() {
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderMock);
        when(orderMock.getOrderStatus()).thenReturn(OrderStatus.NEW.code());
        try {
            facade.cancelOrder(cancelReq);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CUSTOMER_CAN_NOT_CANCEL_NOT_CONFIRMED_ORDER.message(), e.getMessage());
            verify(lockMock).unlock();
        }
    }

    @Test
    public void cancelOrder() {
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderMock);
        when(orderMock.getOrderStatus()).thenReturn(OrderStatus.CONFIRMED.code());
        facade.cancelOrder(cancelReq);
        verify(commerceChangeOrderStatusStrategy).changeStatusOrder(any(CommerceChangeOrderStatusParameter.class));
    }

    @Test
    public void getDetailOrder() {
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(orderMock);
        facade.getDetailOrder("code", 2L);
        verify(commerceCartDataConverter).convert(any(OrderModel.class));
    }

    @Test
    public void countOrderByUser() {
        facade.countOrderByUser(2L);
        verify(orderService).storefrontCountOrderByUser(any(OrderSearchRequest.class));
    }

    @Test
    public void updateAddressShipping() {
        StoreFrontCheckoutRequest request = new StoreFrontCheckoutRequest();
        request.setCompanyId(2L);
        request.setCode("code");
        request.setShippingFeeSettingId(2L);
        request.setDeliveryCost(20000d);
        request.setFinalPrice(200000d);
        CustomerRequest customerRequest = new CustomerRequest();
        AddressRequest addressRequest = new AddressRequest();
        addressRequest.setProvinceId(2L);
        addressRequest.setDistrictId(2L);
        customerRequest.setShippingAddress(addressRequest);
        request.setCustomer(customerRequest);
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(orderMock);
        when(commerceCartShippingFeeService.getValidateShippingFee(any(OrderModel.class), any(StoreFrontCheckoutRequest.class))).thenReturn(new ShippingFeeData());
        when(commercePlaceOrderStrategy.updateAddressShipping(any(OrderModel.class), any(ShippingFeeData.class), any(StoreFrontCheckoutRequest.class))).thenReturn(orderMock);
        facade.updateAddressShipping(request);
        verify(commerceCartDataConverter).convert(any(OrderModel.class));
    }
}
