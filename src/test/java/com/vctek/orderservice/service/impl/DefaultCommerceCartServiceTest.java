package com.vctek.orderservice.service.impl;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.*;
import com.vctek.orderservice.dto.request.CustomerRequest;
import com.vctek.orderservice.dto.request.EntryRequest;
import com.vctek.orderservice.dto.request.UpdateCustomerRequest;
import com.vctek.orderservice.dto.request.storefront.StoreFrontSubOrderEntryRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.OrderSettingDiscountFacade;
import com.vctek.orderservice.facade.OrderSettingFacade;
import com.vctek.orderservice.feignclient.dto.CustomerData;
import com.vctek.orderservice.feignclient.dto.DistributorSetingPriceData;
import com.vctek.orderservice.feignclient.dto.PriceProductRequest;
import com.vctek.orderservice.feignclient.dto.ProductSearchRequest;
import com.vctek.orderservice.kafka.producer.UpdateProductInventoryProducer;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.service.*;
import com.vctek.orderservice.service.event.OrderEvent;
import com.vctek.orderservice.strategy.*;
import com.vctek.orderservice.util.CurrencyType;
import com.vctek.orderservice.util.PriceType;
import com.vctek.redis.elastic.ProductSearchData;
import com.vctek.service.UserService;
import com.vctek.util.ComboType;
import com.vctek.util.OrderStatus;
import com.vctek.util.OrderType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DefaultCommerceCartServiceTest {
    private DefaultCommerceCartService service;

    @Mock
    private AddToCartStrategy addToCartStrategy;
    @Mock
    private CommerceCartCalculationStrategy commerceCartCalculationStrategy;
    @Mock
    private CommerceRemoveEntriesStrategy commerceRemoveEntriesStrategy;
    @Mock
    private CommerceUpdateCartEntryStrategy commerceUpdateCartEntryStrategy;
    @Mock
    private CommerceUpdateCartStrategy commerceUpdateCartStrategy;
    @Mock
    private CommercePlaceOrderStrategy commercePlaceOrderStrategy;
    @Mock
    private ModelService modelService;
    @Mock
    private CommerceAbstractOrderParameter abstractOrderParamMock;
    @Mock
    private CommerceCartModification modificationMock;
    @Mock
    private AbstractOrderEntryModel entryMock;
    @Mock
    private OrderModel orderMock;
    @Mock
    private CartModel cartMock;
    @Mock
    private CommerceAbstractOrderEntryParameter entryParamMock;
    @Mock
    private ProductInComboData productInComboMock;
    @Mock
    private ComboData comboMock;
    @Mock
    private BillService billService;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private LoyaltyService loyaltyService;
    private SubOrderEntryModel subEntry1 = new SubOrderEntryModel();
    private SubOrderEntryModel subEntry2 = new SubOrderEntryModel();

    @Mock
    private ToppingItemModel toppingItemModel;

    @Mock
    private ToppingOptionModel toppingOptionModel;

    @Mock
    private ToppingItemService toppingItemService;

    @Mock
    private ToppingOptionService toppingOptionService;

    @Mock
    private ProductService productService;
    @Mock
    private CalculationService calculationService;
    @Mock
    private OrderSettingDiscountFacade orderSettingDiscountFacade;
    @Mock
    private OrderSettingFacade orderSettingFacade;
    @Mock
    private LogisticService logisticService;
    @Mock
    private UpdateProductInventoryProducer updateProductInventoryProducer;
    @Mock
    private CartService cartService;
    @Mock
    private InventoryService inventoryService;

    private AbstractOrderEntryModel abstractOrderEntryModel = new AbstractOrderEntryModel();
    private ToppingOptionParameter param = new ToppingOptionParameter();
    private ToppingItemParameter toppingItemParam = new ToppingItemParameter();
    private com.vctek.redis.PriceData priceData = new com.vctek.redis.PriceData();
    private ArgumentCaptor<Double> priceCaptor = ArgumentCaptor.forClass(Double.class);
    private Map<Long, Double> priceMap;
    private Map<Long, Integer> stockMap;
    @Mock
    private AbstractOrderEntryModel comboEntry1;
    @Mock
    private AbstractOrderEntryModel comboEntry2;
    @Mock
    private AbstractOrderEntryModel normalEntry;
    @Mock
    private StoreFrontSubOrderEntryRequest request;
    @Mock
    private UserService userService;
    @Mock
    private CartEntryModel cartEntryMock;
    private Set<SubOrderEntryModel> subEntries;
    @Mock
    private PriceData priceProduct;
    @Mock
    private ComboData comboData;
    private CommerceCartValidateParam commerceCartValidateParam;
    @Mock
    private OrderSourceService orderSourceService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        priceMap = new HashMap<>();
        stockMap = new HashMap<>();
        subEntries = new HashSet<>();
        service = new DefaultCommerceCartService(addToCartStrategy, commerceCartCalculationStrategy);
        service.setCommercePlaceOrderStrategy(commercePlaceOrderStrategy);
        service.setCommerceRemoveEntriesStrategy(commerceRemoveEntriesStrategy);
        service.setCommerceUpdateCartEntryStrategy(commerceUpdateCartEntryStrategy);
        service.setCommerceUpdateCartStrategy(commerceUpdateCartStrategy);
        service.setModelService(modelService);
        service.setApplicationEventPublisher(applicationEventPublisher);
        service.setBillService(billService);
        service.setToppingItemService(toppingItemService);
        service.setToppingOptionService(toppingOptionService);
        service.setLoyaltyService(loyaltyService);
        service.setProductService(productService);
        service.setCalculationService(calculationService);
        service.setOrderSettingDiscountFacade(orderSettingDiscountFacade);
        service.setOrderSettingFacade(orderSettingFacade);
        service.setLogisticService(logisticService);
        service.setUpdateProductInventoryProducer(updateProductInventoryProducer);
        service.setCartService(cartService);
        service.setInventoryService(inventoryService);
        service.setUserService(userService);
        service.setOrderSourceService(orderSourceService);
        when(inventoryService.getStoreFrontAvailableStockOfProductList(anyLong(), anyList())).thenReturn(stockMap);
        when(modificationMock.getEntry()).thenReturn(entryMock);
        when(abstractOrderParamMock.getOrder()).thenReturn(orderMock);
        when(modelService.save(orderMock)).thenReturn(orderMock);
        when(modelService.save(cartMock)).thenReturn(cartMock);

        when(entryParamMock.getOrderModel()).thenReturn(orderMock);
        when(entryParamMock.getOrderEntryModel()).thenReturn(entryMock);
        when(entryParamMock.getProductInComboData()).thenReturn(productInComboMock);
        when(entryParamMock.getComboData()).thenReturn(comboMock);
        when(priceProduct.getPrice()).thenReturn(20000d);

        subEntry1.setId(1l);
        subEntry1.setProductId(1l);
        subEntry1.setQuantity(1);
        subEntry2.setId(2l);
        subEntry2.setProductId(2l);
        subEntry2.setQuantity(1);
        when(entryMock.getProductId()).thenReturn(1111l);
        when(entryMock.getSubOrderEntries()).thenReturn(new LinkedHashSet<>(Arrays.asList(subEntry1, subEntry2)));

        toppingItemModel.setId(1l);
        toppingItemModel.setBasePrice(20000d);
        toppingOptionModel.setId(1l);
        toppingOptionModel.setToppingItemModels(Collections.singleton(toppingItemModel));


        abstractOrderEntryModel.setId(1l);
        abstractOrderEntryModel.setEntryNumber(1);
        abstractOrderEntryModel.setOrder(orderMock);
        param.setAbstractOrderModel(orderMock);
        param.setAbstractOrderEntryModel(abstractOrderEntryModel);
        param.setId(1l);
        when(toppingOptionService.findByIdAndOrderEntry(1l, abstractOrderEntryModel))
                .thenReturn(toppingOptionModel);
        toppingItemParam.setToppingOptionModel(toppingOptionModel);
        toppingItemParam.setToppingItemId(1l);
        toppingItemParam.setQuantity(1);
        when(toppingItemService.findByIdAndToppingOption(1l, toppingOptionModel))
                .thenReturn(toppingItemModel);
        when(request.getCompanyId()).thenReturn(1l);
        when(request.getProductId()).thenReturn(1212l);
        when(request.getEntryId()).thenReturn(112l);
        when(request.getSubEntryId()).thenReturn(33l);
        when(userService.getCurrentUserId()).thenReturn(null);
        when(cartService.getCartByGuid(any(CartInfoParameter.class))).thenReturn(cartMock);
        when(cartService.findEntryBy(anyLong(), eq(cartMock))).thenReturn(cartEntryMock);
        when(cartEntryMock.getSubOrderEntries()).thenReturn(subEntries);
        when(cartEntryMock.getQuantity()).thenReturn(1l);
        when(productService.getCombo(anyLong(), anyLong())).thenReturn(comboData);
        when(comboData.getTotalItemQuantity()).thenReturn(2);
        commerceCartValidateParam = new CommerceCartValidateParam(cartMock);
        subEntries.add(subEntry1);
        subEntries.add(subEntry2);
    }

    @Test
    public void addToCart() {
        service.addToCart(abstractOrderParamMock);
        verify(addToCartStrategy).addToCart(abstractOrderParamMock);
    }

    @Test
    public void addEntryToOrder_UpdateBillWithRetailOrder() {
        when(orderMock.getType()).thenReturn(OrderType.RETAIL.toString());
        when(addToCartStrategy.addEntryToOrder(abstractOrderParamMock)).thenReturn(modificationMock);
        when(billService.shouldUpdateBillOf(orderMock)).thenReturn(true);
        service.addEntryToOrder(abstractOrderParamMock);
        verify(modelService).save(orderMock);
        verify(commercePlaceOrderStrategy).updateProductInReturnBillWithOrder(orderMock, modificationMock);
        verify(commercePlaceOrderStrategy).updatePriceAndDiscountBillOf(orderMock);
        verify(applicationEventPublisher).publishEvent(any(OrderEvent.class));
    }

    @Test
    public void addEntryToOrder_WithOnlineOrder_Confirmed_NotUpdateBill() {
        when(orderMock.getType()).thenReturn(OrderType.ONLINE.toString());
        when(orderMock.getOrderStatus()).thenReturn(OrderStatus.CONFIRMED.code());
        when(addToCartStrategy.addEntryToOrder(abstractOrderParamMock)).thenReturn(modificationMock);

        when(billService.shouldUpdateBillOf(orderMock)).thenReturn(false);
        service.addEntryToOrder(abstractOrderParamMock);
        verify(modelService).save(orderMock);
        verify(commercePlaceOrderStrategy, times(0)).updateProductInReturnBillWithOrder(orderMock, modificationMock);
        verify(commercePlaceOrderStrategy, times(0)).updatePriceAndDiscountBillOf(orderMock);
        verify(applicationEventPublisher).publishEvent(any(OrderEvent.class));
    }

    @Test
    public void addEntryToOrder_WithOnlineOrder_Shipping_UpdateBill() {
        when(orderMock.getType()).thenReturn(OrderType.ONLINE.toString());
        when(orderMock.getOrderStatus()).thenReturn(OrderStatus.SHIPPING.code());
        when(addToCartStrategy.addEntryToOrder(abstractOrderParamMock)).thenReturn(modificationMock);
        when(billService.shouldUpdateBillOf(orderMock)).thenReturn(true);

        service.addEntryToOrder(abstractOrderParamMock);
        verify(modelService).save(orderMock);
        verify(commercePlaceOrderStrategy, times(1)).updateProductInReturnBillWithOrder(orderMock, modificationMock);
        verify(commercePlaceOrderStrategy, times(1)).updatePriceAndDiscountBillOf(orderMock);
        verify(applicationEventPublisher).publishEvent(any(OrderEvent.class));
    }

    @Test
    public void removeAllEntries() {
        service.removeAllEntries(abstractOrderParamMock);
        verify(commerceRemoveEntriesStrategy).removeAllEntries(abstractOrderParamMock);
        verify(commerceCartCalculationStrategy).recalculateCart(abstractOrderParamMock);
    }

    @Test
    public void updateQuantityForCartEntry() {
        service.updateQuantityForCartEntry(abstractOrderParamMock);
        verify(commerceUpdateCartEntryStrategy).updateQuantityForCartEntry(abstractOrderParamMock);
    }

    @Test
    public void updateDiscountForCartEntry() {
        service.updateDiscountForCartEntry(abstractOrderParamMock);
        verify(commerceUpdateCartEntryStrategy).updateDiscountForCartEntry(abstractOrderParamMock);
    }

    @Test
    public void updateDiscountForCart() {
        service.updateDiscountForCart(abstractOrderParamMock);
        verify(commerceUpdateCartStrategy).updateCartDiscount(abstractOrderParamMock);
    }

    @Test
    public void updateVatForCart() {
        service.updateVatForCart(abstractOrderParamMock);
        verify(commerceUpdateCartStrategy).updateVat(abstractOrderParamMock);
    }

    @Test
    public void updateWeightForOrderEntry() {
        service.updateWeightForOrderEntry(abstractOrderParamMock);
        verify(commerceUpdateCartEntryStrategy).updateWeightForOrderEntry(abstractOrderParamMock);
        verify(applicationEventPublisher).publishEvent(any(OrderEvent.class));
    }

    @Test
    public void updatePriceForCartEntry() {
        service.updatePriceForCartEntry(abstractOrderParamMock);
        verify(commerceUpdateCartEntryStrategy).updatePriceForCartEntry(abstractOrderParamMock);
    }

    @Test
    public void updatePriceForOrderEntry() {
        service.updatePriceForCartEntry(abstractOrderParamMock);
        verify(commerceUpdateCartEntryStrategy).updatePriceForCartEntry(abstractOrderParamMock);
        verify(applicationEventPublisher).publishEvent(any(OrderEvent.class));
    }

    @Test
    public void changeOrderEntryToComboEntry() {
        service.changeOrderEntryToComboEntry(abstractOrderParamMock);
        verify(addToCartStrategy).changeOrderEntryToComboEntry(abstractOrderParamMock);
        verify(applicationEventPublisher).publishEvent(any(OrderEvent.class));
    }

    @Test
    public void updateSubOrderEntry() {
        service.updateSubOrderEntry(entryMock);
        verify(commerceUpdateCartEntryStrategy).updateSubOrderEntry(entryMock);
    }

    @Test
    public void recalculate() {
        service.recalculate(orderMock, true);
        verify(commerceCartCalculationStrategy).recalculateCart(any(CommerceAbstractOrderParameter.class));
    }

    @Test
    public void updateDiscountForOrderEntry() {
        service.updateDiscountForOrderEntry(abstractOrderParamMock);
        verify(commerceUpdateCartEntryStrategy).updateDiscountForCartEntry(abstractOrderParamMock);
        verify(modelService).save(orderMock);
        verify(commercePlaceOrderStrategy).updatePriceAndDiscountBillOf(orderMock);
        verify(applicationEventPublisher).publishEvent(any(OrderEvent.class));
    }

    @Test
    public void updateDiscountForOrder() {
        service.updateDiscountForOrder(abstractOrderParamMock);
        verify(commerceUpdateCartStrategy).updateCartDiscount(abstractOrderParamMock);
        verify(modelService).save(orderMock);
        verify(commercePlaceOrderStrategy).updatePriceAndDiscountBillOf(orderMock);
        verify(applicationEventPublisher).publishEvent(any(OrderEvent.class));
    }


    @Test
    public void updateOrderEntry_IsNotDeletedOrderEntry_ShouldUpdateSubEntryIfNeed() {
        when(orderMock.getType()).thenReturn(OrderType.RETAIL.toString());
        when(commerceUpdateCartEntryStrategy.updateQuantityForCartEntry(abstractOrderParamMock)).thenReturn(modificationMock);
        when(modificationMock.isDeletedEntry()).thenReturn(false);
        when(billService.shouldUpdateBillOf(orderMock)).thenReturn(true);

        service.updateOrderEntry(abstractOrderParamMock);
        verify(modelService).save(orderMock);
        verify(commerceUpdateCartEntryStrategy).updateSubOrderEntry(entryMock);
        verify(commercePlaceOrderStrategy, times(0))
                .deleteProductInReturnBillWithOrder(orderMock, modificationMock);
        verify(commercePlaceOrderStrategy).updateProductInReturnBillWithOrder(orderMock, modificationMock);
        verify(commercePlaceOrderStrategy).updatePriceAndDiscountBillOf(orderMock);
        verify(modificationMock).setOrder(orderMock);
        verify(applicationEventPublisher).publishEvent(any(OrderEvent.class));
    }

    @Test
    public void updateOrderEntry_IsDeletedOrderEntry_NotUpdateSubEntry() {
        when(orderMock.getType()).thenReturn(OrderType.RETAIL.toString());
        when(commerceUpdateCartEntryStrategy.updateQuantityForCartEntry(abstractOrderParamMock)).thenReturn(modificationMock);
        when(modificationMock.isDeletedEntry()).thenReturn(true);

        AbstractOrderEntryModel entryModel = new AbstractOrderEntryModel();
        Set<ToppingOptionModel> toppingOptionModels = new HashSet<>();
        ToppingOptionModel toppingOptionModel = new ToppingOptionModel();
        ToppingItemModel itemModel = new ToppingItemModel();
        itemModel.setProductId(1L);
        toppingOptionModel.setToppingItemModels(Collections.singleton(itemModel));
        toppingOptionModels.add(toppingOptionModel);
        entryModel.setToppingOptionModels(toppingOptionModels);
        when(modificationMock.getEntry()).thenReturn(entryModel);
        when(billService.shouldUpdateBillOf(orderMock)).thenReturn(true);

        service.updateOrderEntry(abstractOrderParamMock);
        verify(modelService).save(orderMock);
        verify(commerceUpdateCartEntryStrategy, times(0)).updateSubOrderEntry(entryMock);
        verify(commercePlaceOrderStrategy, times(1))
                .deleteProductInReturnBillWithOrder(orderMock, modificationMock);
        verify(commercePlaceOrderStrategy, times(0)).updateProductInReturnBillWithOrder(orderMock, modificationMock);
        verify(commercePlaceOrderStrategy).updatePriceAndDiscountBillOf(orderMock);
        verify(modificationMock).setOrder(orderMock);
        verify(commercePlaceOrderStrategy).updateOrDeleteToppingInReturnBillWithOrder(any(OrderModel.class), anyList());
        verify(applicationEventPublisher).publishEvent(any(OrderEvent.class));
    }

    @Test
    public void addProductToCombo_NewSubEntry() {
        Set<SubOrderEntryModel> subEntries = new HashSet<>();
        when(entryMock.getSubOrderEntries()).thenReturn(subEntries);
        when(entryMock.getQuantity()).thenReturn(3l);

        service.addProductToCombo(entryParamMock);

        verify(modelService).save(entryMock);
        assertEquals(1, subEntries.size());
        assertEquals(3, subEntries.iterator().next().getQuantity(), 0);
    }

    @Test
    public void addProductToCombo_ExistedSubEntry_AllowDuplicated() {
        when(entryMock.getQuantity()).thenReturn(3l);
        subEntry1.setQuantity(3);
        subEntry2.setQuantity(3);
        when(productInComboMock.getId()).thenReturn(1l);
        when(productInComboMock.getPrice()).thenReturn(30000d);
        when(productInComboMock.getQuantity()).thenReturn(1);
        when(comboMock.isDuplicateSaleProduct()).thenReturn(true);

        service.addProductToCombo(entryParamMock);
        verify(modelService).save(entryMock);
        assertEquals(6, subEntry1.getQuantity(), 0);
        assertEquals(3, subEntry2.getQuantity(), 0);
    }

    @Test
    public void updateDiscountForToppingItem() {
        ToppingItemParameter toppingItemParameter = new ToppingItemParameter();
        toppingItemParameter.setAbstractOrderEntryModel(entryMock);
        toppingItemParameter.setAbstractOrderModel(orderMock);
        toppingItemParameter.setDiscount(10d);
        toppingItemParameter.setDiscountType(CurrencyType.PERCENT.toString());
        toppingItemParameter.setToppingItemId(1l);
        toppingItemParameter.setToppingOptionModel(toppingOptionModel);
        when(toppingItemService.findByIdAndToppingOption(anyLong(), any())).thenReturn(toppingItemModel);
        service.updateDiscountForToppingItem(toppingItemParameter);
        verify(modelService).save(toppingItemModel);
    }

    @Test
    public void updateDiscountForToppingItem_invalid_topping_item() {
        try {
            ToppingItemParameter toppingItemParameter = new ToppingItemParameter();
            toppingItemParameter.setAbstractOrderEntryModel(entryMock);
            toppingItemParameter.setAbstractOrderModel(orderMock);
            toppingItemParameter.setDiscount(10d);
            toppingItemParameter.setDiscountType(CurrencyType.PERCENT.toString());
            toppingItemParameter.setToppingItemId(1l);
            toppingItemParameter.setToppingOptionModel(toppingOptionModel);
            when(toppingItemService.findByIdAndToppingOption(anyLong(), any())).thenReturn(null);
            service.updateDiscountForToppingItem(toppingItemParameter);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_TOPPING_ITEM_ID.code(), e.getCode());
        }
    }

    @Test
    public void updateListOrderEntry() {
        EntryRequest entryRequest = new EntryRequest();
        entryRequest.setOrderCode("123");
        entryRequest.setCompanyId(1l);
        entryRequest.setEntryIds("123");
        when(orderMock.getType()).thenReturn(OrderType.RETAIL.toString());
        CommerceCartModification modification = new CommerceCartModification();
        modification.setOrder(orderMock);
        when(commerceUpdateCartEntryStrategy.removeListCartEntry(orderMock, entryRequest)).thenReturn(modificationMock);
        service.updateListOrderEntry(orderMock, entryRequest);
        verify(commerceUpdateCartEntryStrategy).removeListCartEntry(any(), any());
    }

    @Test
    public void updateOrderToppingOption() {
        param.setQuantity(1);
        service.updateOrderToppingOption(param);
        verify(modelService).save(abstractOrderEntryModel);
    }

    @Test
    public void updateOrderToppingItem() {
        param.setQuantity(1);
        service.updateToppingItem(toppingItemParam);
        verify(modelService).save(toppingOptionModel);
    }

    @Test
    public void updatePriceForCartEntries_widthRetailPrice() {
        PriceData priceData = new PriceData();
        priceData.setProductId(123l);
        priceData.setPrice(1000d);
        when(entryMock.getProductId()).thenReturn(123l);
        when(orderMock.getEntries()).thenReturn(Arrays.asList(entryMock));
        when(orderMock.getPriceType()).thenReturn(PriceType.RETAIL_PRICE.name());
        when(productService.getListPriceOfProductIds(anyString())).thenReturn(Arrays.asList(priceData));
        service.updatePriceForCartEntries(orderMock);
        verify(modelService, times(2)).save(orderMock);
        verify(applicationEventPublisher).publishEvent(any(OrderEvent.class));
    }

    @Test
    public void updatePriceForCartEntries_productNotWholesalePrice() {
        try {
            ProductSearchData productSearchData = new ProductSearchData();
            productSearchData.setId(123l);
            productSearchData.setWholesalePrice(null);
            when(entryMock.getProductId()).thenReturn(123l);
            when(orderMock.getEntries()).thenReturn(Arrays.asList(entryMock));
            when(orderMock.getPriceType()).thenReturn(PriceType.WHOLESALE_PRICE.name());
            when(productService.search(any(ProductSearchRequest.class))).thenReturn(Arrays.asList(productSearchData));
            service.updatePriceForCartEntries(orderMock);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.PRODUCT_HAS_NOT_WHOLESALE_PRICE.code(), e.getCode());
        }
    }

    @Test
    public void updatePriceForCartEntries_productNotWholesalePrice_ButFreeGift_ShouldGetPrice() {
        ProductSearchData productSearchData = new ProductSearchData();
        productSearchData.setId(123l);
        priceData.setPrice(20000d);
        productSearchData.setPrices(Arrays.asList(priceData));
        productSearchData.setWholesalePrice(null);
        when(entryMock.getProductId()).thenReturn(123l);
        when(entryMock.isGiveAway()).thenReturn(true);
        when(orderMock.getEntries()).thenReturn(Arrays.asList(entryMock));
        when(orderMock.getPriceType()).thenReturn(PriceType.WHOLESALE_PRICE.name());
        when(productService.search(any(ProductSearchRequest.class))).thenReturn(Arrays.asList(productSearchData));
        service.updatePriceForCartEntries(orderMock);
        verify(entryMock).setBasePrice(priceCaptor.capture());
        assertEquals(20000d, priceCaptor.getValue(), 0);
    }

    @Test
    public void updatePriceForCartEntries_widthWholesalePrice() {
        ProductSearchData productSearchData = new ProductSearchData();
        productSearchData.setId(123l);
        productSearchData.setWholesalePrice(1000d);
        when(entryMock.getProductId()).thenReturn(123l);
        when(orderMock.getEntries()).thenReturn(Arrays.asList(entryMock));
        when(orderMock.getPriceType()).thenReturn(PriceType.WHOLESALE_PRICE.name());
        when(productService.search(any(ProductSearchRequest.class))).thenReturn(Arrays.asList(productSearchData));
        service.updatePriceForCartEntries(orderMock);
        verify(modelService, times(2)).save(orderMock);
        verify(applicationEventPublisher).publishEvent(any(OrderEvent.class));
    }

    @Test
    public void updatePriceForCartEntries_widthDistributorPrice() {
        Map<Long, DistributorSetingPriceData> priceDataMap = new HashMap<>();
        DistributorSetingPriceData setingPriceData = new DistributorSetingPriceData();
        priceDataMap.put(123L, setingPriceData);

        PriceData priceData = new PriceData();
        priceData.setProductId(123l);
        priceData.setPrice(10000d);
        when(entryMock.getProductId()).thenReturn(123l);
        when(orderMock.getDistributorId()).thenReturn(2L);
        when(orderMock.getEntries()).thenReturn(Arrays.asList(entryMock));
        when(orderMock.getPriceType()).thenReturn(PriceType.DISTRIBUTOR_PRICE.name());
        when(productService.getListPriceOfProductIds(anyString())).thenReturn(Arrays.asList(priceData));
        when(logisticService.getProductPriceSetting(anyLong(), anyLong(), anyList())).thenReturn(priceDataMap);
        when(logisticService.calculateDistributorSettingPrice(any(), any())).thenReturn(2000d);
        service.updatePriceForCartEntries(orderMock);
        verify(modelService, times(2)).save(orderMock);
        verify(applicationEventPublisher).publishEvent(any(OrderEvent.class));
        verify(logisticService).getProductPriceSetting(anyLong(), anyLong(), anyList());
    }

    @Test
    public void updateShippingFee() {
        service.updateShippingFee(abstractOrderParamMock);
        verify(calculationService).calculateVat(abstractOrderParamMock.getOrder());
    }

    @Test
    public void updateDefaultSettingCustomer() {
        service.updateDefaultSettingCustomer(abstractOrderParamMock);
        verify(modelService).save(abstractOrderParamMock.getOrder());
    }

    @Test
    public void checkDiscountMaximum_discountEntry_notLargeThan_maximumDiscount() {
        Map<Long, OrderSettingDiscountData> map = new HashMap<>();
        OrderSettingDiscountData data = new OrderSettingDiscountData();
        data.setProductId(123l);
        data.setDiscount(50d);
        data.setDiscountType(CurrencyType.PERCENT.toString());
        OrderSettingDiscountData data2 = new OrderSettingDiscountData();
        data2.setProductId(2l);
        data2.setDiscount(10000d);
        data2.setDiscountType(CurrencyType.CASH.toString());
        map.put(data.getProductId(), data);
        map.put(data2.getProductId(), data2);

        when(entryMock.getProductId()).thenReturn(123l);
        when(entryMock.getQuantity()).thenReturn(2l);
        when(entryMock.getOriginBasePrice()).thenReturn(30000d);
        when(entryMock.getTotalPrice()).thenReturn(60000d);
        when(entryMock.getDiscountType()).thenReturn(CurrencyType.CASH.toString());
        when(entryMock.getDiscount()).thenReturn(12000d);
        when(orderMock.getEntries()).thenReturn(Arrays.asList(entryMock));
        when(orderMock.getCompanyId()).thenReturn(1l);
        when(toppingItemService.findAllByOrderId(anyLong())).thenReturn(new HashSet<>());
        when(orderSettingDiscountFacade.getDiscountProduct(anyLong(), anyList())).thenReturn(map);
        when(orderSettingFacade.getOrderMaximumDiscount(anyLong())).thenReturn(new OrderSettingData());
        List<OrderSettingDiscountData> results = service.checkDiscountMaximumOrder(orderMock);
        assertEquals(0, results.size());
        verify(toppingItemService).findAllByOrderId(anyLong());
        verify(orderSettingDiscountFacade).getDiscountProduct(anyLong(), anyList());
    }

    @Test
    public void checkDiscountMaximum_with_discountEntry_largeThan_maximumDiscount() {
        Map<Long, OrderSettingDiscountData> map = new HashMap<>();
        OrderSettingDiscountData data = new OrderSettingDiscountData();
        data.setProductId(123l);
        data.setDiscount(10000d);
        data.setDiscountType(CurrencyType.CASH.toString());
        OrderSettingDiscountData data2 = new OrderSettingDiscountData();
        data2.setProductId(2l);
        data2.setDiscount(10000d);
        data2.setDiscountType(CurrencyType.CASH.toString());
        map.put(data.getProductId(), data);
        map.put(data2.getProductId(), data2);

        when(entryMock.getProductId()).thenReturn(123l);
        when(entryMock.getDiscountType()).thenReturn(CurrencyType.CASH.name());
        when(entryMock.getDiscount()).thenReturn(12000d);
        when(entryMock.getQuantity()).thenReturn(1l);
        when(entryMock.getQuantity()).thenReturn(1l);
        when(orderMock.getEntries()).thenReturn(Arrays.asList(entryMock));
        when(orderMock.getCompanyId()).thenReturn(1l);
        when(toppingItemService.findAllByOrderId(anyLong())).thenReturn(new HashSet<>());
        when(orderSettingDiscountFacade.getDiscountProduct(anyLong(), anyList())).thenReturn(map);
        when(orderSettingFacade.getOrderMaximumDiscount(anyLong())).thenReturn(new OrderSettingData());
        List<OrderSettingDiscountData> results = service.checkDiscountMaximumOrder(orderMock);
        assertEquals(1, results.size());
        verify(toppingItemService).findAllByOrderId(anyLong());
        verify(orderSettingDiscountFacade).getDiscountProduct(anyLong(), anyList());
    }

    @Test
    public void checkDiscountMaximum_with_discountEntryAndToppingItem_largeThan_maximumDiscount() {
        Map<Long, OrderSettingDiscountData> map = new HashMap<>();
        OrderSettingDiscountData data = new OrderSettingDiscountData();
        data.setProductId(123l);
        data.setDiscount(10000d);
        data.setDiscountType(CurrencyType.CASH.name());
        OrderSettingDiscountData data2 = new OrderSettingDiscountData();
        data2.setProductId(2l);
        data2.setDiscount(10000d);
        data2.setDiscountType(CurrencyType.CASH.name());
        OrderSettingDiscountData data3 = new OrderSettingDiscountData();
        data3.setProductId(233l);
        data3.setDiscount(10000d);
        data3.setDiscountType(CurrencyType.CASH.name());
        map.put(data.getProductId(), data);
        map.put(data2.getProductId(), data2);
        map.put(data3.getProductId(), data3);

        when(entryMock.getProductId()).thenReturn(123l);
        when(entryMock.getDiscountType()).thenReturn(CurrencyType.CASH.name());
        when(entryMock.getDiscount()).thenReturn(12000d);
        when(entryMock.getQuantity()).thenReturn(1l);
        when(orderMock.getEntries()).thenReturn(Arrays.asList(entryMock));
        when(orderMock.getCompanyId()).thenReturn(1l);
        when(toppingItemModel.getProductId()).thenReturn(233l);
        when(toppingItemModel.getBasePrice()).thenReturn(50000d);
        when(toppingItemModel.getDiscount()).thenReturn(30d);
        when(toppingItemModel.getQuantity()).thenReturn(2);
        when(toppingOptionModel.getQuantity()).thenReturn(1);
        when(toppingItemModel.getToppingOptionModel()).thenReturn(toppingOptionModel);
        when(toppingItemModel.getDiscountType()).thenReturn(CurrencyType.PERCENT.name());
        when(toppingItemService.findAllByOrderId(anyLong())).thenReturn(new HashSet<>(Arrays.asList(toppingItemModel)));
        when(orderSettingDiscountFacade.getDiscountProduct(anyLong(), anyList())).thenReturn(map);
        when(orderSettingFacade.getOrderMaximumDiscount(anyLong())).thenReturn(new OrderSettingData());
        List<OrderSettingDiscountData> results = service.checkDiscountMaximumOrder(orderMock);
        assertEquals(2, results.size());
        verify(toppingItemService).findAllByOrderId(anyLong());
        verify(orderSettingDiscountFacade).getDiscountProduct(anyLong(), anyList());
    }

    @Test
    public void checkDiscountMaximum_with_discountEntryAndToppingItem_largeThan_maximumDiscount_discountType_PERCENT() {
        Map<Long, OrderSettingDiscountData> map = new HashMap<>();
        OrderSettingDiscountData data = new OrderSettingDiscountData();
        data.setProductId(123l);
        data.setDiscount(10d);
        data.setDiscountType(CurrencyType.PERCENT.name());
        map.put(data.getProductId(), data);

        when(entryMock.getProductId()).thenReturn(123l);
        when(entryMock.getDiscountType()).thenReturn(CurrencyType.PERCENT.name());
        when(entryMock.getDiscount()).thenReturn(20d);
        when(entryMock.getQuantity()).thenReturn(2l);
        when(entryMock.getTotalPrice()).thenReturn(200000d);
        when(entryMock.getOriginBasePrice()).thenReturn(100000d);
        when(orderMock.getEntries()).thenReturn(Arrays.asList(entryMock));
        when(orderMock.getCompanyId()).thenReturn(1l);
        when(orderSettingDiscountFacade.getDiscountProduct(anyLong(), anyList())).thenReturn(map);
        when(orderSettingFacade.getOrderMaximumDiscount(anyLong())).thenReturn(new OrderSettingData());
        List<OrderSettingDiscountData> results = service.checkDiscountMaximumOrder(orderMock);
        assertEquals(1, results.size());
        verify(orderSettingDiscountFacade).getDiscountProduct(anyLong(), anyList());
    }

    @Test
    public void updateRecommendedRetailPriceForCartEntry() {
        when(commerceUpdateCartEntryStrategy.updateRecommendedRetailPriceForCartEntry(any())).thenReturn(true);
        service.updateRecommendedRetailPriceForCartEntry(abstractOrderParamMock);
        verify(commerceUpdateCartEntryStrategy).updateRecommendedRetailPriceForCartEntry(abstractOrderParamMock);
    }

    @Test
    public void markEntrySaleOff_cartModel() {
        CommerceAbstractOrderParameter parameter = new CommerceAbstractOrderParameter();
        parameter.setOrder(cartMock);
        service.markEntrySaleOff(parameter);
        verify(commerceUpdateCartEntryStrategy).markEntrySaleOff(any(CommerceAbstractOrderParameter.class));
        verify(applicationEventPublisher, times(0)).publishEvent(any(OrderEvent.class));
    }

    @Test
    public void markEntrySaleOff_orderModel() {
        CommerceAbstractOrderParameter parameter = new CommerceAbstractOrderParameter();
        parameter.setOrder(orderMock);
        service.markEntrySaleOff(parameter);
        verify(commerceUpdateCartEntryStrategy).markEntrySaleOff(any(CommerceAbstractOrderParameter.class));
        verify(applicationEventPublisher, times(1)).publishEvent(any(OrderEvent.class));
        verify(updateProductInventoryProducer, times(1)).sendUpdateStockEntries(any(OrderModel.class), anyList());
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
        CustomerData customerData = new CustomerData();
        customerData.setLimitedApplyPromotionAndReward(true);
        service.updateCustomer(request, orderMock);
        verify(modelService).save(any(OrderModel.class));
        verify(applicationEventPublisher).publishEvent(any(OrderEvent.class));
        verify(commerceCartCalculationStrategy).recalculateCart(any(CommerceAbstractOrderParameter.class));
    }

    @Test
    public void updateLatestPriceForEntries_EmptyEntries() {
        when(cartMock.getEntries()).thenReturn(new ArrayList<>());
        service.updateLatestPriceForEntries(cartMock);
        verify(productService, times(0)).getPriceOfProductList(any(PriceProductRequest.class));
    }

    @Test
    public void updateLatestPriceForEntries_noChangePriceOfEntries() {
        when(entryMock.getProductId()).thenReturn(1111l);
        when(entryMock.getSubOrderEntries()).thenReturn(new HashSet<>());
        when(cartMock.getEntries()).thenReturn(Arrays.asList(entryMock));
        when(cartService.isComboEntry(entryMock)).thenReturn(false);
        when(entryMock.getBasePrice()).thenReturn(12000d);
        when(productService.getPriceOfProductList(any(PriceProductRequest.class))).thenReturn(priceMap);
        priceMap.put(1111l, 12000d);

        service.updateLatestPriceForEntries(cartMock);
        verify(productService, times(1)).getPriceOfProductList(any(PriceProductRequest.class));
        verify(commerceCartCalculationStrategy, times(0)).recalculateCart(any(CommerceAbstractOrderParameter.class));
    }

    @Test
    public void updateLatestPriceForEntries_HasChangePriceOfEntries() {
        when(entryMock.getProductId()).thenReturn(1111l);
        when(entryMock.getSubOrderEntries()).thenReturn(new HashSet<>());
        when(cartMock.getEntries()).thenReturn(Arrays.asList(entryMock));
        when(cartService.isComboEntry(entryMock)).thenReturn(false);
        when(entryMock.getBasePrice()).thenReturn(12000d);
        when(productService.getPriceOfProductList(any(PriceProductRequest.class))).thenReturn(priceMap);
        priceMap.put(1111l, 12400d);

        service.updateLatestPriceForEntries(cartMock);
        verify(productService, times(1)).getPriceOfProductList(any(PriceProductRequest.class));
        verify(commerceCartCalculationStrategy, times(1)).recalculateCart(any(CommerceAbstractOrderParameter.class));
    }

    @Test
    public void updateLatestPriceForEntries_HasNotChangePriceOfSubEntries() {
        when(entryMock.getProductId()).thenReturn(1111l);
        when(entryMock.getQuantity()).thenReturn(1l);
        when(entryMock.getComboType()).thenReturn(ComboType.FIXED_COMBO.toString());
        when(entryMock.getSubOrderEntries()).thenReturn(new HashSet<>(Arrays.asList(subEntry1)));
        when(cartMock.getEntries()).thenReturn(Arrays.asList(entryMock));
        when(cartService.isComboEntry(entryMock)).thenReturn(true);
        when(entryMock.getBasePrice()).thenReturn(12000d);
        subEntry1.setProductId(12222l);
        subEntry1.setOriginPrice(10000d);
        when(productService.getPriceOfProductList(any(PriceProductRequest.class))).thenReturn(priceMap);
        PriceData priceCombo = new PriceData();
        priceCombo.setPrice(12000d);
        when(productService.getPriceOfProduct(1111l, 1)).thenReturn(priceCombo);
        priceMap.put(1111l, 12000d);
        priceMap.put(12222l, 10000d);

        service.updateLatestPriceForEntries(cartMock);
        verify(productService, times(1)).getPriceOfProductList(any(PriceProductRequest.class));
        verify(commerceCartCalculationStrategy, times(0)).recalculateCart(any(CommerceAbstractOrderParameter.class));
    }

    @Test
    public void updateLatestPriceForEntries_HasChangePriceOfSubEntries() {
        when(entryMock.getProductId()).thenReturn(1111l);
        when(entryMock.getQuantity()).thenReturn(3l);
        when(entryMock.getComboType()).thenReturn(ComboType.FIXED_COMBO.toString());
        when(entryMock.getSubOrderEntries()).thenReturn(new HashSet<>(Arrays.asList(subEntry1)));
        when(cartMock.getEntries()).thenReturn(Arrays.asList(entryMock));
        when(cartService.isComboEntry(entryMock)).thenReturn(true);
        when(entryMock.getBasePrice()).thenReturn(12000d);
        subEntry1.setProductId(12222l);
        subEntry1.setOriginPrice(12000d);
        when(productService.getPriceOfProductList(any(PriceProductRequest.class))).thenReturn(priceMap);
        PriceData priceCombo = new PriceData();
        priceCombo.setPrice(12000d);
        when(productService.getPriceOfProduct(1111l, 3)).thenReturn(priceCombo);
        priceMap.put(1111l, 12000d);
        priceMap.put(12222l, 10000d);

        service.updateLatestPriceForEntries(cartMock);
        verify(productService, times(1)).getPriceOfProductList(any(PriceProductRequest.class));
        verify(commerceCartCalculationStrategy, times(1)).recalculateCart(any(CommerceAbstractOrderParameter.class));
    }

    @Test
    public void validate_emptyEntries() {
        when(cartMock.getEntries()).thenReturn(new ArrayList<>());
        CommerceCartValidateData validateData = service.validate(commerceCartValidateParam);
        assertEquals(0, validateData.getEntryErrors().size());
    }

    @Test
    public void validate_oneEntry_offsite() {
        when(cartMock.getEntries()).thenReturn(Arrays.asList(entryMock));
        when(cartMock.getCompanyId()).thenReturn(1l);
        when(entryMock.getId()).thenReturn(112l);
        when(entryMock.getProductId()).thenReturn(1111l);
        when(entryMock.getQuantity()).thenReturn(3l);
        when(entryMock.getSubOrderEntries()).thenReturn(new HashSet<>());
        when(productService.isOnsite(1111l, 1l)).thenReturn(false);
        stockMap.put(1111l, 2);

        CommerceCartValidateData validateData = service.validate(commerceCartValidateParam);
        Map<Long, CommerceEntryError> entryErrors = validateData.getEntryErrors();
        assertEquals(1, entryErrors.size());
        assertEquals(ErrorCodes.OFF_SITE_PRODUCT.code(), entryErrors.get(112l).getErrorCode());
    }

    @Test
    public void validate_oneEntry_outOfStock() {
        when(cartMock.getEntries()).thenReturn(Arrays.asList(entryMock));
        when(cartMock.getCompanyId()).thenReturn(1l);
        when(entryMock.getId()).thenReturn(112l);
        when(entryMock.getProductId()).thenReturn(1111l);
        when(entryMock.getQuantity()).thenReturn(3l);
        when(entryMock.getSubOrderEntries()).thenReturn(new HashSet<>());
        when(productService.isOnsite(1111l, 1l)).thenReturn(true);
        stockMap.put(1111l, 0);

        CommerceCartValidateData validateData = service.validate(commerceCartValidateParam);
        Map<Long, CommerceEntryError> entryErrors = validateData.getEntryErrors();
        assertEquals(1, entryErrors.size());
        assertEquals(ErrorCodes.PRODUCT_OUT_OF_STOCK.code(), entryErrors.get(112l).getErrorCode());

    }

    @Test
    public void validate_oneEntry_NotEnoughStock() {
        when(cartMock.getEntries()).thenReturn(Arrays.asList(entryMock));
        when(cartMock.getCompanyId()).thenReturn(1l);
        when(entryMock.getId()).thenReturn(112l);
        when(entryMock.getProductId()).thenReturn(1111l);
        when(entryMock.getQuantity()).thenReturn(3l);
        when(entryMock.getSubOrderEntries()).thenReturn(new HashSet<>());
        when(productService.isOnsite(1111l, 1l)).thenReturn(true);
        stockMap.put(1111l, 2);

        CommerceCartValidateData validateData = service.validate(commerceCartValidateParam);
        Map<Long, CommerceEntryError> entryErrors = validateData.getEntryErrors();
        assertEquals(1, entryErrors.size());
        assertEquals(ErrorCodes.NOT_ENOUGH_STOCK.code(), entryErrors.get(112l).getErrorCode());

    }

    @Test
    public void validate_oneComboEntryOffsite_SubEntryValid() {
        when(cartMock.getEntries()).thenReturn(Arrays.asList(entryMock));
        when(cartMock.getCompanyId()).thenReturn(1l);
        when(entryMock.getId()).thenReturn(112l);
        when(entryMock.getProductId()).thenReturn(1111l);
        when(entryMock.getQuantity()).thenReturn(3l);
        when(entryMock.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());
        when(comboData.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());

        subEntry1.setId(1121l);
        subEntry1.setProductId(111121l);
        subEntry1.setQuantity(3);
        subEntry2.setId(1122l);
        subEntry2.setProductId(111122l);
        subEntry2.setQuantity(3);

        when(productService.isOnsite(1111l, 1l)).thenReturn(false);
        when(productService.isOnsite(111121l, 1l)).thenReturn(true);
        when(productService.isOnsite(111122l, 1l)).thenReturn(true);
        stockMap.put(1111l, 3);
        stockMap.put(111121l, 3);
        stockMap.put(111122l, 3);

        CommerceCartValidateData validateData = service.validate(commerceCartValidateParam);
        Map<Long, CommerceEntryError> entryErrors = validateData.getEntryErrors();
        assertEquals(1, entryErrors.size());
        CommerceEntryError commerceEntryError = entryErrors.get(112l);
        assertEquals(ErrorCodes.OFF_SITE_PRODUCT.code(), commerceEntryError.getErrorCode());
        assertEquals(0, commerceEntryError.getSubEntryErrors().size());
    }

    @Test
    public void validate_oneComboEntryValid_SubEntryOffsite_ShouldSuccess() {
        when(cartMock.getEntries()).thenReturn(Arrays.asList(entryMock));
        when(cartMock.getCompanyId()).thenReturn(1l);
        when(entryMock.getId()).thenReturn(112l);
        when(entryMock.getProductId()).thenReturn(1111l);
        when(entryMock.getQuantity()).thenReturn(3l);
        when(entryMock.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());
        when(comboData.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());

        subEntry1.setId(1121l);
        subEntry1.setProductId(111121l);
        subEntry1.setQuantity(3);
        subEntry2.setId(1122l);
        subEntry2.setProductId(111122l);
        subEntry2.setQuantity(3);

        when(productService.isOnsite(1111l, 1l)).thenReturn(true);
        when(productService.isOnsite(111121l, 1l)).thenReturn(true);
        when(productService.isOnsite(111122l, 1l)).thenReturn(false);
        stockMap.put(1111l, 3);
        stockMap.put(111121l, 3);
        stockMap.put(111122l, 3);

        CommerceCartValidateData validateData = service.validate(commerceCartValidateParam);
        Map<Long, CommerceEntryError> entryErrors = validateData.getEntryErrors();
        assertEquals(0, entryErrors.size());
    }

    @Test
    public void validate_oneComboEntryValid_SubEntryNotEnoughStock() {
        when(cartMock.getEntries()).thenReturn(Arrays.asList(entryMock));
        when(cartMock.getCompanyId()).thenReturn(1l);
        when(entryMock.getId()).thenReturn(112l);
        when(entryMock.getProductId()).thenReturn(1111l);
        when(entryMock.getQuantity()).thenReturn(3l);
        when(entryMock.getComboType()).thenReturn(ComboType.FIXED_COMBO.toString());
        when(comboData.getComboType()).thenReturn(ComboType.FIXED_COMBO.toString());
        subEntry1.setId(1121l);
        subEntry1.setProductId(111121l);
        subEntry1.setQuantity(3);
        subEntry2.setId(1122l);
        subEntry2.setProductId(111122l);
        subEntry2.setQuantity(3);

        when(productService.isOnsite(1111l, 1l)).thenReturn(true);
        when(productService.isOnsite(111121l, 1l)).thenReturn(true);
        when(productService.isOnsite(111122l, 1l)).thenReturn(true);
        stockMap.put(1111l, 3);
        stockMap.put(111121l, 1);
        stockMap.put(111122l, 2);

        CommerceCartValidateData validateData = service.validate(commerceCartValidateParam);
        Map<Long, CommerceEntryError> entryErrors = validateData.getEntryErrors();
        assertEquals(1, entryErrors.size());
        CommerceEntryError commerceEntryError = entryErrors.get(112l);
        assertNull(commerceEntryError.getErrorCode());
        assertEquals(2, commerceEntryError.getSubEntryErrors().size());
        assertEquals(ErrorCodes.NOT_ENOUGH_STOCK.code(), commerceEntryError.getSubEntryErrors().get(1121l).getErrorCode());
        assertEquals(ErrorCodes.NOT_ENOUGH_STOCK.code(), commerceEntryError.getSubEntryErrors().get(1122l).getErrorCode());
    }

    @Test
    public void validate_oneComboEntry_NotValidTotalItemINCombo() {
        when(cartMock.getEntries()).thenReturn(Arrays.asList(comboEntry1));
        when(cartMock.getCompanyId()).thenReturn(1l);
        when(comboEntry1.getId()).thenReturn(111l);
        when(comboEntry1.getProductId()).thenReturn(1111l);
        when(comboEntry1.getQuantity()).thenReturn(3l);
        when(comboEntry1.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());
        when(comboData.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());

        subEntry1.setId(1111l);
        subEntry1.setProductId(1113l);
        subEntry1.setQuantity(3);
        when(comboEntry1.getSubOrderEntries()).thenReturn(new HashSet<>(Arrays.asList(subEntry1)));

        when(productService.isOnsite(1111l, 1l)).thenReturn(true);
        when(productService.isOnsite(1112l, 1l)).thenReturn(true);
        when(productService.isOnsite(1113l, 1l)).thenReturn(true);
        stockMap.put(1111l, 3);
        stockMap.put(1112l, 3);
        stockMap.put(1113l, 9);

        CommerceCartValidateData validateData = service.validate(commerceCartValidateParam);
        assertEquals(ErrorCodes.INVALID_TOTAL_ITEM_IN_COMBO.code(), validateData.getEntryErrors().get(111l).getErrorCode());
    }

    @Test
    public void validate_oneComboEntry_DiffComboType() {
        when(cartMock.getEntries()).thenReturn(Arrays.asList(comboEntry1));
        when(cartMock.getCompanyId()).thenReturn(1l);
        when(comboEntry1.getId()).thenReturn(111l);
        when(comboEntry1.getProductId()).thenReturn(1111l);
        when(comboEntry1.getQuantity()).thenReturn(3l);
        when(comboEntry1.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());
        when(comboData.getComboType()).thenReturn(ComboType.MULTI_GROUP.toString());
        when(comboData.getTotalItemQuantity()).thenReturn(1);

        subEntry1.setId(1111l);
        subEntry1.setProductId(1113l);
        subEntry1.setQuantity(3);
        when(comboEntry1.getSubOrderEntries()).thenReturn(new HashSet<>(Arrays.asList(subEntry1)));

        when(productService.isOnsite(1111l, 1l)).thenReturn(true);
        when(productService.isOnsite(1112l, 1l)).thenReturn(true);
        when(productService.isOnsite(1113l, 1l)).thenReturn(true);
        stockMap.put(1111l, 3);
        stockMap.put(1112l, 3);
        stockMap.put(1113l, 9);

        CommerceCartValidateData validateData = service.validate(commerceCartValidateParam);
        assertEquals(ErrorCodes.OFF_SITE_PRODUCT.code(), validateData.getEntryErrors().get(111l).getErrorCode());
    }

    @Test
    public void validate_oneComboEntry_ProductNotInCombo() {
        when(cartMock.getEntries()).thenReturn(Arrays.asList(comboEntry1));
        when(cartMock.getCompanyId()).thenReturn(1l);
        when(comboEntry1.getId()).thenReturn(111l);
        when(comboEntry1.getProductId()).thenReturn(1111l);
        when(comboEntry1.getQuantity()).thenReturn(3l);
        when(comboEntry1.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());
        when(comboData.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());
        when(comboData.getTotalItemQuantity()).thenReturn(1);

        subEntry1.setId(1111l);
        subEntry1.setProductId(3333l);
        subEntry1.setQuantity(3);
        when(comboEntry1.getSubOrderEntries()).thenReturn(new HashSet<>(Arrays.asList(subEntry1)));

        when(productService.isOnsite(1111l, 1l)).thenReturn(true);
        when(productService.isOnsite(3333l, 1l)).thenReturn(true);
        ErrorCodes err = ErrorCodes.PRODUCT_IS_NOT_COMBO;
        doThrow(new ServiceException(err.code(),err.message(), err.httpStatus()))
                .when(productService).validateProductInCombo(anyLong(), anyLong(), anyString());
        stockMap.put(1111l, 9);
        stockMap.put(3333l, 9);

        CommerceCartValidateData validateData = service.validate(commerceCartValidateParam);
        assertEquals(err.code(), validateData.getEntryErrors().get(111l).getSubEntryErrors().get(1111l).getErrorCode());
    }

    @Test
    public void validate_oneComboEntryValid_EnoughStock_withProduct_InMultipleCombo_BuyNormal() {
        when(cartMock.getEntries()).thenReturn(Arrays.asList(comboEntry1, comboEntry2, normalEntry));
        when(cartMock.getCompanyId()).thenReturn(1l);
        when(comboEntry1.getId()).thenReturn(111l);
        when(comboEntry1.getProductId()).thenReturn(1111l);
        when(comboEntry1.getQuantity()).thenReturn(3l);
        when(comboEntry1.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());
        when(comboEntry2.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());
        when(comboData.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());
        subEntry1.setId(1111l);
        subEntry1.setProductId(1113l);
        subEntry1.setQuantity(3);
        when(comboEntry1.getSubOrderEntries()).thenReturn(new HashSet<>(Arrays.asList(subEntry1)));

        when(comboEntry2.getId()).thenReturn(112l);
        when(comboEntry2.getProductId()).thenReturn(1112l);
        when(comboEntry2.getQuantity()).thenReturn(3l);
        subEntry2.setId(1112l);
        subEntry2.setProductId(1113l);
        subEntry2.setQuantity(3);
        when(comboEntry2.getSubOrderEntries()).thenReturn(new HashSet<>(Arrays.asList(subEntry2)));

        when(normalEntry.getId()).thenReturn(113l);
        when(normalEntry.getProductId()).thenReturn(1113l);
        when(normalEntry.getQuantity()).thenReturn(3l);


        when(productService.isOnsite(1111l, 1l)).thenReturn(true);
        when(productService.isOnsite(1112l, 1l)).thenReturn(true);
        when(productService.isOnsite(1113l, 1l)).thenReturn(true);
        stockMap.put(1111l, 3);
        stockMap.put(1112l, 3);
        stockMap.put(1113l, 3);

        CommerceCartValidateData validateData = service.validate(commerceCartValidateParam);
        Map<Long, CommerceEntryError> entryErrors = validateData.getEntryErrors();
        assertEquals(3, entryErrors.size());
        assertEquals(ErrorCodes.NOT_ENOUGH_STOCK.code(), entryErrors.get(111l).getSubEntryErrors().get(1111l).getErrorCode());
        assertEquals(ErrorCodes.NOT_ENOUGH_STOCK.code(), entryErrors.get(112l).getSubEntryErrors().get(1112l).getErrorCode());
        assertEquals(ErrorCodes.NOT_ENOUGH_STOCK.code(), entryErrors.get(113l).getErrorCode());
    }


    @Test
    public void changeProductInCombo_notFoundSubEntry() {
        when(request.getSubEntryId()).thenReturn(3l);
        service.changeProductInCombo(request);
        verify(productService, times(0)).getPriceOfProduct(anyLong(), anyInt());
        verify(productService, times(0)).getCombo(anyLong(), anyLong());
        verify(commerceCartCalculationStrategy, times(0)).recalculateCart(any(CommerceAbstractOrderParameter.class));
        verify(cartService, times(0)).save(cartMock);
    }

    @Test
    public void changeProductInCombo_newProductTheSameSubEntryProduct() {
        when(request.getSubEntryId()).thenReturn(1l);
        when(request.getProductId()).thenReturn(1l);
        service.changeProductInCombo(request);
        verify(productService, times(0)).getPriceOfProduct(anyLong(), anyInt());
        verify(productService, times(0)).getCombo(anyLong(), anyLong());
        verify(commerceCartCalculationStrategy, times(0)).recalculateCart(any(CommerceAbstractOrderParameter.class));
        verify(cartService, times(0)).save(cartMock);
    }

    @Test
    public void changeProductInCombo_newProductHasNotPrice() {
        when(request.getSubEntryId()).thenReturn(1l);
        when(request.getProductId()).thenReturn(3l);
        subEntry1.setQuantity(2);
        when(productService.getPriceOfProduct(anyLong(), anyInt())).thenReturn(new PriceData());
        try {
            service.changeProductInCombo(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_PRODUCT_PRICE.message(), e.getMessage());
        }
    }

    @Test
    public void changeProductInCombo_multiGroupCombo() {
        when(request.getSubEntryId()).thenReturn(1l);
        when(request.getProductId()).thenReturn(3l);
        subEntry1.setQuantity(2);
        when(productService.getPriceOfProduct(anyLong(), anyInt())).thenReturn(priceProduct);
        when(productService.getCombo(anyLong(), anyLong())).thenReturn(comboMock);
        when(comboMock.getComboType()).thenReturn(ComboType.MULTI_GROUP.toString());

        service.changeProductInCombo(request);
        assertEquals(2, subEntries.size());
        assertEquals(3, subEntry1.getProductId(), 0);
        assertEquals(2, subEntry1.getQuantity(), 0);
        assertEquals(20000d, subEntry1.getOriginPrice(), 0);
        verify(productService, times(1)).getPriceOfProduct(anyLong(), anyInt());
        verify(productService, times(1)).getCombo(anyLong(), anyLong());
        verify(commerceCartCalculationStrategy, times(1)).recalculateCart(any(CommerceAbstractOrderParameter.class));
        verify(cartService, times(1)).saveEntry(cartEntryMock);
    }

    @Test
    public void changeProductInCombo_oneGroupCombo_NotDuplicateProduct() {
        when(request.getSubEntryId()).thenReturn(1l);
        when(request.getProductId()).thenReturn(3l);
        subEntry1.setQuantity(2);
        when(productService.getPriceOfProduct(anyLong(), anyInt())).thenReturn(priceProduct);
        when(productService.getCombo(anyLong(), anyLong())).thenReturn(comboMock);
        when(comboMock.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());
        when(comboMock.isDuplicateSaleProduct()).thenReturn(false);

        service.changeProductInCombo(request);
        assertEquals(2, subEntries.size());
        assertEquals(3, subEntry1.getProductId(), 0);
        assertEquals(2, subEntry1.getQuantity(), 0);
        assertEquals(20000d, subEntry1.getOriginPrice(), 0);
        verify(productService, times(1)).getPriceOfProduct(anyLong(), anyInt());
        verify(productService, times(1)).getCombo(anyLong(), anyLong());
        verify(commerceCartCalculationStrategy, times(1)).recalculateCart(any(CommerceAbstractOrderParameter.class));
        verify(cartService, times(1)).saveEntry(cartEntryMock);
    }

    @Test
    public void changeProductInCombo_oneGroupCombo_DuplicateProduct() {
        when(request.getSubEntryId()).thenReturn(1l);
        when(request.getProductId()).thenReturn(2l);
        subEntry1.setQuantity(2);
        subEntry2.setQuantity(1);
        when(productService.getPriceOfProduct(anyLong(), anyInt())).thenReturn(priceProduct);
        when(productService.getCombo(anyLong(), anyLong())).thenReturn(comboMock);
        when(comboMock.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());
        when(comboMock.isDuplicateSaleProduct()).thenReturn(true);

        service.changeProductInCombo(request);
        assertEquals(1, subEntries.size());
        assertEquals(subEntry2, subEntries.iterator().next());
        assertEquals(2l, subEntry2.getProductId(), 0);
        assertEquals(3, subEntry2.getQuantity(), 0);
        assertEquals(20000d, subEntry1.getOriginPrice(), 0);
        verify(productService, times(1)).getPriceOfProduct(anyLong(), anyInt());
        verify(productService, times(1)).getCombo(anyLong(), anyLong());
        verify(commerceCartCalculationStrategy, times(1)).recalculateCart(any(CommerceAbstractOrderParameter.class));
        verify(cartService, times(1)).saveEntry(cartEntryMock);
    }

    @Test
    public void changeOrderSource_setToNull() {
        service.changeOrderSource(cartMock, null);
        verify(cartMock).setOrderSourceModel(null);
        verify(commerceCartCalculationStrategy).recalculateCart(any(CommerceAbstractOrderParameter.class));
    }

    @Test
    public void changeOrderSource_InvalidOrderSourceId() {
        when(cartMock.getCompanyId()).thenReturn(2l);
        when(orderSourceService.findByIdAndCompanyId(1l, 2l)).thenReturn(null);
        try {
            service.changeOrderSource(cartMock, 1l);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_SOURCE_ID.message(), e.getMessage());
        }
    }

    @Test
    public void changeOrderSource_setToValidOrderSource() {
        when(cartMock.getCompanyId()).thenReturn(2l);
        OrderSourceModel model = new OrderSourceModel();
        when(orderSourceService.findByIdAndCompanyId(1l, 2l)).thenReturn(model);
        service.changeOrderSource(cartMock, 1l);
        verify(cartMock).setOrderSourceModel(model);
        verify(commerceCartCalculationStrategy).recalculateCart(any(CommerceAbstractOrderParameter.class));
    }

}
