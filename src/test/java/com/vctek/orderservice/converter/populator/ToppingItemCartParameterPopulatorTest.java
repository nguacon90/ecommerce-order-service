package com.vctek.orderservice.converter.populator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.PriceData;
import com.vctek.orderservice.dto.ToppingItemParameter;
import com.vctek.orderservice.dto.request.ToppingItemRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.CartEntryModel;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.model.ToppingOptionModel;
import com.vctek.orderservice.service.CartService;
import com.vctek.orderservice.service.ProductService;
import com.vctek.orderservice.service.ToppingOptionService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class ToppingItemCartParameterPopulatorTest {
    @Mock
    private CartService cartService;
    @Mock
    private ToppingOptionService toppingOptionService;
    @Mock
    private ProductService productService;

    private ToppingItemCartParameterPopulator populator;

    @Mock
    private ToppingItemRequest request;
    private ToppingItemParameter target;
    @Mock
    private CartModel cartMock;
    @Mock
    private CartEntryModel cartEntryMock;
    @Mock
    private ToppingOptionModel toppingOptionMock;
    @Mock
    private PriceData priceDataMock;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        populator = new ToppingItemCartParameterPopulator();
        populator.setCartService(cartService);
        populator.setToppingOptionService(toppingOptionService);
        populator.setProductService(productService);
        target = new ToppingItemParameter();
        when(request.getCompanyId()).thenReturn(1l);
        when(request.getProductId()).thenReturn(11l);
        when(request.getOrderCode()).thenReturn("765342");
        when(request.getEntryId()).thenReturn(0l);
        when(request.getQuantity()).thenReturn(2);
        when(priceDataMock.getPrice()).thenReturn(10000d);
    }

    @Test
    public void populate_InvalidCartCode() {
        try {
            when(productService.productIsAvailableToSell(anyLong())).thenReturn(true);
            when(cartService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(null);
            populator.populate(request, target);
            fail("Must throw exeption");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_ID.code(), e.getCode());
        }
    }

    @Test
    public void populate_InvalidCartEntry() {
        try {
            when(cartService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(cartMock);
            when(cartService.findEntryBy(eq(cartMock), anyInt())).thenReturn(null);
            populator.populate(request, target);
            fail("Must throw exeption");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ENTRY_NUMBER.code(), e.getCode());
        }
    }

    @Test
    public void populate_InvalidToppingModel() {
        try {
            when(cartService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(cartMock);
            when(cartService.findEntryBy(anyLong(), eq(cartMock))).thenReturn(cartEntryMock);
            populator.populate(request, target);
            fail("Must throw exeption");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_TOPPING_OPTION_ID.code(), e.getCode());
        }
    }

    @Test
    public void populate_ToppingNotAvailableToSell() {
        try {
            when(productService.productIsAvailableToSell(anyLong())).thenReturn(false);
            when(cartService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(cartMock);
            when(cartService.findEntryBy(anyLong(), eq(cartMock))).thenReturn(cartEntryMock);
            when(toppingOptionService.findByIdAndOrderEntry(anyLong(), eq(cartEntryMock))).thenReturn(toppingOptionMock);
            populator.populate(request, target);
            fail("Must throw exeption");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.PRODUCT_STOP_SELLING.code(), e.getCode());
        }
    }

    @Test
    public void populate_EmptyCompany() {
        try {
            when(request.getCompanyId()).thenReturn(null);
            populator.populate(request, target);
            fail("Must throw exeption");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_COMPANY_ID.code(), e.getCode());
        }
    }

    @Test
    public void populate_EmptyProductPrice() {
        try {
            when(productService.productIsAvailableToSell(anyLong())).thenReturn(false);
            when(cartService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(cartMock);
            when(cartService.findEntryBy(anyLong(), eq(cartMock))).thenReturn(cartEntryMock);
            when(toppingOptionService.findByIdAndOrderEntry(anyLong(), eq(cartEntryMock))).thenReturn(toppingOptionMock);
            when(productService.getPriceOfProduct(anyLong(), any())).thenReturn(null);
            populator.populate(request, target);
            fail("Must throw exeption");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.PRODUCT_STOP_SELLING.code(), e.getCode());
        }
    }

    @Test
    public void populate() {
        when(productService.productIsAvailableToSell(anyLong())).thenReturn(true);
        when(cartService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(cartMock);
        when(cartService.findEntryBy(anyLong(), eq(cartMock))).thenReturn(cartEntryMock);
        when(toppingOptionService.findByIdAndOrderEntry(anyLong(), eq(cartEntryMock))).thenReturn(toppingOptionMock);
        when(productService.getPriceOfProduct(anyLong(), any())).thenReturn(priceDataMock);
        populator.populate(request, target);
        assertEquals(cartMock, target.getAbstractOrderModel());
        assertEquals(cartEntryMock, target.getAbstractOrderEntryModel());
        assertEquals(toppingOptionMock, target.getToppingOptionModel());
        assertEquals(10000d, target.getPrice(), 0);
        assertEquals(11l, target.getProductId(), 0);
        assertEquals(2, target.getQuantity(), 0);
    }
}
