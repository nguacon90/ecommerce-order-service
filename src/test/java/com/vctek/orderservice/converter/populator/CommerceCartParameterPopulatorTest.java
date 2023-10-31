package com.vctek.orderservice.converter.populator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.*;
import com.vctek.orderservice.dto.request.CheckPermissionRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.CheckPermissionClient;
import com.vctek.orderservice.feignclient.dto.DistributorSetingPriceData;
import com.vctek.orderservice.feignclient.dto.ProductSearchRequest;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.service.AuthService;
import com.vctek.orderservice.service.CartService;
import com.vctek.orderservice.service.LogisticService;
import com.vctek.orderservice.service.ProductService;
import com.vctek.orderservice.util.PriceType;
import com.vctek.redis.ProductData;
import com.vctek.util.OrderType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class CommerceCartParameterPopulatorTest {
    @Mock
    private CartService cartService;
    @Mock
    private AuthService authService;
    @Mock
    private CheckPermissionClient checkPermissionClient;
    @Mock
    private ProductService productService;
    @Mock
    private CheckPermissionData permission;
    @Mock
    private LogisticService logisticService;

    private CommerceCartParameterPopulator populator;
    private OrderEntryDTO orderEntryDTO = new OrderEntryDTO();
    private CommerceAbstractOrderParameter data = new CommerceAbstractOrderParameter();
    private CartModel cart = new CartModel();

    private ProductData productData = new ProductData();
    private PriceData priceData = new PriceData();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        orderEntryDTO.setQuantity(2l);
        orderEntryDTO.setProductId(2222l);
        orderEntryDTO.setOrderType(OrderType.RETAIL.toString());
        populator = new CommerceCartParameterPopulator(cartService, authService, checkPermissionClient);
        populator.setProductService(productService);
        populator.setLogisticService(logisticService);
        when(checkPermissionClient.checkPermission(any(CheckPermissionRequest.class))).thenReturn(permission);
        when(productService.getPriceOfProduct(anyLong(), anyInt())).thenReturn(priceData);
    }

    @Test
    public void populate_InvalidProductAvailableToSell() {
        try {
            when(productService.productIsAvailableToSell(anyLong())).thenReturn(false);
            populator.populate(orderEntryDTO, data);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.PRODUCT_STOP_SELLING.code(), e.getCode());
        }
    }

    @Test
    public void populate_cartNotExisted() {
        try {
            when(productService.productIsAvailableToSell(anyLong())).thenReturn(true);
            populator.populate(orderEntryDTO, data);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_ID.code(), e.getCode());
        }
    }

    @Test
    public void populate_BasePrice_WithHasEditPermission_NotValidPrice() {
        try {
            when(productService.productIsAvailableToSell(anyLong())).thenReturn(true);
            when(cartService.findByIdAndCompanyIdAndTypeAndCreateByUser(any(CartInfoParameter.class))).thenReturn(cart);
            when(permission.getPermission()).thenReturn(true);
            orderEntryDTO.setPrice(-2000d);
            populator.populate(orderEntryDTO, data);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_PRODUCT_PRICE.code(), e.getCode());
        }
    }

    @Test
    public void populate_BasePrice_WithHasEditPermission_ValidPrice() {
        when(productService.productIsAvailableToSell(anyLong())).thenReturn(true);
        when(cartService.findByIdAndCompanyIdAndTypeAndCreateByUser(any(CartInfoParameter.class))).thenReturn(cart);
        when(permission.getPermission()).thenReturn(true);
        orderEntryDTO.setPrice(2000d);
        populator.populate(orderEntryDTO, data);
        assertEquals(2000d, data.getBasePrice(), 0);
    }

    @Test
    public void populate_BasePrice_WithHasNotEditPermission_EmptyPrice() {
        try {
            when(productService.productIsAvailableToSell(anyLong())).thenReturn(true);
            when(cartService.findByIdAndCompanyIdAndTypeAndCreateByUser(any(CartInfoParameter.class))).thenReturn(cart);
            when(permission.getPermission()).thenReturn(false);
            productData.setId(orderEntryDTO.getProductId());
            when(productService.getBasicProductDetail(anyLong())).thenReturn(productData);
            populator.populate(orderEntryDTO, data);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_PRODUCT_PRICE.code(), e.getCode());
        }
    }

    @Test
    public void populate_BasePrice_WithHasNotEditPermission_EmptyWholesalePrice() {
        try {
            cart.setType(OrderType.ONLINE.name());
            cart.setPriceType(PriceType.WHOLESALE_PRICE.name());
            when(productService.productIsAvailableToSell(anyLong())).thenReturn(true);
            when(cartService.findByIdAndCompanyIdAndTypeAndCreateByUser(any(CartInfoParameter.class))).thenReturn(cart);
            when(permission.getPermission()).thenReturn(false);
            productData.setId(orderEntryDTO.getProductId());
            when(productService.search(any(ProductSearchRequest.class))).thenReturn(new ArrayList<>());
            populator.populate(orderEntryDTO, data);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.PRODUCT_HAS_NOT_WHOLESALE_PRICE.code(), e.getCode());
        }
    }

    @Test
    public void populate_BasePrice_HasNotPermission_withPriceTypeDistributor_InvalidDistributor() {
        try {
            priceData.setPrice(2000d);
            cart.setType(OrderType.ONLINE.name());
            cart.setPriceType(PriceType.DISTRIBUTOR_PRICE.name());
            when(productService.productIsAvailableToSell(anyLong())).thenReturn(true);
            when(cartService.findByIdAndCompanyIdAndTypeAndCreateByUser(any(CartInfoParameter.class))).thenReturn(cart);
            when(permission.getPermission()).thenReturn(false);
            when(productService.getPriceOfProduct(anyLong(), anyInt())).thenReturn(priceData);
            productData.setId(orderEntryDTO.getProductId());
            when(productService.search(any(ProductSearchRequest.class))).thenReturn(new ArrayList<>());
            populator.populate(orderEntryDTO, data);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_DISTRIBUTOR_ID.message(), e.getMessage());
        }
    }

    @Test
    public void populate_BasePrice_HasPermission_withPriceTypeDistributor_InvalidRecommendedRetailPrice() {
        priceData.setPrice(2000d);
        cart.setDistributorId(2L);
        cart.setType(OrderType.ONLINE.name());
        cart.setPriceType(PriceType.DISTRIBUTOR_PRICE.name());
        when(productService.productIsAvailableToSell(anyLong())).thenReturn(true);
        when(cartService.findByIdAndCompanyIdAndTypeAndCreateByUser(any(CartInfoParameter.class))).thenReturn(cart);
        when(permission.getPermission()).thenReturn(true);
        when(productService.getPriceOfProduct(anyLong(), anyInt())).thenReturn(priceData);
        productData.setId(orderEntryDTO.getProductId());
        when(productService.search(any(ProductSearchRequest.class))).thenReturn(new ArrayList<>());
        populator.populate(orderEntryDTO, data);
        assertEquals(2000d, data.getRecommendedRetailPrice(), 0);
    }

    @Test
    public void populate_BasePrice_HasNotPermission_withPriceTypeDistributor() {
        DistributorSetingPriceData setingPriceData = new DistributorSetingPriceData();
        setingPriceData.setProductId(2222L);
        setingPriceData.setPrice(20000d);
        Map<Long, DistributorSetingPriceData> priceDataMap = new HashMap<>();
        priceDataMap.put(setingPriceData.getProductId(), setingPriceData);

        priceData.setPrice(2000d);
        cart.setType(OrderType.ONLINE.name());
        cart.setCompanyId(2L);
        cart.setDistributorId(2L);
        cart.setPriceType(PriceType.DISTRIBUTOR_PRICE.name());
        when(productService.productIsAvailableToSell(anyLong())).thenReturn(true);
        when(cartService.findByIdAndCompanyIdAndTypeAndCreateByUser(any(CartInfoParameter.class))).thenReturn(cart);
        when(permission.getPermission()).thenReturn(false);
        when(productService.getPriceOfProduct(anyLong(), anyInt())).thenReturn(priceData);
        when(logisticService.getProductPriceSetting(anyLong(), anyLong(), anyList())).thenReturn(priceDataMap);
        when(logisticService.calculateDistributorSettingPrice(any(DistributorSetingPriceData.class), anyDouble())).thenReturn(20000d);
        productData.setId(orderEntryDTO.getProductId());
        when(productService.search(any(ProductSearchRequest.class))).thenReturn(new ArrayList<>());
        populator.populate(orderEntryDTO, data);
        assertEquals(2000d, data.getRecommendedRetailPrice(), 0);
        assertEquals(20000d, data.getBasePrice(), 0);
    }

    @Test
    public void populate_BasePrice_HasPermission_withPriceTypeDistributor_NotSetting() {
        orderEntryDTO.setRecommendedRetailPrice(2000d);
        cart.setType(OrderType.ONLINE.name());
        cart.setCompanyId(2L);
        cart.setDistributorId(2L);
        cart.setPriceType(PriceType.DISTRIBUTOR_PRICE.name());
        priceData.setPrice(2000d);
        when(productService.productIsAvailableToSell(anyLong())).thenReturn(true);
        when(cartService.findByIdAndCompanyIdAndTypeAndCreateByUser(any(CartInfoParameter.class))).thenReturn(cart);
        when(permission.getPermission()).thenReturn(true);
        when(productService.getPriceOfProduct(anyLong(), anyInt())).thenReturn(priceData);
        when(logisticService.getProductPriceSetting(anyLong(), anyLong(), anyList())).thenReturn(new HashMap<>());
        productData.setId(orderEntryDTO.getProductId());
        when(productService.search(any(ProductSearchRequest.class))).thenReturn(new ArrayList<>());
        populator.populate(orderEntryDTO, data);
        assertEquals(2000d, data.getRecommendedRetailPrice(), 0);
        assertEquals(2000d, data.getBasePrice(), 0);
    }

    @Test
    public void populate_success() {
        when(productService.productIsAvailableToSell(anyLong())).thenReturn(true);
        when(cartService.findByIdAndCompanyIdAndTypeAndCreateByUser(any(CartInfoParameter.class))).thenReturn(cart);
        when(permission.getPermission()).thenReturn(false);
        priceData.setPrice(2000d);

        populator.populate(orderEntryDTO, data);
        assertEquals(priceData.getPrice(), priceData.getPrice());
    }
}
