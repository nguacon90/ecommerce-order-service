package com.vctek.orderservice.strategy.impl;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.CommerceAbstractOrderParameter;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.CompanyClient;
import com.vctek.orderservice.feignclient.InventoryClient;
import com.vctek.orderservice.feignclient.dto.BasicProductData;
import com.vctek.orderservice.feignclient.dto.ProductIsCombo;
import com.vctek.orderservice.feignclient.dto.ProductStockData;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.service.*;
import com.vctek.orderservice.service.impl.DefaultCalculationService;
import com.vctek.orderservice.strategy.CommerceCartCalculationStrategy;
import com.vctek.orderservice.strategy.CommerceUpdateCartEntryStrategy;
import com.vctek.orderservice.util.PriceType;
import com.vctek.util.ComboType;
import com.vctek.util.OrderStatus;
import com.vctek.util.OrderType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class DefaultAddToCartStrategyTest {
    @Mock
    private ModelService modelService;

    @Mock
    private CartService cartService;

    @Mock
    private CommerceCartCalculationStrategy commerceCartCalculationStrategy;

    @Mock
    private OrderService orderService;

    @Mock
    private CompanyClient companyClient;

    @Mock
    private InventoryClient inventoryClient;
    @Mock
    private InventoryService inventoryService;
    @Mock
    private ProductStockData inventory;
    @Mock
    private ProductService productService;
    @Mock
    private DefaultCalculationService defaultCalculationService;
    @Mock
    private CommerceUpdateCartEntryStrategy commerceUpdateCartEntryStrategy;

    private DefaultAddToCartStrategy strategy;
    private CommerceAbstractOrderParameter param = new CommerceAbstractOrderParameter();
    private CartModel cart = new CartModel();
    private AbstractOrderEntryModel entry1 = new CartEntryModel();
    private AbstractOrderEntryModel entryFixedCombo = new CartEntryModel();
    private AbstractOrderEntryModel entryDynamicCombo = new CartEntryModel();
    private Long comboId = 22l;
    private Integer entryNumber = 0;
    private Long entry1ProductId = 223l;
    @Mock
    private ProductIsCombo productIsComboDataMock;
    private SubOrderEntryModel subEntry1;
    private SubOrderEntryModel subEntry2;
    private OrderModel order = new OrderModel();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        strategy = new DefaultAddToCartStrategy();
        strategy.setCartService(cartService);
        strategy.setCommerceCartCalculationStrategy(commerceCartCalculationStrategy);
        strategy.setModelService(modelService);
        strategy.setOrderService(orderService);
        strategy.setCompanyClient(companyClient);
        strategy.setProductService(productService);
        strategy.setInventoryService(inventoryService);
        strategy.setCommerceUpdateCartEntryStrategy(commerceUpdateCartEntryStrategy);
        cart.setWarehouseId(2l);
        cart.setCompanyId(1l);
        order.setCompanyId(1l);
        order.setWarehouseId(2l);
        param.setOrder(cart);
        entryFixedCombo.setComboType(ComboType.FIXED_COMBO.toString());
        entryFixedCombo.setId(1l);
        entryFixedCombo.setEntryNumber(1);
        entryDynamicCombo.setComboType(ComboType.ONE_GROUP.toString());
        entryDynamicCombo.setId(2l);
        entryDynamicCombo.setEntryNumber(2);
        entry1.setId(0l);
        entry1.setEntryNumber(entryNumber);
        entry1.setQuantity(2l);
        entry1.setProductId(entry1ProductId);
        List<AbstractOrderEntryModel> entries = new ArrayList<>();
        entries.add(entry1);
        entries.add(entryFixedCombo);
        entries.add(entryDynamicCombo);
        cart.setEntries(entries);

        subEntry1 = new SubOrderEntryModel();
        subEntry2 = new SubOrderEntryModel();
        entryFixedCombo.setSubOrderEntries(new LinkedHashSet<>(Arrays.asList(subEntry1, subEntry2)));
        when(inventoryClient.getAvailableStock(anyLong(), anyLong(), anyLong())).thenReturn(inventory);
    }

    @Test
    public void addToCart_CompanyAcceptOverSell() {
        when(companyClient.checkSellLessZero(anyLong())).thenReturn(true);
        param.setQuantity(2);
        param.setProductId(2222l);
        param.setCompanyId(2l);
        ProductIsCombo productIsCombo = new ProductIsCombo();
        productIsCombo.setCombo(Boolean.TRUE);
        productIsCombo.setComboType(ComboType.FIXED_COMBO.toString());
        BasicProductData productData = new BasicProductData();
        productData.setId(1l);
        productIsCombo.setComboProducts(Arrays.asList(productData));
        when(cartService.addNewEntry(any(CartModel.class), anyLong(), anyLong(), eq(false))).thenReturn(new CartEntryModel());
        when(productService.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(productIsCombo);

        strategy.addToCart(param);
        verify(modelService).save(any(CartModel.class));
        verify(inventoryClient, times(0)).getAvailableStock(anyLong(), anyLong(), anyLong());
    }

    @Test
    public void addToCart_CompanyNotAcceptOverSell_QtySmallerThanAvailableStock() {
        when(companyClient.checkSellLessZero(anyLong())).thenReturn(false);
        when(inventory.getQuantity()).thenReturn(1000);
        param.setQuantity(2);
        param.setProductId(2222l);
        param.setCompanyId(2l);
        ProductIsCombo productIsCombo = new ProductIsCombo();
        productIsCombo.setCombo(Boolean.TRUE);
        productIsCombo.setComboType(ComboType.FIXED_COMBO.toString());
        when(cartService.addNewEntry(any(CartModel.class), anyLong(), anyLong(), eq(false))).thenReturn(new CartEntryModel());
        when(productService.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(productIsCombo);


        strategy.addToCart(param);
        verify(modelService).save(any(CartModel.class));
    }

    @Test
    public void addToCart_CompanyNotAcceptOverSell_QtyLargerThanAvailableStock_ShouldNotAccept() {
            param.setQuantity(2);
            param.setProductId(2222l);
            param.setCompanyId(1l);
            param.setWarehouseId(406l);
            when(companyClient.checkSellLessZero(anyLong())).thenReturn(false);
            when(inventory.getQuantity()).thenReturn(0);
            when(cartService.addNewEntry(any(CartModel.class), anyLong(), anyLong(), eq(false)))
                    .thenReturn(new CartEntryModel());
            when(productService.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(productIsComboDataMock);
            when(productIsComboDataMock.isCombo()).thenReturn(false);

            strategy.addToCart(param);
            verify(inventoryService, times(1)).validateOutOfStock(any());
    }

    @Test
    public void addToCart_Combo_CompanyNotAcceptOverSell_QtyLargerThanAvailableStock_ShouldNotAccept() {
            param.setQuantity(2);
            param.setProductId(2222l);
            param.setCompanyId(1l);
            param.setWarehouseId(406l);
            when(companyClient.checkSellLessZero(anyLong())).thenReturn(false);
            when(inventory.getQuantity()).thenReturn(0);
            when(cartService.addNewEntry(any(CartModel.class), anyLong(), anyLong(), eq(false)))
                    .thenReturn(new CartEntryModel());
            when(productService.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(productIsComboDataMock);
            when(productIsComboDataMock.isCombo()).thenReturn(true);
            when(productIsComboDataMock.getId()).thenReturn(1212l);

            strategy.addToCart(param);
            verify(productService).checkAvailableToSale(any(), any());
            verify(inventoryService, times(0)).validateOutOfStock(any());
    }

    @Test
    public void addToOrder() {
        param.setQuantity(2);
        param.setCompanyId(1l);
        param.setProductId(2222l);
        param.setWarehouseId(11l);
        param.setOrder(order);
        when(inventory.getQuantity()).thenReturn(2);
        when(companyClient.checkSellLessZero(anyLong())).thenReturn(false);
        when(orderService.addNewEntry(any(OrderModel.class), anyLong(), anyLong(), eq(false))).thenReturn(new OrderEntryModel());
        when(productService.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(new ProductIsCombo());
        strategy.addEntryToOrder(param);
        verify(modelService).save(any(OrderModel.class));
        verify(commerceCartCalculationStrategy).recalculateCart(any(CommerceAbstractOrderParameter.class));;
    }

    @Test
    public void changeOrderEntryToComboEntry_AddTheSameFixedComboExistedInCart() {
        param.setComboId(comboId);
        param.setProductId(entry1ProductId);
        param.setOrder(cart);
        param.setEntryId(0l);
        entryFixedCombo.setProductId(comboId);
        entryFixedCombo.setQuantity(4l);
        subEntry1.setQuantity(4);
        subEntry2.setQuantity(4);


        strategy.changeOrderEntryToComboEntry(param);
        assertEquals(2, cart.getEntries().size());
        assertEquals(5, cart.getEntries().get(0).getQuantity(), 0);
        assertEquals(5, subEntry1.getQuantity(), 0);
        assertEquals(5, subEntry2.getQuantity(), 0);
        assertFalse(cart.isCalculated());
        verify(modelService).save(cart);
        verify(commerceCartCalculationStrategy).recalculateCart(param);
    }

    @Test
    public void changeOrderEntryToComboEntry_AddTheSameDynamicComboExistedInCart_NotComboId() {
        try {
            param.setComboId(comboId);
            param.setCompanyId(1l);
            param.setProductId(entry1ProductId);
            param.setOrder(cart);
            param.setEntryId(0l);
            entry1.setQuantity(2l);
            entryDynamicCombo.setProductId(comboId);
            entryDynamicCombo.setQuantity(4l);
            when(productIsComboDataMock.isCombo()).thenReturn(false);
            when(productService.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(productIsComboDataMock);

            strategy.changeOrderEntryToComboEntry(param);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.PRODUCT_IS_NOT_COMBO.code(), e.getCode());
        }
    }

    @Test
    public void changeOrderEntryToComboEntry_AddTheSameDynamicComboExistedInCart_ShouldAddNewCombo() {
        param.setComboId(comboId);
        param.setCompanyId(1l);
        param.setProductId(entry1ProductId);
        param.setOrder(cart);
        param.setEntryId(0l);
        entry1.setQuantity(2l);
        entry1.setEntryNumber(0);
        entry1.setId(0l);
        entryDynamicCombo.setProductId(comboId);
        entryDynamicCombo.setQuantity(4l);
        entryDynamicCombo.setEntryNumber(1);
        entryDynamicCombo.setId(1l);
        when(productIsComboDataMock.isCombo()).thenReturn(true);
        when(productIsComboDataMock.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());
        when(productIsComboDataMock.getPrice()).thenReturn(22000d);
        when(productIsComboDataMock.getId()).thenReturn(comboId);
        when(productService.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(productIsComboDataMock);
        CartEntryModel newEntry = new CartEntryModel();
        newEntry.setEntryNumber(1);
        when(cartService.addNewEntry(cart, comboId, 2, false)).thenReturn(newEntry);
        cart.getEntries().add(newEntry);

        strategy.changeOrderEntryToComboEntry(param);
        assertEquals(3, cart.getEntries().size());
        AbstractOrderEntryModel newDynamicComboEntry = cart.getEntries().get(2);
        assertEquals(2, newDynamicComboEntry.getQuantity(), 0);
        assertEquals(4, entryDynamicCombo.getQuantity(), 0);
        assertEquals(entry1.getProductId(), newDynamicComboEntry.getSubOrderEntries().iterator().next().getProductId());
        assertEquals(entry1.getQuantity(), newDynamicComboEntry.getSubOrderEntries().iterator().next().getQuantity(), 0);
        verify(modelService).save(cart);
        verify(commerceCartCalculationStrategy).recalculateCart(param);

    }

    @Test
    public void changeOrderEntryToComboEntry_NewFixed_ShouldAddNewCombo() {
        param.setComboId(comboId);
        param.setCompanyId(1l);
        param.setProductId(entry1ProductId);
        param.setOrder(cart);
        param.setEntryId(0l);
        entry1.setQuantity(2l);
        entry1.setEntryNumber(0);
        entry1.setId(0l);
        entryDynamicCombo.setProductId(comboId);
        entryDynamicCombo.setQuantity(4l);
        entryDynamicCombo.setEntryNumber(1);
        entryDynamicCombo.setId(1l);
        when(productIsComboDataMock.isCombo()).thenReturn(true);
        when(productIsComboDataMock.getComboType()).thenReturn(ComboType.FIXED_COMBO.toString());
        when(productIsComboDataMock.getPrice()).thenReturn(22000d);
        when(productIsComboDataMock.getId()).thenReturn(comboId);
        BasicProductData basicProductData = new BasicProductData();
        basicProductData.setId(2l);
        when(productIsComboDataMock.getComboProducts()).thenReturn(Arrays.asList(basicProductData));
        when(productService.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(productIsComboDataMock);
        CartEntryModel newEntry = new CartEntryModel();
        newEntry.setEntryNumber(1);
        when(cartService.addNewEntry(cart, comboId, 2, false)).thenReturn(newEntry);
        cart.getEntries().add(newEntry);
        cart.setPriceType(PriceType.RETAIL_PRICE.toString());

        strategy.changeOrderEntryToComboEntry(param);
        assertEquals(3, cart.getEntries().size());
        AbstractOrderEntryModel newFixedComboEntry = cart.getEntries().get(2);
        assertEquals(2, newFixedComboEntry.getQuantity(), 0);
        assertEquals(4, entryDynamicCombo.getQuantity(), 0);
        verify(modelService).save(cart);
        verify(cartService).addSubOrderEntriesToComboEntry(newFixedComboEntry, Arrays.asList(basicProductData), 2);
        verify(commerceCartCalculationStrategy).recalculateCart(param);

    }
    @Test
    public void changeOrderEntryToComboEntry_WholesalePriceType_ComboHasNotWholesalePrice_ThrowException() {
        param.setComboId(comboId);
        param.setCompanyId(1l);
        param.setProductId(entry1ProductId);
        param.setOrder(cart);
        param.setEntryId(0l);
        entry1.setQuantity(2l);
        entry1.setEntryNumber(0);
        entryDynamicCombo.setProductId(comboId);
        entryDynamicCombo.setQuantity(4l);
        entryDynamicCombo.setEntryNumber(1);
        entryDynamicCombo.setId(1l);
        when(productIsComboDataMock.isCombo()).thenReturn(true);
        when(productIsComboDataMock.getComboType()).thenReturn(ComboType.MULTI_GROUP.toString());
        when(productIsComboDataMock.getPrice()).thenReturn(22000d);
        when(productIsComboDataMock.getWholesalePrice()).thenReturn(null);
        when(productIsComboDataMock.getId()).thenReturn(comboId);
        BasicProductData basicProductData = new BasicProductData();
        basicProductData.setId(2l);
        when(productIsComboDataMock.getComboProducts()).thenReturn(Arrays.asList(basicProductData));
        when(productService.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(productIsComboDataMock);
        CartEntryModel newEntry = new CartEntryModel();
        newEntry.setEntryNumber(1);
        when(cartService.addNewEntry(cart, comboId, 2, false)).thenReturn(newEntry);
        cart.getEntries().add(newEntry);
        cart.setPriceType(PriceType.WHOLESALE_PRICE.toString());
        try {
            strategy.changeOrderEntryToComboEntry(param);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.PRODUCT_HAS_NOT_WHOLESALE_PRICE.message(), e.getMessage());
        }
    }

    @Test
    public void changeOrderEntryToComboEntry_WholesalePriceType_ComboHasWholesalePrice_NewFixed_ShouldAddNewCombo() {
        param.setComboId(comboId);
        param.setCompanyId(1l);
        param.setProductId(entry1ProductId);
        param.setOrder(cart);
        param.setEntryId(0l);
        entry1.setQuantity(2l);
        entry1.setEntryNumber(0);
        entry1.setId(0l);
        entryDynamicCombo.setProductId(comboId);
        entryDynamicCombo.setQuantity(4l);
        entryDynamicCombo.setEntryNumber(1);
        when(productIsComboDataMock.isCombo()).thenReturn(true);
        when(productIsComboDataMock.getComboType()).thenReturn(ComboType.FIXED_COMBO.toString());
        when(productIsComboDataMock.getPrice()).thenReturn(22000d);
        when(productIsComboDataMock.getWholesalePrice()).thenReturn(20000d);
        when(productIsComboDataMock.getId()).thenReturn(comboId);
        BasicProductData basicProductData = new BasicProductData();
        basicProductData.setId(2l);
        when(productIsComboDataMock.getComboProducts()).thenReturn(Arrays.asList(basicProductData));
        when(productService.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(productIsComboDataMock);
        CartEntryModel newEntry = new CartEntryModel();
        newEntry.setEntryNumber(1);
        when(cartService.addNewEntry(cart, comboId, 2, false)).thenReturn(newEntry);
        cart.getEntries().add(newEntry);
        cart.setPriceType(PriceType.WHOLESALE_PRICE.toString());

        strategy.changeOrderEntryToComboEntry(param);
        assertEquals(3, cart.getEntries().size());
        AbstractOrderEntryModel newFixedComboEntry = cart.getEntries().get(2);
        assertEquals(2, newFixedComboEntry.getQuantity(), 0);
        assertEquals(4, entryDynamicCombo.getQuantity(), 0);
        verify(cartService).addSubOrderEntriesToComboEntry(newFixedComboEntry, Arrays.asList(basicProductData), 2);
        verify(modelService).save(cart);
        verify(commerceCartCalculationStrategy).recalculateCart(param);
    }

    @Test
    public void changeOrderEntryToComboEntry_PreOrder_HasComboFixedAndHasNotHolding_normalEntry_HasHolding() {
        param.setComboId(comboId);
        param.setCompanyId(1l);
        param.setProductId(entry1ProductId);
        OrderEntryModel normalEntry = new OrderEntryModel();
        normalEntry.setEntryNumber(entryNumber);
        normalEntry.setId(Long.valueOf(entryNumber));
        normalEntry.setQuantity(2l);
        normalEntry.setProductId(entry1ProductId);
        normalEntry.setHolding(true);

        AbstractOrderModel orderModel = new OrderModel();
        orderModel.setCompanyId(2l);
        orderModel.setWarehouseId(4l);
        OrderEntryModel entryModel = new OrderEntryModel();
        orderModel.getEntries().add(entryModel);
        orderModel.getEntries().add(normalEntry);
        orderModel.setOrderStatus(OrderStatus.PRE_ORDER.code());
        orderModel.setType(OrderType.ONLINE.toString());
        param.setOrder(orderModel);
        param.setEntryId(0l);

        subEntry1.setQuantity(1);
        subEntry1.setProductId(2l);
        subEntry2.setQuantity(1);
        subEntry2.setProductId(entry1ProductId);
        entryModel.setProductId(comboId);
        entryModel.setQuantity(1l);
        entryModel.setEntryNumber(1);
        entryModel.setId(1l);
        entryModel.setComboType(ComboType.FIXED_COMBO.name());
        entryModel.setSubOrderEntries(new LinkedHashSet<>(Arrays.asList(subEntry1, subEntry2)));

        when(productIsComboDataMock.isCombo()).thenReturn(true);
        when(productIsComboDataMock.getComboType()).thenReturn(ComboType.FIXED_COMBO.toString());
        when(productIsComboDataMock.getPrice()).thenReturn(22000d);
        when(productIsComboDataMock.getId()).thenReturn(comboId);
        BasicProductData basicProductData = new BasicProductData();
        basicProductData.setId(2l);
        when(productIsComboDataMock.getComboProducts()).thenReturn(Arrays.asList(basicProductData));
        when(productService.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(productIsComboDataMock);

        strategy.changeOrderEntryToComboEntry(param);
        assertEquals(1, orderModel.getEntries().size(), 0);
        AbstractOrderEntryModel newFixedComboEntry = orderModel.getEntries().get(0);
        assertEquals(2, newFixedComboEntry.getQuantity(), 0);
        assertEquals(basicProductData.getId(), newFixedComboEntry.getSubOrderEntries().iterator().next().getProductId());

        verify(inventoryService).updateStockHoldingProductOfList(any(), anyList(), anyBoolean());
        verify(inventoryService).resetHoldingStockOf(any(), any());
        verify(modelService).save(orderModel);
        verify(commerceCartCalculationStrategy).recalculateCart(param);
    }

    @Test
    public void changeOrderEntryToComboEntry_PreOrder_HasComboFixedAndHasNotHolding_normalEntry_HasPreOrder() {
        param.setComboId(comboId);
        param.setCompanyId(1l);
        param.setProductId(entry1ProductId);
        OrderEntryModel normalEntry = new OrderEntryModel();
        normalEntry.setEntryNumber(entryNumber);
        normalEntry.setId(Long.valueOf(entryNumber));
        normalEntry.setQuantity(2l);
        normalEntry.setProductId(entry1ProductId);
        normalEntry.setPreOrder(true);

        AbstractOrderModel orderModel = new OrderModel();
        orderModel.setCompanyId(2l);
        orderModel.setWarehouseId(4l);
        OrderEntryModel entryModel = new OrderEntryModel();
        orderModel.getEntries().add(entryModel);
        orderModel.getEntries().add(normalEntry);
        orderModel.setOrderStatus(OrderStatus.PRE_ORDER.code());
        orderModel.setType(OrderType.ONLINE.toString());
        param.setOrder(orderModel);
        param.setEntryId(0l);

        subEntry1.setQuantity(1);
        subEntry1.setProductId(2l);
        subEntry2.setQuantity(1);
        subEntry2.setProductId(entry1ProductId);
        entryModel.setProductId(comboId);
        entryModel.setQuantity(1l);
        entryModel.setEntryNumber(1);
        entryModel.setId(1l);
        entryModel.setComboType(ComboType.FIXED_COMBO.name());
        entryModel.setSubOrderEntries(new LinkedHashSet<>(Arrays.asList(subEntry1, subEntry2)));

        when(productIsComboDataMock.isCombo()).thenReturn(true);
        when(productIsComboDataMock.getComboType()).thenReturn(ComboType.FIXED_COMBO.toString());
        when(productIsComboDataMock.getPrice()).thenReturn(22000d);
        when(productIsComboDataMock.getId()).thenReturn(comboId);
        BasicProductData basicProductData = new BasicProductData();
        basicProductData.setId(2l);
        when(productIsComboDataMock.getComboProducts()).thenReturn(Arrays.asList(basicProductData));
        when(productService.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(productIsComboDataMock);

        strategy.changeOrderEntryToComboEntry(param);
        assertEquals(1, orderModel.getEntries().size(), 0);
        AbstractOrderEntryModel newFixedComboEntry = orderModel.getEntries().get(0);
        assertEquals(2, newFixedComboEntry.getQuantity(), 0);
        assertEquals(basicProductData.getId(), newFixedComboEntry.getSubOrderEntries().iterator().next().getProductId());

        verify(inventoryService, times(2)).updatePreOrderProductOfList(any(), anyList(), anyBoolean());
        verify(modelService).save(orderModel);
        verify(commerceCartCalculationStrategy).recalculateCart(param);
    }

    @Test
    public void changeOrderEntryToComboEntry_PreOrder_HasComboFixedAndHasHolding_normalEntry_HasNotHolding() {
        param.setComboId(comboId);
        param.setCompanyId(1l);
        param.setProductId(entry1ProductId);
        OrderEntryModel normalEntry = new OrderEntryModel();
        normalEntry.setEntryNumber(entryNumber);
        normalEntry.setId(Long.valueOf(entryNumber));
        normalEntry.setQuantity(2l);
        normalEntry.setProductId(entry1ProductId);

        AbstractOrderModel orderModel = new OrderModel();
        orderModel.setCompanyId(2l);
        orderModel.setWarehouseId(4l);
        OrderEntryModel entryModel = new OrderEntryModel();
        orderModel.getEntries().add(entryModel);
        orderModel.getEntries().add(normalEntry);
        orderModel.setOrderStatus(OrderStatus.PRE_ORDER.code());
        orderModel.setType(OrderType.ONLINE.toString());
        param.setOrder(orderModel);
        param.setEntryId(0l);

        subEntry1.setQuantity(1);
        subEntry1.setProductId(2l);
        subEntry2.setQuantity(1);
        subEntry2.setProductId(3l);
        entryModel.setHolding(true);
        entryModel.setProductId(comboId);
        entryModel.setQuantity(1l);
        entryModel.setEntryNumber(1);
        entryModel.setId(1l);
        entryModel.setComboType(ComboType.FIXED_COMBO.name());
        entryModel.setSubOrderEntries(new LinkedHashSet<>(Arrays.asList(subEntry1, subEntry2)));

        when(productIsComboDataMock.isCombo()).thenReturn(true);
        when(productIsComboDataMock.getComboType()).thenReturn(ComboType.FIXED_COMBO.toString());
        when(productIsComboDataMock.getPrice()).thenReturn(22000d);
        when(productIsComboDataMock.getId()).thenReturn(comboId);
        BasicProductData basicProductData = new BasicProductData();
        basicProductData.setId(2l);
        when(productIsComboDataMock.getComboProducts()).thenReturn(Arrays.asList(basicProductData));
        when(productService.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(productIsComboDataMock);

        strategy.changeOrderEntryToComboEntry(param);
        assertEquals(1, orderModel.getEntries().size(), 0);
        AbstractOrderEntryModel newFixedComboEntry = orderModel.getEntries().get(0);
        assertEquals(2, newFixedComboEntry.getQuantity(), 0);
        assertEquals(basicProductData.getId(), newFixedComboEntry.getSubOrderEntries().iterator().next().getProductId());
        verify(modelService).save(orderModel);
        verify(commerceCartCalculationStrategy).recalculateCart(param);
        verify(inventoryService).updateStockHoldingProductOfList(any(), anyList(), anyBoolean());
    }

    @Test
    public void changeOrderEntryToComboEntry_PreOrder_HasComboFixedAndHasPreOrder_normalEntry_HasNotHolding() {
        param.setComboId(comboId);
        param.setCompanyId(1l);
        param.setProductId(entry1ProductId);
        OrderEntryModel normalEntry = new OrderEntryModel();
        normalEntry.setEntryNumber(entryNumber);
        normalEntry.setId(Long.valueOf(entryNumber));
        normalEntry.setQuantity(2l);
        normalEntry.setProductId(entry1ProductId);

        AbstractOrderModel orderModel = new OrderModel();
        orderModel.setCompanyId(2l);
        orderModel.setWarehouseId(4l);
        OrderEntryModel entryModel = new OrderEntryModel();
        orderModel.getEntries().add(entryModel);
        orderModel.getEntries().add(normalEntry);
        orderModel.setOrderStatus(OrderStatus.PRE_ORDER.code());
        orderModel.setType(OrderType.ONLINE.toString());
        param.setOrder(orderModel);
        param.setEntryId(0l);

        subEntry1.setQuantity(1);
        subEntry1.setProductId(2l);
        subEntry2.setQuantity(1);
        subEntry2.setProductId(3l);
        entryModel.setProductId(comboId);
        entryModel.setQuantity(1l);
        entryModel.setEntryNumber(1);
        entryModel.setId(1l);
        entryModel.setPreOrder(true);
        entryModel.setComboType(ComboType.FIXED_COMBO.name());
        entryModel.setSubOrderEntries(new LinkedHashSet<>(Arrays.asList(subEntry1, subEntry2)));

        when(productIsComboDataMock.isCombo()).thenReturn(true);
        when(productIsComboDataMock.getComboType()).thenReturn(ComboType.FIXED_COMBO.toString());
        when(productIsComboDataMock.getPrice()).thenReturn(22000d);
        when(productIsComboDataMock.getId()).thenReturn(comboId);
        BasicProductData basicProductData = new BasicProductData();
        basicProductData.setId(2l);
        when(productIsComboDataMock.getComboProducts()).thenReturn(Arrays.asList(basicProductData));
        when(productService.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(productIsComboDataMock);

        strategy.changeOrderEntryToComboEntry(param);
        assertEquals(1, orderModel.getEntries().size(), 0);
        AbstractOrderEntryModel newFixedComboEntry = orderModel.getEntries().get(0);
        assertEquals(2, newFixedComboEntry.getQuantity(), 0);
        assertEquals(basicProductData.getId(), newFixedComboEntry.getSubOrderEntries().iterator().next().getProductId());
        verify(modelService).save(orderModel);
        verify(commerceCartCalculationStrategy).recalculateCart(param);
        verify(inventoryService).updatePreOrderProductOfList(any(), anyList(), anyBoolean());
    }

    @Test
    public void changeOrderEntryToComboEntry_PreOrder_HasComboFixedAndHasPreOrder_normalEntry_HasHolding() {
        param.setComboId(comboId);
        param.setCompanyId(1l);
        param.setProductId(entry1ProductId);
        OrderEntryModel normalEntry = new OrderEntryModel();
        normalEntry.setEntryNumber(entryNumber);
        normalEntry.setId(Long.valueOf(entryNumber));
        normalEntry.setQuantity(2l);
        normalEntry.setHolding(true);
        normalEntry.setProductId(entry1ProductId);

        AbstractOrderModel orderModel = new OrderModel();
        orderModel.setCompanyId(2l);
        orderModel.setWarehouseId(4l);
        OrderEntryModel entryModel = new OrderEntryModel();
        orderModel.getEntries().add(entryModel);
        orderModel.getEntries().add(normalEntry);
        orderModel.setOrderStatus(OrderStatus.PRE_ORDER.code());
        orderModel.setType(OrderType.ONLINE.toString());
        param.setOrder(orderModel);
        param.setEntryId(0l);

        subEntry1.setQuantity(1);
        subEntry1.setProductId(2l);
        subEntry2.setQuantity(1);
        subEntry2.setProductId(3l);
        entryModel.setProductId(comboId);
        entryModel.setQuantity(1l);
        entryModel.setEntryNumber(1);
        entryModel.setId(1l);
        entryModel.setPreOrder(true);
        entryModel.setComboType(ComboType.FIXED_COMBO.name());
        entryModel.setSubOrderEntries(new LinkedHashSet<>(Arrays.asList(subEntry1, subEntry2)));

        when(productIsComboDataMock.isCombo()).thenReturn(true);
        when(productIsComboDataMock.getComboType()).thenReturn(ComboType.FIXED_COMBO.toString());
        when(productIsComboDataMock.getPrice()).thenReturn(22000d);
        when(productIsComboDataMock.getId()).thenReturn(comboId);
        BasicProductData basicProductData = new BasicProductData();
        basicProductData.setId(2l);
        when(productIsComboDataMock.getComboProducts()).thenReturn(Arrays.asList(basicProductData));
        when(productService.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(productIsComboDataMock);

        strategy.changeOrderEntryToComboEntry(param);
        assertEquals(1, orderModel.getEntries().size(), 0);
        AbstractOrderEntryModel newFixedComboEntry = orderModel.getEntries().get(0);
        assertEquals(2, newFixedComboEntry.getQuantity(), 0);
        assertEquals(basicProductData.getId(), newFixedComboEntry.getSubOrderEntries().iterator().next().getProductId());
        verify(modelService).save(orderModel);
        verify(commerceCartCalculationStrategy).recalculateCart(param);
        verify(inventoryService).updateStockHoldingProductOfList(any(), anyList(), anyBoolean());
        verify(inventoryService).updatePreOrderProductOfList(any(), anyList(), anyBoolean());
        verify(inventoryService).resetHoldingStockOf(any(), any());
    }

    @Test
    public void changeOrderEntryToComboEntry_PreOrder_HasComboFixedAndHasHolding_normalEntry_HasPreOrder() {
        param.setComboId(comboId);
        param.setCompanyId(1l);
        param.setProductId(entry1ProductId);
        OrderEntryModel normalEntry = new OrderEntryModel();
        normalEntry.setEntryNumber(entryNumber);
        normalEntry.setId(Long.valueOf(entryNumber));
        normalEntry.setQuantity(2l);
        normalEntry.setPreOrder(true);
        normalEntry.setProductId(entry1ProductId);

        AbstractOrderModel orderModel = new OrderModel();
        orderModel.setCompanyId(2l);
        orderModel.setWarehouseId(4l);
        OrderEntryModel entryModel = new OrderEntryModel();
        orderModel.getEntries().add(entryModel);
        orderModel.getEntries().add(normalEntry);
        orderModel.setOrderStatus(OrderStatus.PRE_ORDER.code());
        orderModel.setType(OrderType.ONLINE.toString());
        param.setOrder(orderModel);
        param.setEntryId(0l);

        subEntry1.setQuantity(1);
        subEntry1.setProductId(2l);
        subEntry2.setQuantity(1);
        subEntry2.setProductId(3l);
        entryModel.setProductId(comboId);
        entryModel.setQuantity(1l);
        entryModel.setEntryNumber(1);
        entryModel.setId(1l);
        entryModel.setHolding(true);
        entryModel.setComboType(ComboType.FIXED_COMBO.name());
        entryModel.setSubOrderEntries(new LinkedHashSet<>(Arrays.asList(subEntry1, subEntry2)));

        when(productIsComboDataMock.isCombo()).thenReturn(true);
        when(productIsComboDataMock.getComboType()).thenReturn(ComboType.FIXED_COMBO.toString());
        when(productIsComboDataMock.getPrice()).thenReturn(22000d);
        when(productIsComboDataMock.getId()).thenReturn(comboId);
        BasicProductData basicProductData = new BasicProductData();
        basicProductData.setId(2l);
        when(productIsComboDataMock.getComboProducts()).thenReturn(Arrays.asList(basicProductData));
        when(productService.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(productIsComboDataMock);

        strategy.changeOrderEntryToComboEntry(param);
        assertEquals(1, orderModel.getEntries().size(), 0);
        AbstractOrderEntryModel newFixedComboEntry = orderModel.getEntries().get(0);
        assertEquals(2, newFixedComboEntry.getQuantity(), 0);
        assertEquals(basicProductData.getId(), newFixedComboEntry.getSubOrderEntries().iterator().next().getProductId());
        verify(modelService).save(orderModel);
        verify(commerceCartCalculationStrategy).recalculateCart(param);
        verify(inventoryService).resetHoldingStockOf(any(), any());
        verify(inventoryService, times(2)).updatePreOrderProductOfList(any(), anyList(), anyBoolean());
    }

    @Test
    public void validateValidAddEntryToOnlineOrder_IgnoreWithCardModel() {
        param.setOrder(cart);
        strategy.validateValidAddEntryToOnlineOrder(param);
        assertTrue("success", true);
    }

    @Test
    public void validateValidAddEntryToOnlineOrder_IgnoreWithRetailOrderModel() {
        order.setType(OrderType.RETAIL.toString());
        param.setOrder(order);
        strategy.validateValidAddEntryToOnlineOrder(param);
        assertTrue("success", true);
    }

    @Test
    public void validateValidAddEntryToOnlineOrder_IgnoreWithWholesaleOrderModel() {
        order.setType(OrderType.WHOLESALE.toString());
        param.setOrder(order);
        strategy.validateValidAddEntryToOnlineOrder(param);
        assertTrue("success", true);
    }

    @Test
    public void validateValidAddEntryToOnlineOrder_IgnoreWithNewOnlineOrderModel() {
        order.setType(OrderType.ONLINE.toString());
        order.setOrderStatus(OrderStatus.NEW.code());
        param.setOrder(order);
        strategy.validateValidAddEntryToOnlineOrder(param);
        assertTrue("success", true);
    }

    @Test
    public void validateValidAddEntryToOnlineOrder_IgnoreWithPreOnlineOrderModel_ProductNotFnBAndCombo() {
        order.setType(OrderType.ONLINE.toString());
        order.setOrderStatus(OrderStatus.PRE_ORDER.code());
        when(productService.isFnB(anyLong())).thenReturn(false);
        when(productService.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(productIsComboDataMock);
        when(productIsComboDataMock.isCombo()).thenReturn(false);
        param.setOrder(order);
        param.setQuantity(1l);
        param.setProductId(11l);
        param.setCompanyId(1l);
        strategy.validateValidAddEntryToOnlineOrder(param);
        assertTrue("success", true);
    }

    @Test
    public void validateValidAddEntryToOnlineOrder_PreOnlineOrderModel_NotAcceptFnB() {
        order.setType(OrderType.ONLINE.toString());
        order.setOrderStatus(OrderStatus.PRE_ORDER.code());
        when(productService.isFnB(anyLong())).thenReturn(true);
        param.setOrder(order);
        param.setQuantity(1l);
        param.setProductId(11l);
        param.setCompanyId(1l);
        try {
            strategy.validateValidAddEntryToOnlineOrder(param);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CANNOT_ADD_FNB_TO_PREORDER.message(), e.getMessage());
        }
    }

//    @Test
//    public void validateValidAddEntryToOnlineOrder_PreOnlineOrderModel_NotAcceptCombo() {
//        order.setType(OrderType.ONLINE.toString());
//        order.setOrderStatus(OrderStatus.PRE_ORDER.code());
//        when(productService.isFnB(anyLong())).thenReturn(false);
//        when(productService.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(productIsComboDataMock);
//        when(productIsComboDataMock.isCombo()).thenReturn(true);
//        param.setOrder(order);
//        param.setQuantity(1l);
//        param.setProductId(11l);
//        param.setCompanyId(1l);
//        try {
//            strategy.validateValidAddEntryToOnlineOrder(param);
//            fail("Must throw exception");
//        } catch (ServiceException e) {
//            assertEquals(ErrorCodes.CANNOT_ADD_COMBO_TO_PREORDER.message(), e.getMessage());
//        }
//    }


}
