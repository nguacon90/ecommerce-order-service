package com.vctek.orderservice.converter.populator;

import com.vctek.orderservice.dto.AbstractOrderItemImportParameter;
import com.vctek.orderservice.dto.excel.OrderItemDTO;
import com.vctek.orderservice.elasticsearch.model.ProductSearchModel;
import com.vctek.orderservice.elasticsearch.service.ProductSearchService;
import com.vctek.orderservice.excel.RowMapperErrorCodes;
import com.vctek.orderservice.facade.PermissionFacade;
import com.vctek.orderservice.feignclient.dto.BasicProductData;
import com.vctek.orderservice.feignclient.dto.DistributorSetingPriceData;
import com.vctek.orderservice.feignclient.dto.ProductIsCombo;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.service.*;
import com.vctek.orderservice.strategy.CommerceUpdateCartEntryStrategy;
import com.vctek.orderservice.util.CurrencyType;
import com.vctek.orderservice.util.PriceType;
import com.vctek.orderservice.util.ProductTypeSell;
import com.vctek.redis.PriceData;
import com.vctek.util.ComboType;
import com.vctek.util.OrderType;
import com.vctek.util.PermissionCodes;
import com.vctek.util.SettingPriceType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.*;

public class OrderEntriesPopulatorTest {
    private OrderEntriesPopulator populator;
    @Mock
    private CartService cartServiceMock;
    @Mock
    private OrderService orderServiceMock;
    @Mock
    private ProductService productServiceMock;
    private AbstractOrderModel orderModel;
    private AbstractOrderItemImportParameter param;
    private List<OrderItemDTO> orderItems = new ArrayList<>();
    @Mock
    private OrderItemDTO itemMock1;
    @Mock
    private OrderItemDTO itemMock2;
    @Mock
    private OrderItemDTO itemMock3;
    @Mock
    private OrderItemDTO itemMock4;
    @Mock
    private OrderItemDTO itemMock5;
    @Mock
    private ProductSearchModel productMock1;
    @Mock
    private ProductSearchModel productMock2;
    @Mock
    private ProductSearchModel productMock3;
    @Mock
    private ProductSearchModel productMock4;
    @Mock
    private ProductSearchModel productMock5;
    @Mock
    private PriceData priceMock1;
    @Mock
    private PriceData priceMock2;
    @Mock
    private CommerceUpdateCartEntryStrategy commerceUpdateCartEntryStrategy;

    private CartEntryModel cartEntryModel1 = new CartEntryModel();
    private CartEntryModel cartEntryModel2 = new CartEntryModel();
    private CartEntryModel cartEntryModel3 = new CartEntryModel();
    private CartEntryModel cartEntryModel4 = new CartEntryModel();
    private CartEntryModel cartEntryModel5 = new CartEntryModel();
    @Mock
    private ProductIsCombo productIsComboMock;
    @Mock
    private PermissionFacade permissionFacade;
    @Mock
    private ComboPriceSettingService comboPriceSettingService;
    @Mock
    private LogisticService logisticService;
    @Mock
    private ProductSearchService productSearchService;
    private ArgumentCaptor<String> errorCapture = ArgumentCaptor.forClass(String.class);

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        populator = new OrderEntriesPopulator();
        populator.setCartService(cartServiceMock);
        populator.setOrderService(orderServiceMock);
        populator.setProductService(productServiceMock);
        populator.setPermissionFacade(permissionFacade);
        populator.setComboPriceSettingService(comboPriceSettingService);
        populator.setLogisticService(logisticService);
        populator.setProductSearchService(productSearchService);
        populator.setCommerceUpdateCartEntryStrategy(commerceUpdateCartEntryStrategy);
        param = new AbstractOrderItemImportParameter(orderItems);
        when(itemMock1.getSku()).thenReturn("sku1");
        when(itemMock1.getQuantity()).thenReturn("1");
        when(itemMock1.getDiscount()).thenReturn("10000");
        when(itemMock1.getDiscountType()).thenReturn(CurrencyType.CASH.toString());

        when(itemMock2.getSku()).thenReturn("sku2");
        when(itemMock2.getQuantity()).thenReturn("2");

        when(itemMock3.getSku()).thenReturn("sku3");
        when(itemMock3.getQuantity()).thenReturn("3");

        when(itemMock4.getSku()).thenReturn("sku4");
        when(itemMock4.getQuantity()).thenReturn("4");

        when(itemMock5.getSku()).thenReturn("sku5");
        when(itemMock5.getQuantity()).thenReturn("5");

        when(productMock1.getId()).thenReturn(1l);
        when(productMock1.getSku()).thenReturn("sku1");

        when(productMock2.getId()).thenReturn(2l);
        when(productMock2.getSku()).thenReturn("sku2");

        when(productMock3.getId()).thenReturn(3l);
        when(productMock3.getSku()).thenReturn("sku3");

        when(productMock4.getId()).thenReturn(4l);
        when(productMock4.getSku()).thenReturn("sku4");

        when(productMock5.getId()).thenReturn(5l);
        when(productMock5.getSku()).thenReturn("sku5");


        when(priceMock1.getPrice()).thenReturn(200000d);
        when(priceMock2.getPrice()).thenReturn(210000d);
        when(productMock1.getPrices()).thenReturn(Arrays.asList(priceMock1));
        when(productMock2.getPrices()).thenReturn(Arrays.asList(priceMock2));
        when(productMock3.getPrices()).thenReturn(Arrays.asList(priceMock1));
        when(productMock4.getPrices()).thenReturn(Arrays.asList(priceMock2));
        when(productMock5.getPrices()).thenReturn(Arrays.asList(priceMock2));
        when(productMock1.isBaseProduct()).thenReturn(false);
        when(productMock1.getTypeSell()).thenReturn(ProductTypeSell.SELLING.toString());
        when(productMock1.isAllowSell()).thenReturn(true);
        when(productMock2.isBaseProduct()).thenReturn(false);
        when(productMock2.getTypeSell()).thenReturn(ProductTypeSell.SELLING.toString());
        when(productMock2.isAllowSell()).thenReturn(true);
        when(productMock3.isBaseProduct()).thenReturn(false);
        when(productMock3.getTypeSell()).thenReturn(ProductTypeSell.SELLING.toString());
        when(productMock3.isAllowSell()).thenReturn(true);
        when(productMock4.isBaseProduct()).thenReturn(false);
        when(productMock4.getTypeSell()).thenReturn(ProductTypeSell.SELLING.toString());
        when(productMock4.isAllowSell()).thenReturn(true);
        when(productMock5.isBaseProduct()).thenReturn(false);
        when(productMock5.getTypeSell()).thenReturn(ProductTypeSell.SELLING.toString());
        when(productMock5.isAllowSell()).thenReturn(true);
        cartEntryModel1.setQuantity(1l);
        cartEntryModel1.setProductId(1l);
        cartEntryModel2.setQuantity(2l);
        cartEntryModel2.setProductId(2l);
        cartEntryModel3.setQuantity(3l);
        cartEntryModel3.setProductId(3l);
        cartEntryModel4.setQuantity(4l);
        cartEntryModel4.setProductId(4l);

