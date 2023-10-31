package com.vctek.orderservice.facade.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vctek.converter.Converter;
import com.vctek.converter.Populator;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.*;
import com.vctek.orderservice.dto.excel.OrderItemDTO;
import com.vctek.orderservice.dto.request.*;
import com.vctek.orderservice.excel.OrderItemExcelFileReader;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.dto.CustomerData;
import com.vctek.orderservice.feignclient.dto.ProductSearchRequest;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.service.*;
import com.vctek.orderservice.util.CurrencyType;
import com.vctek.orderservice.util.PriceType;
import com.vctek.redis.elastic.ProductSearchData;
import com.vctek.sync.MutexFactory;
import com.vctek.sync.XMutexFactoryImpl;
import com.vctek.util.ComboType;
import com.vctek.util.OrderType;
import com.vctek.validate.Validator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class CartFacadeTest {
    @Mock
    private CartService cartService;
    @Mock
    private AuthService authService;
    @Mock
    private Converter<CartModel, CartData> cartConverter;
    @Mock
    private Converter<OrderEntryDTO, CommerceAbstractOrderParameter> commerceCartParameterConverter;
    @Mock
    private CommerceCartService commerceCartService;
    @Mock
    private Validator<List<OrderItemDTO>> importOrderItemValidator;
    private CartFacadeImpl facade;
    private CartInfoParameter param = new CartInfoParameter();
    private ArgumentCaptor<CartModel> captor = ArgumentCaptor.forClass(CartModel.class);
    private OrderEntryDTO orderEntryDTO = new OrderEntryDTO();
    private CommerceAbstractOrderParameter commerceCartParam = new CommerceAbstractOrderParameter();
    private RefreshCartRequest request = new RefreshCartRequest();
    private CartModel cart = new CartModel();
    private CartDiscountRequest cartDiscountRequest = new CartDiscountRequest();
    private VatRequest vatRequest = new VatRequest();
    @Mock
    private AppliedCouponRequest appliedCouponRequestMock;
    @Mock
    private CouponService couponServiceMock;
    @Mock
    private ProductService productService;

    private AddSubOrderEntryRequest addSubOrderEntryRequest;
    @Mock
    private Converter<AddSubOrderEntryRequest, CommerceAbstractOrderParameter> commerceSubEntryParameterConverter;
    @Mock
    private SubOrderEntryService subOrderEntryService;
    @Mock
    private MultipartFile multiplePartFileMock;
    @Mock
    private OrderItemExcelFileReader orderItemExcelFileReader;
    @Mock
    private Populator<AbstractOrderItemImportParameter, AbstractOrderModel> orderEntriesPopulator;
    @Mock
    private Converter<AbstractOrderEntryModel, OrderEntryData> orderEntryConverter;
    @Mock
    private CommerceCartModification modification;

    private ToppingOptionRequest toppingOptionRequest;

    private ToppingItemRequest toppingItemRequest;

    private MutexFactory<String> mutexFactory = new XMutexFactoryImpl<>();
    private ArgumentCaptor<ToppingOptionParameter> optionParamCaptor;
    @Mock
    private Populator<ToppingItemRequest, ToppingItemParameter> toppingItemParameterPopulator;

    @Mock
    private ToppingOptionService toppingOptionService;
    @Mock
    private UpdateOrderSequenceCacheService updateOrderSequenceCacheService;
    @Mock
    private LoyaltyService loyaltyService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private Validator<CommerceAbstractOrderParameter> saleOffUpdateQuantityOrderEntryValidator;
    @Mock
    private CustomerService customerService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        optionParamCaptor = ArgumentCaptor.forClass(ToppingOptionParameter.class);
        cartDiscountRequest.setCode("1l");
        cartDiscountRequest.setCompanyId(2l);
        vatRequest.setCode("1l");
        request.setCompanyId(2l);
        request.setOldCompanyId(2l);
        orderEntryDTO.setCompanyId(2l);
        addSubOrderEntryRequest = new AddSubOrderEntryRequest();
        facade = new CartFacadeImpl(cartService, authService, cartConverter);
        facade.setCommerceCartParameterConverter(commerceCartParameterConverter);
        facade.setCommerceCartService(commerceCartService);
        facade.setCouponService(couponServiceMock);
        facade.setProductService(productService);
        facade.setCommerceSubEntryParameterConverter(commerceSubEntryParameterConverter);
        facade.setSubOrderEntryService(subOrderEntryService);
        facade.setOrderItemExcelFileReader(orderItemExcelFileReader);
        facade.setOrderEntriesPopulator(orderEntriesPopulator);
        facade.setOrderEntryConverter(orderEntryConverter);
        facade.setMutexFactory(mutexFactory);
        facade.setToppingItemParameterPopulator(toppingItemParameterPopulator);
        facade.setImportOrderItemValidator(importOrderItemValidator);
        facade.setToppingOptionService(toppingOptionService);
        facade.setUpdateOrderSequenceCacheService(updateOrderSequenceCacheService);
        facade.setLoyaltyService(loyaltyService);
        facade.setObjectMapper(objectMapper);
        facade.setSaleOffUpdateQuantityCartEntryValidator(saleOffUpdateQuantityOrderEntryValidator);
        facade.setCustomerService(customerService);

        request.setCode("1l");

        when(authService.getCurrentUserId()).thenReturn(1l);
        when(updateOrderSequenceCacheService.isValidTimeRequest(any(), any(), any(), any())).thenReturn(true);

        cart.setId(1l);
        cart.setCompanyId(2l);
        cart.setWarehouseId(22l);
        toppingOptionRequest = new ToppingOptionRequest();
        toppingOptionRequest.setQuantity(1);
        toppingOptionRequest.setSugar(100);
        toppingOptionRequest.setIce(100);
        toppingOptionRequest.setEntryId(1l);
        toppingOptionRequest.setCompanyId(2l);

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
        vatRequest.setCompanyId(2l);
    }

    @Test
    public void getDetail_CartNotFound() {
        try {
            when(cartService.findByIdAndCompanyIdAndTypeAndCreateByUser(param)).thenReturn(null);
            facade.getDetail(param);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_ID.code(), e.getCode());
        }
    }

    @Test
    public void getDetail() {
        when(cartService.findByIdAndCompanyIdAndTypeAndCreateByUser(param)).thenReturn(new CartModel());
        when(cartService.save(any(CartModel.class))).thenReturn(cart);
        facade.getDetail(param);
        verify(cartConverter).convert(any(CartModel.class));
    }

    @Test
    public void createNewCart() {
        when(authService.getCurrentUserId()).thenReturn(2l);
        facade.createNewCart(param);
        assertEquals(2l, param.getUserId(), 0);
        verify(cartService).getOrCreateNewCart(param);
    }


    @Test
    public void createNewImageInCart() {
        OrderImagesRequest request = new OrderImagesRequest();
        request.setCompanyId(1l);
        OrderImageData orderImageData = new OrderImageData();
        orderImageData.setUrl("image");
        request.setOrderImages(Arrays.asList(orderImageData));
        when(cartService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(cart);
        facade.createNewImageInCart(request,"123");
        verify(cartService).save(any(CartModel.class));
    }

    @Test
    public void remove_cartNotFound() {
        try {
            when(cartService.findByIdAndCompanyIdAndTypeAndCreateByUser(param)).thenReturn(null);
            facade.remove(param);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_ID.code(), e.getCode());
        }
    }

    @Test
    public void remove_success() {
        when(cartService.findByIdAndCompanyIdAndTypeAndCreateByUser(param)).thenReturn(new CartModel());
        facade.remove(param);
        verify(cartService).delete(any(CartModel.class));
    }

    @Test
    public void addToCart() {
        commerceCartParam.setOrder(new CartModel());
        when(commerceCartParameterConverter.convert(orderEntryDTO)).thenReturn(commerceCartParam);
        facade.addToCart(orderEntryDTO);
        verify(commerceCartService).addToCart(commerceCartParam);
        verify(cartConverter).convert(any(CartModel.class));
    }

    @Test
    public void refresh_cartNotFound() {
        try {
            when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(null);

            facade.refresh(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_ID.code(), e.getCode());
        }
    }

    @Test
    public void refresh_NotClearCartData() {
        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
        when(cartService.save(cart)).thenReturn(cart);
        facade.refresh(request);
        verify(cartService, times(1)).save(cart);
    }

    @Test
    public void refresh_NotRemoveEntriesWhenChangeCompanyIdButCartEmpty() {
        request.setCompanyId(1l);
        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
        when(cartService.save(cart)).thenReturn(cart);
        facade.refresh(request);
        verify(cartService, times(1)).save(captor.capture());
        CartModel cartCapture = captor.getValue();
        assertEquals(1l, cartCapture.getCompanyId(), 0);
        assertEquals(null, cartCapture.getWarehouseId());
    }

    @Test
    public void refresh_NotRemoveEntriesWhenChangeWarehouseButCartEmpty() {
        request.setWarehouseId(11l);
        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
        when(cartService.save(cart)).thenReturn(cart);
        facade.refresh(request);
        verify(cartService, times(1)).save(captor.capture());
        CartModel cartCapture = captor.getValue();
        assertEquals(11l, cartCapture.getWarehouseId(), 0);
        assertEquals(2l, cartCapture.getCompanyId(), 0);
    }

    @Test
    public void refresh_NotRemoveEntriesWhenChangeWarehouse() {
        request.setWarehouseId(11l);
        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
        when(cartService.save(cart)).thenReturn(cart);
        cart.setEntries(Arrays.asList(new CartEntryModel()));

        facade.refresh(request);
        verify(cartService, times(1)).save(captor.capture());
        CartModel cartCapture = captor.getValue();
        assertEquals(11l, cartCapture.getWarehouseId(), 0);
        assertEquals(2l, cartCapture.getCompanyId(), 0);
        verify(commerceCartService, times(0)).removeAllEntries(any(CommerceAbstractOrderParameter.class));
    }

    @Test
    public void refresh_RemoveEntriesWhenChangeCompany() {
        request.setCompanyId(11l);
        cart.setCompanyId(2L);
        cart.setDistributorId(2L);
        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
        when(cartService.save(cart)).thenReturn(cart);
        cart.setEntries(Arrays.asList(new CartEntryModel()));

        facade.refresh(request);
        verify(cartService, times(1)).save(captor.capture());
        CartModel cartCapture = captor.getValue();
        assertNull(cartCapture.getWarehouseId());
        assertNull(cartCapture.getDistributorId());
        assertEquals(11l, cartCapture.getCompanyId(), 0);
        verify(commerceCartService, times(1)).removeAllEntries(any(CommerceAbstractOrderParameter.class));
    }

    @Test
    public void updateCartEntry_QuantityNull() {
        try {
            orderEntryDTO.setOrderCode("1l");
            orderEntryDTO.setEntryId(1l);
            when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(null);

            facade.updateCartEntry(orderEntryDTO);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_QUANTITY.code(), e.getCode());
        }
    }

    @Test
    public void updateCartEntry_CartNotFound() {
        try {
            orderEntryDTO.setOrderCode("1l");
            orderEntryDTO.setEntryId(1l);
            orderEntryDTO.setQuantity(2l);
            when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(null);

            facade.updateCartEntry(orderEntryDTO);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_ID.code(), e.getCode());
        }
    }

    @Test
    public void updateCartEntry() {
        orderEntryDTO.setOrderCode("1l");
        orderEntryDTO.setEntryId(1l);
        orderEntryDTO.setQuantity(2l);
        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);

        facade.updateCartEntry(orderEntryDTO);
        verify(commerceCartService).updateQuantityForCartEntry(any(CommerceAbstractOrderParameter.class));
        verify(cartConverter).convert(cart);
    }

    @Test
    public void updateDiscountOfCartEntry() {
        orderEntryDTO.setOrderCode("1l");
        orderEntryDTO.setEntryId(1l);
        orderEntryDTO.setQuantity(2l);
        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);

        facade.updateDiscountOfCartEntry(orderEntryDTO);

        verify(commerceCartService).updateDiscountForCartEntry(any(CommerceAbstractOrderParameter.class));
        verify(cartConverter).convert(cart);
    }

    @Test
    public void updateDiscountOfCart_InvalidDiscountType() {
        try {
            cartDiscountRequest.setDiscountType("aaa");

            when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);

            facade.updateDiscountOfCart(cartDiscountRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_DISCOUNT_TYPE.code(), e.getCode());
        }
    }

    @Test
    public void updateDiscountOfCart() {
        cartDiscountRequest.setDiscountType(CurrencyType.PERCENT.toString());
        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);

        facade.updateDiscountOfCart(cartDiscountRequest);
        verify(commerceCartService).updateDiscountForCart(any(CommerceAbstractOrderParameter.class));
        verify(cartConverter).convert(cart);
    }

    @Test
    public void updateVatOfCart_InvalidDiscountType() {
        try {
            vatRequest.setVatType("aaa");
            when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);

            facade.updateVatOfCart(vatRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_VAT_TYPE.code(), e.getCode());
        }
    }

    @Test
    public void updateVatOfCart() {
        vatRequest.setVatType(CurrencyType.PERCENT.toString());
        vatRequest.setVat(10d);
        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);

        facade.updateVatOfCart(vatRequest);
        verify(commerceCartService).updateVatForCart(any(CommerceAbstractOrderParameter.class));
        verify(cartConverter).convert(cart);
    }

    @Test
    public void updatePriceCartEntry_errorPriceRetail_smallThanWholesale() {
        try {
            orderEntryDTO.setPrice(12.2);
            orderEntryDTO.setOrderCode("12341234");
            orderEntryDTO.setEntryId(1l);
            orderEntryDTO.setProductId(1l);
            ProductSearchData searchData = new ProductSearchData();
            searchData.setWholesalePrice(20d);
            cart.setPriceType(PriceType.RETAIL_PRICE.name());
            cart.setType(OrderType.ONLINE.name());
            when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
            when(productService.search(any(ProductSearchRequest.class))).thenReturn(Arrays.asList(searchData));
            facade.updatePriceCartEntry(orderEntryDTO);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.RETAIL_PRICE_MUST_BE_LARGE_WHOLESALE_PRICE.code(), e.getCode());
        }
    }

    @Test
    public void updatePriceCartEntry() {
        orderEntryDTO.setPrice(12.2);
        orderEntryDTO.setOrderCode("12341234");
        orderEntryDTO.setEntryId(1l);
        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);

        facade.updatePriceCartEntry(orderEntryDTO);
        verify(commerceCartService).updatePriceForCartEntry(any(CommerceAbstractOrderParameter.class));
        verify(cartConverter).convert(cart);
    }

    @Test
    public void updateWeightForCartEntry() {
        orderEntryDTO.setWeight(1.3);
        orderEntryDTO.setOrderCode("12341234");
        orderEntryDTO.setEntryId(1l);
        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);

        facade.updateWeightForCartEntry(orderEntryDTO);
        verify(commerceCartService).updateWeightForOrderEntry(any(CommerceAbstractOrderParameter.class));
    }

    @Test
    public void applyCoupon() {
        when(appliedCouponRequestMock.getOrderCode()).thenReturn("0229397");
        when(appliedCouponRequestMock.getCompanyId()).thenReturn(1l);
        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);

        facade.applyCoupon(appliedCouponRequestMock);
        verify(couponServiceMock).redeemCoupon(any(CommerceRedeemCouponParameter.class));
    }

    @Test
    public void removeCoupon() {
        when(appliedCouponRequestMock.getOrderCode()).thenReturn("0229397");
        when(appliedCouponRequestMock.getCompanyId()).thenReturn(1l);
        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);

        facade.removeCoupon(appliedCouponRequestMock);
        verify(couponServiceMock).releaseCoupon(any(CommerceRedeemCouponParameter.class));
        verify(cartConverter).convert(cart);
    }

    @Test
    public void validateCombo_product_exceed_allowed_quantity() {
        try {
            ComboData comboData = new ComboData();
            comboData.setId(1l);
            comboData.setComboType(ComboType.ONE_GROUP.toString());
            comboData.setDuplicateSaleProduct(false);
            comboData.setTotalItemQuantity(3);

            ComboGroupProductData groupProductData = new ComboGroupProductData();
            groupProductData.setId(1l);
            groupProductData.setGroupNumber(1);
            groupProductData.setProductIds(Arrays.asList(1l));
            comboData.setComboGroupProductRequests(Arrays.asList(groupProductData));
            when(productService.getCombo(anyLong(), anyLong())).thenReturn(comboData);
            when(productService.getProductInCombo(anyLong(), anyLong(), anyString())).thenReturn(null);
            when(cartService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(new CartModel());
            CartEntryModel cartEntryModel = new CartEntryModel();
            cartEntryModel.setId(1l);
            cartEntryModel.setQuantity(1l);
            Set<SubOrderEntryModel> subOrderEntryModels = new HashSet<>();
            SubOrderEntryModel subOrderEntryModel = new SubOrderEntryModel();
            subOrderEntryModel.setId(1l);
            subOrderEntryModel.setProductId(1l);
            subOrderEntryModel.setQuantity(1);

            SubOrderEntryModel subOrderEntryModel2 = new SubOrderEntryModel();
            subOrderEntryModel2.setId(2l);
            subOrderEntryModel2.setProductId(2l);
            subOrderEntryModel2.setQuantity(2);

            subOrderEntryModels.add(subOrderEntryModel);
            subOrderEntryModels.add(subOrderEntryModel2);
            cartEntryModel.setSubOrderEntries(subOrderEntryModels);
            when(cartService.findEntryBy(anyLong(), any())).thenReturn(cartEntryModel);

            facade.addProductToCombo(addSubOrderEntryRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.PRODUCT_EXCEED_ALLOWED_QUANTITY.code(), e.getCode());
        }
    }

    @Test
    public void validateCombo_product_exceed_allowed_quantity_comboDuplicateSaleProduct() {
        try {
            ComboData comboData = new ComboData();
            comboData.setId(1l);
            comboData.setComboType(ComboType.ONE_GROUP.toString());
            comboData.setDuplicateSaleProduct(true);
            comboData.setTotalItemQuantity(3);
            addSubOrderEntryRequest.setQuantity(2);

            ComboGroupProductData groupProductData = new ComboGroupProductData();
            groupProductData.setId(1l);
            groupProductData.setGroupNumber(1);
            groupProductData.setGroupNumber(1);
            groupProductData.setProductIds(Arrays.asList(1l));
            comboData.setComboGroupProductRequests(Arrays.asList(groupProductData));
            when(productService.getCombo(anyLong(), anyLong())).thenReturn(comboData);
            when(productService.getProductInCombo(anyLong(), anyLong(), anyString())).thenReturn(null);
            when(cartService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(new CartModel());
            CartEntryModel cartEntryModel = new CartEntryModel();
            cartEntryModel.setId(1l);
            cartEntryModel.setQuantity(1l);
            Set<SubOrderEntryModel> subOrderEntryModels = new HashSet<>();
            SubOrderEntryModel subOrderEntryModel = new SubOrderEntryModel();
            subOrderEntryModel.setId(1l);
            subOrderEntryModel.setProductId(1l);
            subOrderEntryModel.setQuantity(1);

            SubOrderEntryModel subOrderEntryModel2 = new SubOrderEntryModel();
            subOrderEntryModel2.setId(2l);
            subOrderEntryModel2.setProductId(2l);
            subOrderEntryModel2.setQuantity(1);

            subOrderEntryModels.add(subOrderEntryModel);
            subOrderEntryModels.add(subOrderEntryModel2);
            cartEntryModel.setSubOrderEntries(subOrderEntryModels);
            when(cartService.findEntryBy(anyLong(), any())).thenReturn(cartEntryModel);

            facade.addProductToCombo(addSubOrderEntryRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.PRODUCT_EXCEED_ALLOWED_QUANTITY.code(), e.getCode());
        }
    }

    @Test
    public void validateCombo_not_existed_product() {
        try {
            ComboData comboData = new ComboData();
            comboData.setId(1l);
            comboData.setComboType(ComboType.ONE_GROUP.toString());
            comboData.setDuplicateSaleProduct(false);
            comboData.setTotalItemQuantity(3);
            ComboGroupProductData groupProductData = new ComboGroupProductData();
            groupProductData.setId(1l);
            groupProductData.setGroupNumber(1);
            groupProductData.setProductIds(Arrays.asList(1l));
            comboData.setComboGroupProductRequests(Arrays.asList(groupProductData));
            when(productService.getCombo(anyLong(), anyLong())).thenReturn(comboData);
            when(productService.getProductInCombo(anyLong(), anyLong(), anyString())).thenReturn(null);
            when(cartService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(new CartModel());
            CartEntryModel cartEntryModel = new CartEntryModel();
            cartEntryModel.setId(1l);
            cartEntryModel.setQuantity(1l);
            when(cartService.findEntryBy(anyLong(), any())).thenReturn(cartEntryModel);
            facade.addProductToCombo(addSubOrderEntryRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.NOT_EXISTED_PRODUCT_IN_COMBO.code(), e.getCode());
        }
    }

    @Test
    public void validateComboWithTypeMultyGroup_existed_product() {
        try {
            ComboData comboData = new ComboData();
            comboData.setId(1l);
            comboData.setComboType(ComboType.MULTI_GROUP.toString());
            comboData.setTotalItemQuantity(3);
            ComboGroupProductData groupProductData = new ComboGroupProductData();
            groupProductData.setId(1l);
            groupProductData.setGroupNumber(1);
            groupProductData.setProductIds(Arrays.asList(1l));
            comboData.setComboGroupProductRequests(Arrays.asList(groupProductData));
            when(productService.getCombo(anyLong(), anyLong())).thenReturn(comboData);
            ProductInComboData productInComboData = new ProductInComboData();
            productInComboData.setId(1l);
            productInComboData.setBarcode("122");
            productInComboData.setName("product");
            productInComboData.setPrice(12d);
            productInComboData.setSku("123");
            when(productService.getProductInCombo(anyLong(), anyLong(), anyString())).thenReturn(Arrays.asList(new ProductInComboData()));
            when(cartService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(new CartModel());
            CartEntryModel cartEntryModel = new CartEntryModel();
            cartEntryModel.setId(1l);
            cartEntryModel.setQuantity(1l);
            Set<SubOrderEntryModel> subOrderEntryModels = new HashSet<>();
            SubOrderEntryModel subOrderEntryModel = new SubOrderEntryModel();
            subOrderEntryModel.setId(1l);
            subOrderEntryModel.setProductId(1l);
            subOrderEntryModels.add(subOrderEntryModel);
            cartEntryModel.setSubOrderEntries(subOrderEntryModels);
            when(cartService.findEntryBy(anyLong(), any())).thenReturn(cartEntryModel);
            facade.addProductToCombo(addSubOrderEntryRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EXISTED_PRODUCT_IN_COMBO.code(), e.getCode());
        }
    }

    @Test
    public void addProductToCombo_existed_product() {
        try {
            ComboData comboData = new ComboData();
            comboData.setId(1l);
            comboData.setComboType(ComboType.ONE_GROUP.toString());
            comboData.setDuplicateSaleProduct(false);
            comboData.setTotalItemQuantity(3);
            ComboGroupProductData groupProductData = new ComboGroupProductData();
            groupProductData.setId(1l);
            groupProductData.setGroupNumber(1);
            groupProductData.setProductIds(Arrays.asList(1l));
            comboData.setComboGroupProductRequests(Arrays.asList(groupProductData));
            when(productService.getCombo(anyLong(), anyLong())).thenReturn(comboData);
            when(cartService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(new CartModel());
            when(productService.getProductInCombo(anyLong(), anyLong(), anyString())).thenReturn(null);
            CartEntryModel cartEntryModel = new CartEntryModel();
            cartEntryModel.setId(1l);
            cartEntryModel.setQuantity(1l);
            Set<SubOrderEntryModel> subOrderEntryModels = new HashSet<>();
            SubOrderEntryModel subOrderEntryModel = new SubOrderEntryModel();
            subOrderEntryModel.setId(1l);
            subOrderEntryModel.setProductId(1l);
            subOrderEntryModels.add(subOrderEntryModel);
            cartEntryModel.setSubOrderEntries(subOrderEntryModels);
            when(cartService.findEntryBy(anyLong(), any())).thenReturn(cartEntryModel);
            facade.addProductToCombo(addSubOrderEntryRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EXISTED_PRODUCT_IN_COMBO.code(), e.getCode());
        }
    }

    @Test
    public void addProductToCombo_success() {
        ComboData comboData = new ComboData();
        comboData.setId(1l);
        comboData.setComboType(ComboType.ONE_GROUP.toString());
        comboData.setDuplicateSaleProduct(true);
        comboData.setTotalItemQuantity(3);
        ComboGroupProductData groupProductData = new ComboGroupProductData();
        groupProductData.setId(1l);
        groupProductData.setGroupNumber(1);
        groupProductData.setProductIds(Arrays.asList(1l));
        addSubOrderEntryRequest.setQuantity(2);
        comboData.setComboGroupProductRequests(Arrays.asList(groupProductData));
        when(productService.getCombo(anyLong(), anyLong())).thenReturn(comboData);
        ProductInComboData productInComboData = new ProductInComboData();
        productInComboData.setId(1l);
        productInComboData.setBarcode("122");
        productInComboData.setName("product");
        productInComboData.setPrice(12d);
        productInComboData.setSku("123");
        when(productService.getProductInCombo(anyLong(), anyLong(), anyString())).thenReturn(Arrays.asList(productInComboData));
        when(cartService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(new CartModel());
        CartEntryModel cartEntryModel = new CartEntryModel();
        cartEntryModel.setId(1l);
        cartEntryModel.setQuantity(1l);
        when(cartService.findEntryBy(anyLong(), any())).thenReturn(cartEntryModel);
        when(commerceCartService.addProductToCombo(any(CommerceAbstractOrderEntryParameter.class)))
                .thenReturn(modification);
        when(modification.getEntry()).thenReturn(cartEntryModel);
        when(cartConverter.convert(any(CartModel.class))).thenReturn(new CartData());

        facade.addProductToCombo(addSubOrderEntryRequest);
        verify(cartConverter).convert(any(CartModel.class));
    }

    @Test
    public void addComboToOrderIndirectly() {
        CartModel cartModel = new CartModel();
        cartModel.setId(1l);
        CartEntryModel cartEntryModel = new CartEntryModel();
        cartEntryModel.setId(1l);
        cartEntryModel.setProductId(1l);
        cartEntryModel.setQuantity(1l);
        cartModel.setEntries(Arrays.asList(cartEntryModel));
        when(cartService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(new CartModel());
        when(cartService.findEntryBy(any(), anyInt())).thenReturn(cartEntryModel);
        ComboData comboData = new ComboData();
        comboData.setId(1l);
        comboData.setComboType(ComboType.FIXED_COMBO.toString());
        comboData.setPrice(12d);
        ComboGroupProductData comboGroupProductData = new ComboGroupProductData();
        comboGroupProductData.setGroupNumber(1);
        comboGroupProductData.setProductIds(Arrays.asList(1l, 2l, 3l));
        comboData.setComboGroupProductRequests(Arrays.asList(comboGroupProductData));
        when(productService.getCombo(anyLong(), anyLong())).thenReturn(comboData);
        commerceCartParam.setOrder(cartModel);
        when(commerceSubEntryParameterConverter.convert(any())).thenReturn(commerceCartParam);
        when(cartService.addNewEntry(any(), anyLong(), anyLong(), eq(false))).thenReturn(cartEntryModel);
        PriceData priceData = new PriceData();
        priceData.setId(1l);
        priceData.setPrice(2d);
        when(productService.getPriceOfProduct(anyLong(), anyInt())).thenReturn(priceData);
        facade.addComboToOrderIndirectly(addSubOrderEntryRequest);
        verify(cartConverter).convert(any(CartModel.class));
    }

    @Test
    public void cannot_removeSubEntry() {
        try {
            RemoveSubOrderEntryRequest removeSubOrderEntryRequest = new RemoveSubOrderEntryRequest();
            removeSubOrderEntryRequest.setCompanyId(1l);
            removeSubOrderEntryRequest.setOrderCode("123");
            removeSubOrderEntryRequest.setEntryId(1l);
            removeSubOrderEntryRequest.setSubEntryId(1l);
            CartEntryModel cartEntryModel = new CartEntryModel();
            cartEntryModel.setId(1l);
            cartEntryModel.setProductId(1l);
            cartEntryModel.setQuantity(1l);
            cartEntryModel.setBasePrice(2d);
            cartEntryModel.setComboType(ComboType.FIXED_COMBO.toString());
            when(cartService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(new CartModel());
            when(cartService.findEntryBy(anyLong(), any())).thenReturn(cartEntryModel);
            when(subOrderEntryService.findByOrderEntryAndId(any(), anyLong())).thenReturn(new SubOrderEntryModel());
            facade.removeSubEntry(removeSubOrderEntryRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CANNOT_REMOVE_SUB_ORDER_ENTRY.code(), e.getCode());
        }
    }

    @Test
    public void removeSubEntry_invalid_subOrderEntryId() {
        try {
            RemoveSubOrderEntryRequest removeSubOrderEntryRequest = new RemoveSubOrderEntryRequest();
            removeSubOrderEntryRequest.setCompanyId(1l);
            removeSubOrderEntryRequest.setOrderCode("123");
            removeSubOrderEntryRequest.setEntryId(1l);
            removeSubOrderEntryRequest.setSubEntryId(1l);
            CartEntryModel cartEntryModel = new CartEntryModel();
            cartEntryModel.setId(1l);
            cartEntryModel.setProductId(1l);
            cartEntryModel.setQuantity(1l);
            cartEntryModel.setBasePrice(2d);
            cartEntryModel.setComboType(ComboType.ONE_GROUP.toString());
            when(cartService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(new CartModel());
            when(cartService.findEntryBy(anyLong(), any())).thenReturn(cartEntryModel);
            when(subOrderEntryService.findByOrderEntryAndId(any(), anyLong())).thenReturn(null);
            facade.removeSubEntry(removeSubOrderEntryRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_SUB_ORDER_ENTRY_ID.code(), e.getCode());
        }
    }


    @Test
    public void removeSubEntry() {
        RemoveSubOrderEntryRequest removeSubOrderEntryRequest = new RemoveSubOrderEntryRequest();
        removeSubOrderEntryRequest.setCompanyId(1l);
        removeSubOrderEntryRequest.setOrderCode("123");
        removeSubOrderEntryRequest.setEntryId(1l);
        removeSubOrderEntryRequest.setSubEntryId(1l);
        CartEntryModel cartEntryModel = new CartEntryModel();
        cartEntryModel.setId(1l);
        cartEntryModel.setProductId(1l);
        cartEntryModel.setQuantity(1l);
        cartEntryModel.setBasePrice(2d);
        when(cartService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(new CartModel());
        when(cartService.findEntryBy(anyLong(), any())).thenReturn(cartEntryModel);
        when(subOrderEntryService.findByOrderEntryAndId(any(), anyLong())).thenReturn(new SubOrderEntryModel());
        facade.removeSubEntry(removeSubOrderEntryRequest);
        verify(cartService).saveEntry(cartEntryModel);
    }

    @Test
    public void importOrderItem_EmptyProductList() {
        try {
            when(cartService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(new CartModel());
            when(orderItemExcelFileReader.read(multiplePartFileMock)).thenReturn(Collections.emptyList());
            facade.importOrderItem("cartCode", 1l, multiplePartFileMock);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_IMPORT_ORDER_PRODUCT.code(), e.getCode());
        }
    }

    @Test
    public void importOrderItem() {
        when(cartService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(new CartModel());
        when(orderItemExcelFileReader.read(multiplePartFileMock)).thenReturn(Arrays.asList(new OrderItemDTO()));

        facade.importOrderItem("cartCode", 1l, multiplePartFileMock);
        verify(orderEntriesPopulator).populate(any(), any());
        verify(commerceCartService).recalculate(any(), eq(true));
        verify(cartConverter).convert(any());
    }

    @Test
    public void addEntryToppingToCart() {
        when(cartService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(cart);
        CartEntryModel cartEntryModel = new CartEntryModel();
        cartEntryModel.setId(1l);
        cartEntryModel.setQuantity(1l);
        when(cartService.findEntryBy(anyLong(), any())).thenReturn(cartEntryModel);
        facade.addToppingOption(toppingOptionRequest, "120");
        verify(commerceCartService).addToppingOption(optionParamCaptor.capture());
        ToppingOptionParameter param = optionParamCaptor.getValue();
        assertEquals(cart, param.getAbstractOrderModel());
        assertEquals(cartEntryModel, param.getAbstractOrderEntryModel());
        assertEquals(toppingOptionRequest.getIce(), param.getIce());
        assertEquals(toppingOptionRequest.getQuantity(), param.getQuantity());
        assertEquals(toppingOptionRequest.getSugar(), param.getSugar());
    }

    @Test
    public void addSubEntryToppingToCart_Invalid_Order_Entry_Topping() {
        facade.addToppingItem(toppingItemRequest);
        verify(toppingItemParameterPopulator).populate(eq(toppingItemRequest), any());
        verify(commerceCartService).addToppingItem(any());
    }

    @Test
    public void updateDiscountForToppingItem() {
        when(cartService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(cart);
        CartEntryModel cartEntryModel = new CartEntryModel();
        cartEntryModel.setId(1l);
        when(cartService.findEntryBy(anyLong(), any())).thenReturn(cartEntryModel);
        when(toppingOptionService.findByIdAndOrderEntry(anyLong(), any())).thenReturn(new ToppingOptionModel());
        facade.updateDiscountForToppingItem(toppingItemRequest);

        verify(commerceCartService).updateDiscountForToppingItem(any(ToppingItemParameter.class));
        verify(cartConverter).convert(cart);
    }

    @Test
    public void removeListCartEntry() {
        EntryRequest entryRequest = new EntryRequest();
        entryRequest.setOrderCode("123");
        entryRequest.setCompanyId(1l);
        entryRequest.setEntryIds("123");
        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);

        facade.removeListCartEntry(entryRequest);
        verify(commerceCartService).updateListOrderEntry(any(), any());
        verify(cartConverter).convert(cart);
    }

    @Test
    public void updatePriceForOrderEntries_isEmptyEntries() {
        param.setCode("code");
        param.setCompanyId(2l);
        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
        facade.updatePriceForCartEntries(param);
        verify(cartService).save(any(CartModel.class));
        verify(cartConverter).convert(any(CartModel.class));
    }

    @Test
    public void updatePriceForOrderEntries() {
        param.setCode("code");
        param.setCompanyId(2l);
        cart.setEntries(Arrays.asList(new CartEntryModel()));
        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
        facade.updatePriceForCartEntries(param);
        verify(commerceCartService).updatePriceForCartEntries(any(CartModel.class));
        verify(cartConverter).convert(any(CartModel.class));
    }

    @Test
    public void updatePriceForOrderEntries_priceTypeEqualsDistributorPrice() {
        try {
            param.setCode("code");
            param.setCompanyId(2l);
            param.setPriceType(PriceType.DISTRIBUTOR_PRICE.toString());
            cart.setType(OrderType.ONLINE.toString());
            cart.setEntries(Arrays.asList(new CartEntryModel()));
            when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
            facade.updatePriceForCartEntries(param);
            fail("Must new throw Exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_DISTRIBUTOR_ID.message(), e.getMessage());
        }
    }

    @Test
    public void getLoyaltyPointsFor() {
        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
        facade.getLoyaltyPointsFor("code", 2l);
        verify(loyaltyService).getLoyaltyPointsOf(any(CartModel.class));
    }

    @Test
    public void checkDiscountMaximum() {
        when(cartService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(cart);
        when(commerceCartService.checkDiscountMaximumOrder(any(CartModel.class))).thenReturn(Arrays.asList(new OrderSettingDiscountData()));
        List<OrderSettingDiscountData> dataList = facade.checkDiscountMaximum(2l, "code");
        assertEquals(1, dataList.size());
        verify(commerceCartService).checkDiscountMaximumOrder(any(CartModel.class));
    }

    @Test
    public void checkDiscountMaximum_setting_null() {
        when(cartService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(cart);
        List<OrderSettingDiscountData> dataList = facade.checkDiscountMaximum(2l, "code");
        assertEquals(0, dataList.size());
        verify(cartService).findByCodeAndCompanyId(anyString(), anyLong());
    }

    @Test
    public void updateAllDiscountForCart() {
        UpdateAllDiscountRequest request = new UpdateAllDiscountRequest();
        request.setCompanyId(1l);
        request.setProductIds(Arrays.asList(123l));
        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
        facade.updateAllDiscountForCart("123", request);
        verify(commerceCartService).updateAllDiscountForCart(any(CommerceAbstractOrderParameter.class), any());
        verify(cartConverter).convert(cart);
    }

    @Test
    public void updateRecommendedRetailPriceForCartEntry() {
        orderEntryDTO.setRecommendedRetailPrice(12.2);
        orderEntryDTO.setOrderCode("12341234");
        orderEntryDTO.setEntryId(1l);
        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
        when(commerceCartService.updateRecommendedRetailPriceForCartEntry(any())).thenReturn(false);
        when(cartConverter.convert(any())).thenReturn(new CartData());
        facade.updateRecommendedRetailPriceForCartEntry(orderEntryDTO);
        verify(commerceCartService).updateRecommendedRetailPriceForCartEntry(any(CommerceAbstractOrderParameter.class));
        verify(cartConverter).convert(cart);
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
        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
        when(commerceCartService.updateCustomer(any(UpdateCustomerRequest.class), any(AbstractOrderModel.class))).thenReturn(cart);
        facade.updateCustomer(request);
        verify(commerceCartService).updateCustomer(any(UpdateCustomerRequest.class), any(AbstractOrderModel.class));
        verify(cartConverter).convert(any(CartModel.class));
    }

}