        when(cartServiceMock.addNewEntry(any(), eq(1l), anyLong(), eq(true))).thenReturn(cartEntryModel1);
        when(cartServiceMock.addNewEntry(any(), eq(2l), anyLong(), eq(true))).thenReturn(cartEntryModel2);
        when(cartServiceMock.addNewEntry(any(), eq(3l), anyLong(), eq(true))).thenReturn(cartEntryModel3);
        when(cartServiceMock.addNewEntry(any(), eq(4l), anyLong(), eq(true))).thenReturn(cartEntryModel4);
        when(cartServiceMock.addNewEntry(any(), eq(5l), anyLong(), eq(true))).thenReturn(cartEntryModel5);
    }

    @Test
    public void populateCart_EmptyEntry_OnePage_NotContainCombo() {
        orderModel = new CartModel();
        orderModel.setCompanyId(1l);
        orderItems.add(itemMock1);
        orderItems.add(itemMock2);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1, productMock2));
        when(itemMock1.getProductData()).thenReturn(productMock1);
        when(itemMock2.getProductData()).thenReturn(productMock2);

        populator.populate(param, orderModel);
        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(2)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(0)).checkIsCombo(anyLong(), anyLong(), anyInt());

        assertEquals(200000d, cartEntryModel1.getBasePrice(), 0);
        assertNull(cartEntryModel1.getComboType());
        assertEquals(10000d, cartEntryModel1.getDiscount(), 0);
        assertEquals(CurrencyType.CASH.toString(), cartEntryModel1.getDiscountType());

        assertEquals(210000d, cartEntryModel2.getBasePrice(), 0);
        assertNull(cartEntryModel2.getComboType());
    }

    @Test
    public void populateCart_EmptyEntry_OnePage_notEmptyPrice() {
        orderModel = new CartModel();
        orderModel.setCompanyId(1l);
        orderItems.add(itemMock1);
        orderItems.add(itemMock2);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1, productMock2));
        when(itemMock1.getProductData()).thenReturn(productMock1);
        when(itemMock2.getProductData()).thenReturn(productMock2);
        when(itemMock2.getPrice()).thenReturn("22222");
        when(permissionFacade.hasPermission(anyString(), anyLong())).thenReturn(true);

        populator.populate(param, orderModel);
        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(2)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(0)).checkIsCombo(anyLong(), anyLong(), anyInt());

        assertEquals(200000d, cartEntryModel1.getBasePrice(), 0);
        assertNull(cartEntryModel1.getComboType());
        assertEquals(10000d, cartEntryModel1.getDiscount(), 0);
        assertEquals(CurrencyType.CASH.toString(), cartEntryModel1.getDiscountType());

        assertEquals(22222d, cartEntryModel2.getBasePrice(), 0);
        assertNull(cartEntryModel2.getComboType());
    }

    @Test
    public void populateCart_HasPermissionEditPrice_WholesalePriceType_EmptyWholesalePriceInProduct_ShouldGetPriceInExcel() {
        orderModel = new CartModel();
        orderModel.setCompanyId(1l);
        orderModel.setPriceType(PriceType.WHOLESALE_PRICE.toString());
        orderItems.add(itemMock1);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1));
        when(itemMock1.getProductData()).thenReturn(productMock1);
        when(itemMock1.getPrice()).thenReturn("15000");
        when(productMock1.getWholesalePrice()).thenReturn(null);
        when(permissionFacade.hasPermission(anyString(), anyLong())).thenReturn(true);

        populator.populate(param, orderModel);
        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(1)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(0)).checkIsCombo(anyLong(), anyLong(), anyInt());

        assertEquals(15000d, cartEntryModel1.getBasePrice(), 0);
    }

    @Test
    public void populateCart_HasPermissionEditPrice_WholesalePriceType_EmptyWholesalePriceInProduct_EmptyPriceInExcel_ERROR() {
        orderModel = new CartModel();
        orderModel.setCompanyId(1l);
        orderModel.setPriceType(PriceType.WHOLESALE_PRICE.toString());
        orderItems.add(itemMock1);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1));
        when(itemMock1.getProductData()).thenReturn(productMock1);
        when(itemMock1.getPrice()).thenReturn(null);
        when(productMock1.getWholesalePrice()).thenReturn(null);
        when(permissionFacade.hasPermission(anyString(), anyLong())).thenReturn(true);

        populator.populate(param, orderModel);

        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(0)).checkIsCombo(anyLong(), anyLong(), anyInt());
        List<OrderItemDTO> orderItems = param.getOrderItems();
        assertEquals(1, orderItems.size());
        verify(itemMock1).setError(errorCapture.capture());
        assertEquals(RowMapperErrorCodes.PRODUCT_HAS_NOT_WHOLESALE_PRICE.toString(), errorCapture.getValue());
    }

    @Test
    public void populateCart_HasNotPermissionEditPrice_WholesalePriceType_EmptyWholesalePriceInProduct_ERROR() {
        orderModel = new CartModel();
        orderModel.setCompanyId(1l);
        orderModel.setPriceType(PriceType.WHOLESALE_PRICE.toString());
        orderItems.add(itemMock1);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1));
        when(itemMock1.getProductData()).thenReturn(productMock1);
        when(itemMock1.getPrice()).thenReturn(null);
        when(productMock1.getWholesalePrice()).thenReturn(null);
        when(permissionFacade.hasPermission(anyString(), anyLong())).thenReturn(false);

        populator.populate(param, orderModel);

        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(0)).checkIsCombo(anyLong(), anyLong(), anyInt());
        List<OrderItemDTO> orderItems = param.getOrderItems();
        assertEquals(1, orderItems.size());
        verify(itemMock1).setError(errorCapture.capture());
        assertEquals(RowMapperErrorCodes.PRODUCT_HAS_NOT_WHOLESALE_PRICE.toString(), errorCapture.getValue());
    }

    @Test
    public void populateCart_HasNotPermissionEditPrice_WholesalePriceType_EmptyWholesalePriceInProduct_NotEmptyPriceExcel_ERROR() {
        orderModel = new CartModel();
        orderModel.setCompanyId(1l);
        orderModel.setPriceType(PriceType.WHOLESALE_PRICE.toString());
        orderItems.add(itemMock1);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1));
        when(itemMock1.getProductData()).thenReturn(productMock1);
        when(itemMock1.getPrice()).thenReturn("12000");
        when(productMock1.getWholesalePrice()).thenReturn(null);
        when(permissionFacade.hasPermission(anyString(), anyLong())).thenReturn(false);

        populator.populate(param, orderModel);

        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(0)).checkIsCombo(anyLong(), anyLong(), anyInt());
        List<OrderItemDTO> orderItems = param.getOrderItems();
        assertEquals(1, orderItems.size());
        verify(itemMock1).setError(errorCapture.capture());
        assertEquals(RowMapperErrorCodes.PRODUCT_HAS_NOT_WHOLESALE_PRICE.toString(), errorCapture.getValue());
    }

    @Test
    public void populateCart_Combo_HasNotPermissionEditPrice_WholesalePriceType_EmptyWholesalePriceInProduct_NotEmptyPriceExcel_ERROR() {
        orderModel = new CartModel();
        orderModel.setCompanyId(1l);
        orderModel.setPriceType(PriceType.WHOLESALE_PRICE.toString());
        orderItems.add(itemMock1);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1));
        when(itemMock1.getProductData()).thenReturn(productMock1);
        when(itemMock1.getPrice()).thenReturn("12000");
        when(productMock1.getWholesalePrice()).thenReturn(null);
        when(productMock1.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());
        when(permissionFacade.hasPermission(anyString(), anyLong())).thenReturn(false);

        populator.populate(param, orderModel);

        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(0)).checkIsCombo(anyLong(), anyLong(), anyInt());
        List<OrderItemDTO> orderItems = param.getOrderItems();
        assertEquals(1, orderItems.size());
        verify(itemMock1).setError(errorCapture.capture());
        assertEquals(RowMapperErrorCodes.PRODUCT_HAS_NOT_WHOLESALE_PRICE.toString(), errorCapture.getValue());
    }

    @Test
    public void populateCartOnline_HasNotPermissionEditPrice_WholesalePriceType_WholesalePriceInProduct_NotEmptyPriceExcel() {
        orderModel = new CartModel();
        orderModel.setType(OrderType.ONLINE.toString());
        orderModel.setCompanyId(1l);
        orderModel.setPriceType(PriceType.WHOLESALE_PRICE.toString());
        OrderItemDTO itemDTO = new OrderItemDTO();
        itemDTO.setSku("sku1");
        itemDTO.setQuantity("2");
        itemDTO.setPrice("12000");
        itemDTO.setProductData(productMock1);
        orderItems.add(itemDTO);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1));
        when(productMock1.getWholesalePrice()).thenReturn(11000d);
        when(permissionFacade.hasPermission(eq(PermissionCodes.EDIT_PRICE_ON_ORDER.code()), anyLong())).thenReturn(false);
        when(permissionFacade.hasPermission(eq(PermissionCodes.CAN_EDIT_COMBO_PRICE_ONLINE.code()), anyLong())).thenReturn(false);

        populator.populate(param, orderModel);

        assertEquals(null, itemDTO.getError());
        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(1)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(0)).checkIsCombo(anyLong(), anyLong(), anyInt());
        assertEquals(11000d, cartEntryModel1.getBasePrice(), 0);
    }

    @Test
    public void populateCartOnline_HasNotPermissionEditPrice_WholesalePriceType_WholesalePriceInProduct_EmptyPriceExcel() {
        orderModel = new CartModel();
        orderModel.setType(OrderType.ONLINE.toString());
        orderModel.setCompanyId(1l);
        orderModel.setPriceType(PriceType.WHOLESALE_PRICE.toString());
        orderItems.add(itemMock1);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1));
        when(itemMock1.getProductData()).thenReturn(productMock1);
        when(itemMock1.getPrice()).thenReturn("");
        when(productMock1.getWholesalePrice()).thenReturn(11000d);
        when(permissionFacade.hasPermission(anyString(), anyLong())).thenReturn(false);

        populator.populate(param, orderModel);

        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(1)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(0)).checkIsCombo(anyLong(), anyLong(), anyInt());
        assertEquals(11000d, cartEntryModel1.getBasePrice(), 0);
    }

    @Test
    public void populateCartOnline_HasPermissionEditPrice_RetailPriceType_WholeSaleEmptyInProduct() {
        orderModel = new CartModel();
        orderModel.setType(OrderType.ONLINE.toString());
        orderModel.setCompanyId(1l);
        orderModel.setPriceType(PriceType.RETAIL_PRICE.toString());
        orderItems.add(itemMock1);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1));
        when(itemMock1.getProductData()).thenReturn(productMock1);
        when(itemMock1.getPrice()).thenReturn("12000");
        when(productMock1.getWholesalePrice()).thenReturn(null);
        when(priceMock1.getPrice()).thenReturn(10000d);
        when(permissionFacade.hasPermission(anyString(), anyLong())).thenReturn(true);

        populator.populate(param, orderModel);

        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(1)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(0)).checkIsCombo(anyLong(), anyLong(), anyInt());
        assertEquals(12000d, cartEntryModel1.getBasePrice(), 0);
    }

    @Test
    public void populateCartOnline_HasPermissionEditPrice_RetailPriceType_PriceExcelEqualWholeSalePriceInProduct() {
        orderModel = new CartModel();
        orderModel.setType(OrderType.ONLINE.toString());
        orderModel.setCompanyId(1l);
        orderModel.setPriceType(PriceType.RETAIL_PRICE.toString());
        orderItems.add(itemMock1);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1));
        when(itemMock1.getProductData()).thenReturn(productMock1);
        when(itemMock1.getPrice()).thenReturn("12000");
        when(productMock1.getWholesalePrice()).thenReturn(12000d);
        when(priceMock1.getPrice()).thenReturn(10000d);
        when(permissionFacade.hasPermission(anyString(), anyLong())).thenReturn(true);

        populator.populate(param, orderModel);

        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(1)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(0)).checkIsCombo(anyLong(), anyLong(), anyInt());
        assertEquals(12000d, cartEntryModel1.getBasePrice(), 0);
    }

    @Test
    public void populateCartOnline_HasPermissionEditPrice_RetailPriceType_PriceExcelLargerWholeSalePriceInProduct() {
        orderModel = new CartModel();
        orderModel.setType(OrderType.ONLINE.toString());
        orderModel.setCompanyId(1l);
        orderModel.setPriceType(PriceType.RETAIL_PRICE.toString());
        orderItems.add(itemMock1);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1));
        when(itemMock1.getProductData()).thenReturn(productMock1);
        when(itemMock1.getPrice()).thenReturn("12001");
        when(productMock1.getWholesalePrice()).thenReturn(12000d);
        when(priceMock1.getPrice()).thenReturn(10000d);
        when(permissionFacade.hasPermission(anyString(), anyLong())).thenReturn(true);

        populator.populate(param, orderModel);

        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(1)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(0)).checkIsCombo(anyLong(), anyLong(), anyInt());
        assertEquals(12001, cartEntryModel1.getBasePrice(), 0);
    }

    @Test
    public void populateCartOnline_HasPermissionEditPrice_RetailPriceType_PriceExcelSmallerWholeSalePriceInProduct() {
        orderModel = new CartModel();
        orderModel.setType(OrderType.ONLINE.toString());
        orderModel.setCompanyId(1l);
        orderModel.setPriceType(PriceType.RETAIL_PRICE.toString());
        orderItems.add(itemMock1);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1));
        when(itemMock1.getProductData()).thenReturn(productMock1);
        when(itemMock1.getPrice()).thenReturn("11001");
        when(productMock1.getWholesalePrice()).thenReturn(12000d);
        when(priceMock1.getPrice()).thenReturn(10000d);
        when(permissionFacade.hasPermission(eq(PermissionCodes.EDIT_PRICE_ON_ORDER.code()), anyLong())).thenReturn(true);
        when(permissionFacade.hasPermission(eq(PermissionCodes.CAN_EDIT_COMBO_PRICE_ONLINE.code()), anyLong())).thenReturn(false);

        populator.populate(param, orderModel);

        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(0)).checkIsCombo(anyLong(), anyLong(), anyInt());

        verify(itemMock1).setError(errorCapture.capture());
        assertEquals(RowMapperErrorCodes.RETAIL_PRICE_MUST_BE_LARGE_WHOLESALE_PRICE.toString(), errorCapture.getValue());
    }

    @Test
    public void populateCartOnline_inValidProductSku() {
        orderModel = new CartModel();
        orderModel.setType(OrderType.ONLINE.toString());
        orderModel.setCompanyId(1l);
        orderModel.setPriceType(PriceType.RETAIL_PRICE.toString());
        orderItems.add(itemMock1);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(new ArrayList<>());
        when(itemMock1.getPrice()).thenReturn("11001");

        populator.populate(param, orderModel);

        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(0)).checkIsCombo(anyLong(), anyLong(), anyInt());

        verify(itemMock1).setError(errorCapture.capture());
        assertEquals(RowMapperErrorCodes.INVALID_PRODUCT_SKU.toString(), errorCapture.getValue());
    }

    @Test
    public void populateCartOnline_inValidProductBase() {
        orderModel = new CartModel();
        orderModel.setType(OrderType.ONLINE.toString());
        orderModel.setCompanyId(1l);
        orderModel.setPriceType(PriceType.RETAIL_PRICE.toString());
        orderItems.add(itemMock1);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1));
        when(productMock1.isBaseProduct()).thenReturn(true);
        when(itemMock1.getPrice()).thenReturn("11001");

        populator.populate(param, orderModel);

        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(0)).checkIsCombo(anyLong(), anyLong(), anyInt());

        verify(itemMock1).setError(errorCapture.capture());
        assertEquals(RowMapperErrorCodes.NOT_ACCEPTED_BASE_PRODUCT.toString(), errorCapture.getValue());
    }

    @Test
    public void populateCartOnline_inValidStopSelling() {
        orderModel = new CartModel();
        orderModel.setType(OrderType.ONLINE.toString());
        orderModel.setCompanyId(1l);
        orderModel.setPriceType(PriceType.RETAIL_PRICE.toString());
        orderItems.add(itemMock1);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1));
        when(productMock1.isBaseProduct()).thenReturn(false);
        when(productMock1.getTypeSell()).thenReturn(ProductTypeSell.STOP_SELLING.toString());
        when(itemMock1.getPrice()).thenReturn("11001");

        populator.populate(param, orderModel);

        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(0)).checkIsCombo(anyLong(), anyLong(), anyInt());

        verify(itemMock1).setError(errorCapture.capture());
        assertEquals(RowMapperErrorCodes.STOP_SELLING.toString(), errorCapture.getValue());
    }

    @Test
    public void populateCartOnline_inValidNotAllowSelling() {
        orderModel = new CartModel();
        orderModel.setType(OrderType.ONLINE.toString());
        orderModel.setCompanyId(1l);
        orderModel.setPriceType(PriceType.RETAIL_PRICE.toString());
        orderItems.add(itemMock1);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1));
        when(productMock1.isBaseProduct()).thenReturn(false);
        when(productMock1.getTypeSell()).thenReturn(ProductTypeSell.SELLING.toString());
        when(productMock1.isAllowSell()).thenReturn(false);
        when(itemMock1.getPrice()).thenReturn("11001");

        populator.populate(param, orderModel);

        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(0)).checkIsCombo(anyLong(), anyLong(), anyInt());

        verify(itemMock1).setError(errorCapture.capture());
        assertEquals(RowMapperErrorCodes.NOT_ALLOW_SELL.toString(), errorCapture.getValue());
    }

    @Test
    public void populateCartOnline_inValidDistributor_priceType_DISTRIBUTOR_PRICE() {
        orderModel = new CartModel();
        orderModel.setType(OrderType.ONLINE.toString());
        orderModel.setCompanyId(1l);
        orderModel.setPriceType(PriceType.DISTRIBUTOR_PRICE.toString());
        orderItems.add(itemMock1);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1));
        when(itemMock1.getPrice()).thenReturn("11001");

        populator.populate(param, orderModel);

        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(0)).checkIsCombo(anyLong(), anyLong(), anyInt());

        verify(itemMock1).setError(errorCapture.capture());
        assertEquals(RowMapperErrorCodes.INVALID_DISTRIBUTOR_ID.toString(), errorCapture.getValue());
    }

    @Test
    public void populateCartOnline_HasNotEditPricePermission_productNotPrice_emptyExcelPrice() {
        orderModel = new CartModel();
        orderModel.setType(OrderType.ONLINE.toString());
        orderModel.setCompanyId(1l);
        orderModel.setDistributorId(1l);
        orderModel.setPriceType(PriceType.RETAIL_PRICE.toString());
        orderItems.add(itemMock1);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1));
        when(productMock1.getPrices()).thenReturn(new ArrayList<>());
        when(itemMock1.getPrice()).thenReturn("11001");
        when(permissionFacade.hasPermission(anyString(), anyLong())).thenReturn(false);

        populator.populate(param, orderModel);

        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(0)).checkIsCombo(anyLong(), anyLong(), anyInt());

        verify(itemMock1).setError(errorCapture.capture());
        assertEquals(RowMapperErrorCodes.PRODUCT_HAS_NOT_PRICE.toString(), errorCapture.getValue());
    }

    @Test
    public void populateCartOnline_HasPermissionEditPriceCombo_RetailPriceType_PriceExcelSmallerWholeSalePriceInProduct() {
        orderModel = new CartModel();
        orderModel.setType(OrderType.ONLINE.toString());
        orderModel.setCompanyId(1l);
        orderModel.setDistributorId(1l);
        orderModel.setPriceType(PriceType.RETAIL_PRICE.toString());
        orderItems.add(itemMock1);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1));
        when(itemMock1.getProductData()).thenReturn(productMock1);
        when(itemMock1.getPrice()).thenReturn("11001");
        when(productMock1.getWholesalePrice()).thenReturn(12000d);
        when(productMock1.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());
        when(priceMock1.getPrice()).thenReturn(10000d);
        when(permissionFacade.hasPermission(eq(PermissionCodes.EDIT_PRICE_ON_ORDER.code()), anyLong())).thenReturn(false);
        when(permissionFacade.hasPermission(eq(PermissionCodes.CAN_EDIT_COMBO_PRICE_ONLINE.code()), anyLong())).thenReturn(true);

        populator.populate(param, orderModel);

        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(0)).checkIsCombo(anyLong(), anyLong(), anyInt());

        verify(itemMock1).setError(errorCapture.capture());
        assertEquals(RowMapperErrorCodes.RETAIL_PRICE_MUST_BE_LARGE_WHOLESALE_PRICE.toString(), errorCapture.getValue());
    }

    @Test
    public void populateCartOnline_FixCombo_HasPermissionEditPrice_PriceExcelLargerPriceInCombo() {
        orderModel = new CartModel();
        orderModel.setCompanyId(1l);
        orderModel.setDistributorId(1l);
        orderItems.add(itemMock1);
        BasicProductData basicProductData1 = new BasicProductData();
        basicProductData1.setPrice(1000d);
        BasicProductData basicProductData2 = new BasicProductData();
        basicProductData2.setPrice(1000d);
        BasicProductData basicProductData3 = new BasicProductData();
        basicProductData3.setPrice(1000d);
        ProductIsCombo productIsCombo = new ProductIsCombo();
        productIsCombo.setComboProducts(Arrays.asList(basicProductData1, basicProductData2, basicProductData3));
        productIsCombo.setComboType(ComboType.FIXED_COMBO.toString());

        when(productMock1.getComboType()).thenReturn(ComboType.FIXED_COMBO.toString());
        when(itemMock1.getProductData()).thenReturn(productMock1);
        when(itemMock1.getPrice()).thenReturn("4000");
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1));
        when(permissionFacade.hasPermission(anyString(), anyLong())).thenReturn(true);
        when(productServiceMock.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(productIsCombo);

        populator.populate(param, orderModel);

        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(1)).checkIsCombo(anyLong(), anyLong(), anyInt());

        verify(itemMock1).setError(errorCapture.capture());
        assertEquals(RowMapperErrorCodes.INVALID_COMBO_PRICE_LESS_THAN.toString(), errorCapture.getValue());
    }

    @Test
    public void populateCartOnline_FixCombo_HasPermissionEditPrice_PriceExcelLargerPriceDefaultCombo() {
        orderModel = new CartModel();
        orderModel.setCompanyId(1l);
        orderModel.setDistributorId(1l);
        orderItems.add(itemMock1);
        OrderSettingModel orderSettingModel = new OrderSettingModel();
        orderSettingModel.setAmount(5000d);
        orderSettingModel.setType(CurrencyType.CASH.toString());
        BasicProductData basicProductData1 = new BasicProductData();
        basicProductData1.setPrice(1000d);
        BasicProductData basicProductData2 = new BasicProductData();
        basicProductData2.setPrice(1000d);
        BasicProductData basicProductData3 = new BasicProductData();
        basicProductData3.setPrice(4000d);
        ProductIsCombo productIsCombo = new ProductIsCombo();
        productIsCombo.setComboProducts(Arrays.asList(basicProductData1, basicProductData2, basicProductData3));
        productIsCombo.setComboType(ComboType.FIXED_COMBO.toString());

        when(productMock1.getComboType()).thenReturn(ComboType.FIXED_COMBO.toString());
        when(itemMock1.getProductData()).thenReturn(productMock1);
        when(itemMock1.getPrice()).thenReturn("4000");
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1));
        when(permissionFacade.hasPermission(anyString(), anyLong())).thenReturn(true);
        when(productServiceMock.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(productIsCombo);
        when(comboPriceSettingService.findByTypeAndCompanyId(anyString(), anyLong())).thenReturn(orderSettingModel);

        populator.populate(param, orderModel);

        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(1)).checkIsCombo(anyLong(), anyLong(), anyInt());
        verify(comboPriceSettingService, times(1)).findByTypeAndCompanyId(anyString(), anyLong());

        verify(itemMock1).setError(errorCapture.capture());
        assertEquals(RowMapperErrorCodes.INVALID_COMBO_PRICE_LARGER_THAN.toString(), errorCapture.getValue());
    }

    @Test
    public void populateCart_EmptyEntry_2Pages_NotContainCombo() {
        orderModel = new CartModel();
        orderModel.setCompanyId(1l);
        orderModel.setDistributorId(1l);
        orderItems.add(itemMock1);
        orderItems.add(itemMock2);
        orderItems.add(itemMock3);
        orderItems.add(itemMock4);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1, productMock2), Arrays.asList(productMock3, productMock4));
        when(itemMock1.getProductData()).thenReturn(productMock1);
        when(itemMock2.getProductData()).thenReturn(productMock2);
        when(itemMock3.getProductData()).thenReturn(productMock3);
        when(itemMock4.getProductData()).thenReturn(productMock4);
        populator.setDefaultPageSize(2);

        populator.populate(param, orderModel);
        verify(productSearchService, times(2)).findAllByCompanyId(any());
        verify(cartServiceMock, times(4)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(0)).checkIsCombo(anyLong(), anyLong(), anyInt());

        assertEquals(200000d, cartEntryModel1.getBasePrice(), 0);
        assertNull(cartEntryModel1.getComboType());
        assertEquals(10000d, cartEntryModel1.getDiscount(), 0);
        assertEquals(CurrencyType.CASH.toString(), cartEntryModel1.getDiscountType());

        assertEquals(210000d, cartEntryModel2.getBasePrice(), 0);
        assertNull(cartEntryModel2.getComboType());

        assertEquals(200000d, cartEntryModel3.getBasePrice(), 0);
        assertNull(cartEntryModel3.getComboType());

        assertEquals(210000d, cartEntryModel4.getBasePrice(), 0);
        assertNull(cartEntryModel4.getComboType());
    }

    @Test
    public void populateCart_EmptyEntry_3Pages_NotContainCombo() {
        orderModel = new CartModel();
        orderModel.setCompanyId(1l);
        orderModel.setDistributorId(1l);
        orderItems.add(itemMock1);
        orderItems.add(itemMock2);
        orderItems.add(itemMock3);
        orderItems.add(itemMock4);
        orderItems.add(itemMock5);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1, productMock2),
                Arrays.asList(productMock3, productMock4), Arrays.asList(productMock5));

        when(itemMock1.getProductData()).thenReturn(productMock1);
        when(itemMock2.getProductData()).thenReturn(productMock2);
        when(itemMock3.getProductData()).thenReturn(productMock3);
        when(itemMock4.getProductData()).thenReturn(productMock4);
        when(itemMock5.getProductData()).thenReturn(productMock5);

        populator.setDefaultPageSize(2);

        populator.populate(param, orderModel);
        verify(productSearchService, times(3)).findAllByCompanyId(any());
        verify(cartServiceMock, times(5)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(0)).checkIsCombo(anyLong(), anyLong(), anyInt());

        assertEquals(200000d, cartEntryModel1.getBasePrice(), 0);
        assertNull(cartEntryModel1.getComboType());
        assertEquals(10000d, cartEntryModel1.getDiscount(), 0);
        assertEquals(CurrencyType.CASH.toString(), cartEntryModel1.getDiscountType());

        assertEquals(210000d, cartEntryModel2.getBasePrice(), 0);
        assertNull(cartEntryModel2.getComboType());

        assertEquals(200000d, cartEntryModel3.getBasePrice(), 0);
        assertNull(cartEntryModel3.getComboType());

        assertEquals(210000d, cartEntryModel4.getBasePrice(), 0);
        assertNull(cartEntryModel4.getComboType());

        assertEquals(210000d, cartEntryModel5.getBasePrice(), 0);
        assertNull(cartEntryModel5.getComboType());
    }

    @Test
    public void populateCart_ExistedEntry_OnePage_NotContainCombo() {
        orderModel = new CartModel();
        orderModel.setEntries(Arrays.asList(cartEntryModel1));
        orderModel.setDistributorId(1l);
        cartEntryModel1.setProductId(1l);
        cartEntryModel1.setQuantity(3l);
        cartEntryModel1.setBasePrice(200000d);
        cartEntryModel1.setComboType("type");
        Set<SubOrderEntryModel> subOrderEntryModels = new HashSet<>();
        SubOrderEntryModel subOrderEntryModel = new SubOrderEntryModel();
        subOrderEntryModel.setQuantity(6);
        subOrderEntryModels.add(subOrderEntryModel);
        cartEntryModel1.setSubOrderEntries(subOrderEntryModels);

        orderModel.setCompanyId(1l);
        orderItems.add(itemMock1);
        orderItems.add(itemMock2);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1, productMock2));
        when(itemMock1.getProductData()).thenReturn(productMock1);
        when(itemMock2.getProductData()).thenReturn(productMock2);
        populator.populate(param, orderModel);
        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(1)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(0)).checkIsCombo(anyLong(), anyLong(), anyInt());

        assertEquals(200000d, cartEntryModel1.getBasePrice(), 0);
        assertEquals(4, cartEntryModel1.getQuantity(), 0);
        assertEquals(210000d, cartEntryModel2.getBasePrice(), 0);
        verify(commerceUpdateCartEntryStrategy).updateSubOrderEntryQty(any(), anyInt(), anyInt());
        assertNull(cartEntryModel2.getComboType());
    }

    @Test
    public void populateCart_ExistedEntry_OnePage_ContainFixedCombo() {
        orderModel = new CartModel();
        orderModel.setDistributorId(1l);
        orderModel.setEntries(Arrays.asList(cartEntryModel1));
        cartEntryModel1.setProductId(1l);
        cartEntryModel1.setQuantity(3l);
        cartEntryModel1.setBasePrice(200000d);

        orderModel.setCompanyId(1l);
        orderItems.add(itemMock1);
        orderItems.add(itemMock2);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1, productMock2));
        when(itemMock1.getProductData()).thenReturn(productMock1);
        when(itemMock2.getProductData()).thenReturn(productMock2);
        when(productMock2.getComboType()).thenReturn(ComboType.FIXED_COMBO.toString());
        cartEntryModel2.setQuantity(1l);
        cartEntryModel2.setProductId(2l);

        when(productServiceMock.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(productIsComboMock);
        when(productIsComboMock.isCombo()).thenReturn(true);
        when(productIsComboMock.getPrice()).thenReturn(240000d);
        when(productIsComboMock.getComboType()).thenReturn(ComboType.FIXED_COMBO.toString());
        when(productIsComboMock.getComboProducts()).thenReturn(Arrays.asList(new BasicProductData()));

        populator.populate(param, orderModel);
        verify(cartServiceMock).addSubOrderEntries(any(), anyList(), anyInt());
        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(1)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(1)).checkIsCombo(anyLong(), anyLong(), anyInt());

        assertEquals(200000d, cartEntryModel1.getBasePrice(), 0);
        assertEquals(4, cartEntryModel1.getQuantity(), 0);
        assertNull(cartEntryModel1.getComboType());

        assertEquals(240000d, cartEntryModel2.getBasePrice(), 0);
        assertEquals(ComboType.FIXED_COMBO.toString(), cartEntryModel2.getComboType());
    }

    @Test
    public void populateCart_ExistedEntry_OnePage_ContainMultiGroupCombo() {
        orderModel = new CartModel();
        orderModel.setDistributorId(1l);
        orderModel.setEntries(Arrays.asList(cartEntryModel1));
        cartEntryModel1.setProductId(1l);
        cartEntryModel1.setQuantity(3l);
        cartEntryModel1.setBasePrice(200000d);

        orderModel.setCompanyId(1l);
        orderItems.add(itemMock1);
        orderItems.add(itemMock2);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1, productMock2));
        when(itemMock1.getProductData()).thenReturn(productMock1);
        when(itemMock2.getProductData()).thenReturn(productMock2);
        when(productMock2.getComboType()).thenReturn(ComboType.MULTI_GROUP.toString());
        cartEntryModel2.setQuantity(1l);
        cartEntryModel2.setProductId(2l);

        when(productServiceMock.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(productIsComboMock);
        when(productIsComboMock.isCombo()).thenReturn(true);
        when(productIsComboMock.getPrice()).thenReturn(240000d);
        when(productIsComboMock.getComboType()).thenReturn(ComboType.MULTI_GROUP.toString());
        when(productIsComboMock.getComboProducts()).thenReturn(Arrays.asList(new BasicProductData()));

        populator.populate(param, orderModel);
        verify(cartServiceMock, times(0)).addSubOrderEntries(any(), anyList(), anyInt());
        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(1)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(1)).checkIsCombo(anyLong(), anyLong(), anyInt());

        assertEquals(200000d, cartEntryModel1.getBasePrice(), 0);
        assertEquals(4, cartEntryModel1.getQuantity(), 0);
        assertNull(cartEntryModel1.getComboType());

        assertEquals(240000d, cartEntryModel2.getBasePrice(), 0);
        assertEquals(ComboType.MULTI_GROUP.toString(), cartEntryModel2.getComboType());
    }

    /**
     * UserPermission: Hasnot EditPrice,
     * NormalProduct: Has Wholesale Price
     * PriceType: Wholesale
     * Excel: Empty price
     * Expected: entry price is wholesale price
     */

    @Test
    public void populateCart_Success_WholesalePriceType_Case1() {
        orderModel = new CartModel();
        orderModel.setCompanyId(1l);
        orderModel.setDistributorId(1l);
        orderModel.setType(OrderType.ONLINE.toString());
        orderModel.setPriceType(PriceType.WHOLESALE_PRICE.toString());
        orderItems.add(itemMock1);
        when(permissionFacade.hasPermission(eq(PermissionCodes.EDIT_PRICE_ON_ORDER.code()),
                anyLong())).thenReturn(false);
        when(permissionFacade.hasPermission(eq(PermissionCodes.CAN_EDIT_COMBO_PRICE_ONLINE.code()),
                anyLong())).thenReturn(false);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1));
        when(itemMock1.getProductData()).thenReturn(productMock1);
        when(productMock1.getWholesalePrice()).thenReturn(23000d);
        when(priceMock1.getPrice()).thenReturn(25000d);
        when(productServiceMock.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(productIsComboMock);
        when(productIsComboMock.isCombo()).thenReturn(false);

        populator.populate(param, orderModel);
        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(1)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(0)).checkIsCombo(anyLong(), anyLong(), anyInt());

        assertEquals(23000d, cartEntryModel1.getBasePrice(), 0);
        assertNull(cartEntryModel1.getComboType());
    }

    /**
     * UserPermission: Has EditPrice,
     * NormalProduct: Has Wholesale Price
     * PriceType: Wholesale
     * Excel: Empty price
     * Expected: entry price is wholesale price
     */
    @Test
    public void populateCart_Success_WholesalePriceType_Case2() {
        orderModel = new CartModel();
        orderModel.setCompanyId(1l);
        orderModel.setDistributorId(1l);
        orderModel.setType(OrderType.ONLINE.toString());
        orderModel.setPriceType(PriceType.WHOLESALE_PRICE.toString());
        orderItems.add(itemMock1);
        when(permissionFacade.hasPermission(eq(PermissionCodes.EDIT_PRICE_ON_ORDER.code()),
                anyLong())).thenReturn(true);
        when(permissionFacade.hasPermission(eq(PermissionCodes.CAN_EDIT_COMBO_PRICE_ONLINE.code()),
                anyLong())).thenReturn(false);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1));
        when(itemMock1.getProductData()).thenReturn(productMock1);
        when(productMock1.getWholesalePrice()).thenReturn(23000d);
        when(priceMock1.getPrice()).thenReturn(25000d);
        when(productServiceMock.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(productIsComboMock);
        when(productIsComboMock.isCombo()).thenReturn(false);

        populator.populate(param, orderModel);
        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(1)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(0)).checkIsCombo(anyLong(), anyLong(), anyInt());

        assertEquals(23000d, cartEntryModel1.getBasePrice(), 0);
        assertNull(cartEntryModel1.getComboType());
    }

    /**
     * UserPermission: Hasnot EditPrice,can edit combo price
     * NormalProduct: Has Wholesale Price
     * PriceType: Wholesale
     * Excel: Empty price
     * Expected: entry price is wholesale price
     */
    @Test
    public void populateCart_Success_WholesalePriceType_Case3() {
        orderModel = new CartModel();
        orderModel.setCompanyId(1l);
        orderModel.setDistributorId(1l);
        orderModel.setType(OrderType.ONLINE.toString());
        orderModel.setPriceType(PriceType.WHOLESALE_PRICE.toString());
        orderItems.add(itemMock1);
        when(permissionFacade.hasPermission(eq(PermissionCodes.EDIT_PRICE_ON_ORDER.code()),
                anyLong())).thenReturn(false);
        when(permissionFacade.hasPermission(eq(PermissionCodes.CAN_EDIT_COMBO_PRICE_ONLINE.code()),
                anyLong())).thenReturn(true);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1));
        when(itemMock1.getProductData()).thenReturn(productMock1);
        when(productMock1.getWholesalePrice()).thenReturn(23000d);
        when(priceMock1.getPrice()).thenReturn(25000d);
        when(productServiceMock.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(productIsComboMock);
        when(productIsComboMock.isCombo()).thenReturn(false);

        populator.populate(param, orderModel);
        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(1)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(0)).checkIsCombo(anyLong(), anyLong(), anyInt());

        assertEquals(23000d, cartEntryModel1.getBasePrice(), 0);
        assertNull(cartEntryModel1.getComboType());
    }

    /**
     * UserPermission: Has EditPrice
     * NormalProduct: Has Wholesale Price
     * PriceType: Wholesale
     * Excel: has price
     * Expected: entry price is excel price
     */
    @Test
    public void populateCart_Success_WholesalePriceType_Case4() {
        orderModel = new CartModel();
        orderModel.setCompanyId(1l);
        orderModel.setDistributorId(1l);
        orderModel.setType(OrderType.ONLINE.toString());
        orderModel.setPriceType(PriceType.WHOLESALE_PRICE.toString());
        orderItems.add(itemMock1);
        when(permissionFacade.hasPermission(eq(PermissionCodes.EDIT_PRICE_ON_ORDER.code()),
                any())).thenReturn(true);
        when(permissionFacade.hasPermission(eq(PermissionCodes.CAN_EDIT_COMBO_PRICE_ONLINE.code()),
                any())).thenReturn(false);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1));
        when(itemMock1.getProductData()).thenReturn(productMock1);
        when(itemMock1.getPrice()).thenReturn("20000");
        when(productMock1.getWholesalePrice()).thenReturn(23000d);
        when(priceMock1.getPrice()).thenReturn(25000d);
        when(productServiceMock.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(productIsComboMock);
        when(productIsComboMock.isCombo()).thenReturn(false);

        populator.populate(param, orderModel);
        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(1)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(0)).checkIsCombo(anyLong(), anyLong(), anyInt());

        assertEquals(20000, cartEntryModel1.getBasePrice(), 0);
        assertNull(cartEntryModel1.getComboType());
    }

    /**
     * UserPermission: Has EditPrice
     * ComboProduct: Has Wholesale Price
     * PriceType: Wholesale
     * Excel: has empty price
     * Expected: entry price is product price
     */
    @Test
    public void populateCart_Success_WholesalePriceType_Case5() {
        orderModel = new CartModel();
        orderModel.setCompanyId(1l);
        orderModel.setDistributorId(1l);
        orderModel.setType(OrderType.ONLINE.toString());
        orderModel.setPriceType(PriceType.WHOLESALE_PRICE.toString());
        orderItems.add(itemMock1);
        when(productMock1.getComboType()).thenReturn(ComboType.FIXED_COMBO.toString());
        when(permissionFacade.hasPermission(eq(PermissionCodes.EDIT_PRICE_ON_ORDER.code()),
                any())).thenReturn(false);
        when(permissionFacade.hasPermission(eq(PermissionCodes.CAN_EDIT_COMBO_PRICE_ONLINE.code()),
                any())).thenReturn(true);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1));
        when(itemMock1.getProductData()).thenReturn(productMock1);
        when(itemMock1.getPrice()).thenReturn("");
        when(productMock1.getWholesalePrice()).thenReturn(23000d);
        when(priceMock1.getPrice()).thenReturn(25000d);
        when(productServiceMock.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(productIsComboMock);
        when(productIsComboMock.isCombo()).thenReturn(true);

        populator.populate(param, orderModel);
        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(1)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(1)).checkIsCombo(anyLong(), anyLong(), anyInt());

        assertEquals(23000d, cartEntryModel1.getBasePrice(), 0);
        assertNull(cartEntryModel1.getComboType());
    }

    /**
     * UserPermission: Has EditPrice
     * ComboProduct: Has not Wholesale Price
     * PriceType: Wholesale
     * Excel: has price
     * Expected: entry price is excel price
     */
    @Test
    public void populateCart_Success_WholesalePriceType_Case6() {
        orderModel = new CartModel();
        orderModel.setCompanyId(1l);
        orderModel.setDistributorId(1l);
        orderModel.setType(OrderType.ONLINE.toString());
        orderModel.setPriceType(PriceType.WHOLESALE_PRICE.toString());
        OrderItemDTO itemDTO = new OrderItemDTO();
        itemDTO.setSku("sku1");
        itemDTO.setQuantity("2");
        itemDTO.setPrice("24000");
        orderItems.add(itemDTO);

        when(productMock1.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());
        when(productMock1.getId()).thenReturn(1l);
        when(permissionFacade.hasPermission(eq(PermissionCodes.EDIT_PRICE_ON_ORDER.code()),
                any())).thenReturn(true);
        when(permissionFacade.hasPermission(eq(PermissionCodes.CAN_EDIT_COMBO_PRICE_ONLINE.code()),
                any())).thenReturn(false);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1));
        itemDTO.setProductData(productMock1);

        when(productMock1.getWholesalePrice()).thenReturn(null);
        when(priceMock1.getPrice()).thenReturn(25000d);
        when(productServiceMock.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(productIsComboMock);
        when(productIsComboMock.isCombo()).thenReturn(true);

        populator.populate(param, orderModel);
        assertEquals(null, itemDTO.getError());
        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(1)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(2)).checkIsCombo(anyLong(), anyLong(), anyInt());

        assertEquals(24000, cartEntryModel1.getBasePrice(), 0);
        assertNull(cartEntryModel1.getComboType());
    }

    /**
     * UserPermission: Has EditComboPrice
     * ComboProduct: Has Wholesale Price
     * PriceType: Wholesale
     * Excel: has price
     * Expected: entry price is excel price
     */
    @Test
    public void populateCart_Success_WholesalePriceType_Case7() {
        orderModel = new CartModel();
        orderModel.setCompanyId(1l);
        orderModel.setDistributorId(1l);
        orderModel.setType(OrderType.ONLINE.toString());
        orderModel.setPriceType(PriceType.WHOLESALE_PRICE.toString());
        OrderItemDTO itemDTO = new OrderItemDTO();
        itemDTO.setSku("sku1");
        itemDTO.setQuantity("2");
        orderItems.add(itemDTO);
        when(productMock1.getComboType()).thenReturn(ComboType.MULTI_GROUP.toString());
        when(productMock1.getId()).thenReturn(1l);
        when(permissionFacade.hasPermission(eq(PermissionCodes.EDIT_PRICE_ON_ORDER.code()),
                any())).thenReturn(false);
        when(permissionFacade.hasPermission(eq(PermissionCodes.CAN_EDIT_COMBO_PRICE_ONLINE.code()),
                any())).thenReturn(true);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1));
        itemDTO.setProductData(productMock1);
        itemDTO.setPrice("24000");
        when(productMock1.getWholesalePrice()).thenReturn(23000d);
        when(priceMock1.getPrice()).thenReturn(25000d);
        when(productServiceMock.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(productIsComboMock);
        when(productIsComboMock.isCombo()).thenReturn(true);

        populator.populate(param, orderModel);
        assertEquals(null, itemDTO.getError());
        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(1)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(2)).checkIsCombo(anyLong(), anyLong(), anyInt());

        assertEquals(24000, cartEntryModel1.getBasePrice(), 0);
        assertNull(cartEntryModel1.getComboType());
    }

    /**
     * UserPermission: Has not EditPrice
     * NormalProduct: Has Wholesale Price
     * PriceType: Wholesale
     * Excel: has not price
     * Expected: entry price is product price
     */
    @Test
    public void populateCart_Success_WholesalePriceType_Case8() {
        orderModel = new CartModel();
        orderModel.setCompanyId(1l);
        orderModel.setDistributorId(1l);
        orderModel.setType(OrderType.ONLINE.toString());
        orderModel.setPriceType(PriceType.WHOLESALE_PRICE.toString());
        OrderItemDTO itemDTO = new OrderItemDTO();
        itemDTO.setSku("sku1");
        itemDTO.setQuantity("2");
        orderItems.add(itemDTO);
        when(productMock1.getId()).thenReturn(1l);
        when(permissionFacade.hasPermission(eq(PermissionCodes.EDIT_PRICE_ON_ORDER.code()),
                any())).thenReturn(false);
        when(permissionFacade.hasPermission(eq(PermissionCodes.CAN_EDIT_COMBO_PRICE_ONLINE.code()),
                any())).thenReturn(true);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1));
        itemDTO.setProductData(productMock1);
        itemDTO.setPrice("24000");
        when(productMock1.getWholesalePrice()).thenReturn(23000d);
        when(priceMock1.getPrice()).thenReturn(25000d);

        populator.populate(param, orderModel);
        assertEquals(null, itemDTO.getError());
        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(1)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(0)).checkIsCombo(anyLong(), anyLong(), anyInt());

        assertEquals(23000, cartEntryModel1.getBasePrice(), 0);
        assertNull(cartEntryModel1.getComboType());
    }

    /**
     * UserPermission: Has EditComboPrice
     * ComboProduct: Has Wholesale Price
     * PriceType: RetailPrice
     * Excel: has price
     * Expected: entry price is excel price
     */
    @Test
    public void populateCart_Success_RetailPriceType() {
        orderModel = new CartModel();
        orderModel.setCompanyId(1l);
        orderModel.setDistributorId(1l);
        orderModel.setType(OrderType.ONLINE.toString());
        orderModel.setPriceType(PriceType.RETAIL_PRICE.toString());
        OrderItemDTO itemDTO = new OrderItemDTO();
        itemDTO.setSku("sku1");
        itemDTO.setQuantity("2");
        orderItems.add(itemDTO);
        when(productMock1.getComboType()).thenReturn(ComboType.MULTI_GROUP.toString());
        when(productMock1.getId()).thenReturn(1l);
        when(permissionFacade.hasPermission(eq(PermissionCodes.EDIT_PRICE_ON_ORDER.code()),
                any())).thenReturn(false);
        when(permissionFacade.hasPermission(eq(PermissionCodes.CAN_EDIT_COMBO_PRICE_ONLINE.code()),
                any())).thenReturn(true);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1));
        itemDTO.setProductData(productMock1);
        itemDTO.setPrice("24000");
        when(productMock1.getWholesalePrice()).thenReturn(23000d);
        when(priceMock1.getPrice()).thenReturn(25000d);
        when(productServiceMock.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(productIsComboMock);
        when(productIsComboMock.isCombo()).thenReturn(true);

        populator.populate(param, orderModel);
        assertEquals(null, itemDTO.getError());
        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(1)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(2)).checkIsCombo(anyLong(), anyLong(), anyInt());

        assertEquals(24000, cartEntryModel1.getBasePrice(), 0);
        assertNull(cartEntryModel1.getComboType());
    }

    /**
     * UserPermission: Has not EditPrice
     * NormalProduct: Has Price
     * PriceType: distributor_price
     * Excel: has price
     * Expected: entry price is product price
     */
    @Test
    public void populatePriceOrderEntryWithExitsSuggestedDistributorPrice() {
        orderModel = new CartModel();
        orderModel.setCompanyId(1l);
        orderModel.setDistributorId(1l);
        orderModel.setType(OrderType.ONLINE.toString());
        orderModel.setPriceType(PriceType.DISTRIBUTOR_PRICE.toString());
        OrderItemDTO itemDTO = new OrderItemDTO();
        itemDTO.setSku("sku1");
        itemDTO.setQuantity("2");
        orderItems.add(itemDTO);
        when(productMock1.getId()).thenReturn(1l);
        when(permissionFacade.hasPermission(eq(PermissionCodes.EDIT_PRICE_ON_ORDER.code()),
                any())).thenReturn(false);
        when(permissionFacade.hasPermission(eq(PermissionCodes.CAN_EDIT_COMBO_PRICE_ONLINE.code()),
                any())).thenReturn(false);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1));
        itemDTO.setProductData(productMock1);
        itemDTO.setPrice("24000");
        when(priceMock1.getPrice()).thenReturn(25000d);
        when(productServiceMock.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(null);

        populator.populate(param, orderModel);
        assertEquals(null, itemDTO.getError());
        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(1)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(0)).checkIsCombo(anyLong(), anyLong(), anyInt());

        assertEquals(25000, cartEntryModel1.getRecommendedRetailPrice(), 0);
        assertEquals(25000, cartEntryModel1.getBasePrice(), 0);
        assertNull(cartEntryModel1.getComboType());
    }

    /**
     * UserPermission: Has EditPrice
     * NormalProduct: Has Price
     * PriceType: distributor_price
     * Excel: has not price
     * Expected: entry price is product price
     */
    @Test
    public void populatePriceOrderEntryWithExitsSuggestedDistributorPrice_case2() {
        orderModel = new CartModel();
        orderModel.setCompanyId(1l);
        orderModel.setDistributorId(1l);
        orderModel.setType(OrderType.ONLINE.toString());
        orderModel.setPriceType(PriceType.DISTRIBUTOR_PRICE.toString());
        OrderItemDTO itemDTO = new OrderItemDTO();
        itemDTO.setSku("sku1");
        itemDTO.setQuantity("2");
        orderItems.add(itemDTO);
        when(productMock1.getId()).thenReturn(1l);
        when(permissionFacade.hasPermission(eq(PermissionCodes.EDIT_PRICE_ON_ORDER.code()),
                any())).thenReturn(true);
        when(permissionFacade.hasPermission(eq(PermissionCodes.CAN_EDIT_COMBO_PRICE_ONLINE.code()),
                any())).thenReturn(false);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1));
        itemDTO.setProductData(productMock1);
        itemDTO.setPrice(null);
        when(priceMock1.getPrice()).thenReturn(25000d);
        when(productServiceMock.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(null);

        populator.populate(param, orderModel);
        assertEquals(null, itemDTO.getError());
        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(1)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(0)).checkIsCombo(anyLong(), anyLong(), anyInt());

        assertEquals(25000, cartEntryModel1.getRecommendedRetailPrice(), 0);
        assertEquals(25000, cartEntryModel1.getBasePrice(), 0);
        assertNull(cartEntryModel1.getComboType());
    }

    /**
     * UserPermission: Has EditPrice
     * NormalProduct: Has Price
     * PriceType: distributor_price
     * Excel: has not price
     * SettingPriceDistributor: price net
     * Expected: entry price is product price
     */
    @Test
    public void populatePriceOrderEntryWithExitsSuggestedDistributorPrice_case3() {
        orderModel = new CartModel();
        orderModel.setCompanyId(1l);
        orderModel.setDistributorId(1l);
        orderModel.setType(OrderType.ONLINE.toString());
        orderModel.setPriceType(PriceType.DISTRIBUTOR_PRICE.toString());
        orderModel.getEntries().add(cartEntryModel1);
        OrderItemDTO itemDTO = new OrderItemDTO();
        itemDTO.setSku("sku1");
        itemDTO.setQuantity("2");
        itemDTO.setProductData(productMock1);
        orderItems.add(itemDTO);
        Map<Long, DistributorSetingPriceData> priceSetting = new HashMap<>();
        DistributorSetingPriceData setingPriceData = new DistributorSetingPriceData();
        setingPriceData.setPrice(20000d);
        setingPriceData.setType(SettingPriceType.PRICE_NET.toString());
        priceSetting.put(2L, setingPriceData);

        when(productMock1.getId()).thenReturn(2l);
        when(permissionFacade.hasPermission(eq(PermissionCodes.EDIT_PRICE_ON_ORDER.code()),
                any())).thenReturn(true);
        when(permissionFacade.hasPermission(eq(PermissionCodes.CAN_EDIT_COMBO_PRICE_ONLINE.code()),
                any())).thenReturn(false);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1));
        when(priceMock1.getPrice()).thenReturn(25000d);
        when(cartServiceMock.addNewEntry(any(CartModel.class), anyLong(), anyLong(), anyBoolean())).thenReturn(cartEntryModel1);
        when(productServiceMock.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(null);
        when(logisticService.getProductPriceSetting(anyLong(), anyLong(), anyList())).thenReturn(priceSetting);
        when(logisticService.calculateDistributorSettingPrice(any(DistributorSetingPriceData.class), anyDouble())).thenReturn(20000d);

        populator.populate(param, orderModel);
        assertEquals(null, itemDTO.getError());
        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(1)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(0)).checkIsCombo(anyLong(), anyLong(), anyInt());

        assertEquals(25000, cartEntryModel1.getRecommendedRetailPrice(), 0);
        assertEquals(20000, cartEntryModel1.getBasePrice(), 0);
        assertNull(cartEntryModel1.getComboType());
    }

    /**
     * UserPermission: Has EditPrice
     * NormalProduct: Has Price
     * PriceType: distributor_price
     * Excel: has not price
     * SettingPriceDistributor: price has discount
     * Expected: entry price is product price
     */
    @Test
    public void populatePriceOrderEntryWithExitsSuggestedRetailPrice_case4() {
        orderModel = new CartModel();
        orderModel.setCompanyId(1l);
        orderModel.setDistributorId(1l);
        orderModel.setType(OrderType.ONLINE.toString());
        orderModel.setPriceType(PriceType.DISTRIBUTOR_PRICE.toString());
        orderModel.getEntries().add(cartEntryModel1);
        OrderItemDTO itemDTO = new OrderItemDTO();
        itemDTO.setSku("sku1");
        itemDTO.setQuantity("2");
        itemDTO.setProductData(productMock1);
        orderItems.add(itemDTO);
        Map<Long, DistributorSetingPriceData> priceSetting = new HashMap<>();
        DistributorSetingPriceData setingPriceData = new DistributorSetingPriceData();
        setingPriceData.setDiscount(10000d);
        setingPriceData.setDiscountType(CurrencyType.CASH.toString());
        setingPriceData.setType(SettingPriceType.PRICE_BY_DISCOUNT.toString());
        priceSetting.put(2L, setingPriceData);

        when(productMock1.getId()).thenReturn(2l);
        when(permissionFacade.hasPermission(eq(PermissionCodes.EDIT_PRICE_ON_ORDER.code()),
                any())).thenReturn(true);
        when(permissionFacade.hasPermission(eq(PermissionCodes.CAN_EDIT_COMBO_PRICE_ONLINE.code()),
                any())).thenReturn(false);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1));
        when(priceMock1.getPrice()).thenReturn(25000d);
        when(cartServiceMock.addNewEntry(any(CartModel.class), anyLong(), anyLong(), anyBoolean())).thenReturn(cartEntryModel1);
        when(productServiceMock.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(null);
        when(logisticService.getProductPriceSetting(anyLong(), anyLong(), anyList())).thenReturn(priceSetting);
        when(logisticService.calculateDistributorSettingPrice(any(DistributorSetingPriceData.class), anyDouble())).thenReturn(14000d);

        populator.populate(param, orderModel);
        assertEquals(null, itemDTO.getError());
        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(1)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(0)).checkIsCombo(anyLong(), anyLong(), anyInt());

        assertEquals(25000, cartEntryModel1.getRecommendedRetailPrice(), 0);
        assertEquals(14000, cartEntryModel1.getBasePrice(), 0);
        assertNull(cartEntryModel1.getComboType());
    }

    /**
     * UserPermission: Has EditPrice
     * NormalProduct: Has Price
     * PriceType: distributor_price
     * Excel: has price
     * SettingPriceDistributor: has not setting
     * Expected: entry price is product price
     */
    @Test
    public void populatePriceOrderEntryWithExitsSuggestedRetailPrice_case5() {
        orderModel = new CartModel();
        orderModel.setCompanyId(1l);
        orderModel.setDistributorId(1l);
        orderModel.setType(OrderType.ONLINE.toString());
        orderModel.setPriceType(PriceType.DISTRIBUTOR_PRICE.toString());
        orderModel.getEntries().add(cartEntryModel1);
        OrderItemDTO itemDTO = new OrderItemDTO();
        itemDTO.setSku("sku1");
        itemDTO.setQuantity("2");
        orderItems.add(itemDTO);

        when(productMock1.getId()).thenReturn(2l);
        when(permissionFacade.hasPermission(eq(PermissionCodes.EDIT_PRICE_ON_ORDER.code()),
                any())).thenReturn(true);
        when(permissionFacade.hasPermission(eq(PermissionCodes.CAN_EDIT_COMBO_PRICE_ONLINE.code()),
                any())).thenReturn(false);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(Arrays.asList(productMock1));
        itemDTO.setProductData(productMock1);
        itemDTO.setPrice("24000");
        when(priceMock1.getPrice()).thenReturn(25000d);
        when(cartServiceMock.addNewEntry(any(CartModel.class), anyLong(), anyLong(), anyBoolean())).thenReturn(cartEntryModel1);
        when(productServiceMock.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(null);
        when(logisticService.getProductPriceSetting(anyLong(), anyLong(), anyList())).thenReturn(new HashMap<>());

        populator.populate(param, orderModel);
        assertEquals(null, itemDTO.getError());
        verify(productSearchService, times(1)).findAllByCompanyId(any());
        verify(cartServiceMock, times(1)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(orderServiceMock, times(0)).addNewEntry(any(), anyLong(), anyLong(), eq(true));
        verify(productServiceMock, times(0)).checkIsCombo(anyLong(), anyLong(), anyInt());

        assertEquals(25000, cartEntryModel1.getRecommendedRetailPrice(), 0);
        assertEquals(24000, cartEntryModel1.getBasePrice(), 0);
        assertNull(cartEntryModel1.getComboType());
    }

}
