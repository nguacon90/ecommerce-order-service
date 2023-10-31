package com.vctek.orderservice.strategy.impl;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.ComboData;
import com.vctek.orderservice.dto.CommerceAbstractOrderParameter;
import com.vctek.orderservice.dto.PriceData;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.CompanyClient;
import com.vctek.orderservice.feignclient.dto.DistributorSetingPriceData;
import com.vctek.orderservice.feignclient.dto.ProductStockData;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.repository.CartEntryRepository;
import com.vctek.orderservice.service.*;
import com.vctek.orderservice.strategy.CommerceCartCalculationStrategy;
import com.vctek.orderservice.util.CurrencyType;
import com.vctek.orderservice.util.DiscountType;
import com.vctek.orderservice.util.OrderSettingType;
import com.vctek.orderservice.util.PriceType;
import com.vctek.util.OrderStatus;
import com.vctek.util.SettingPriceType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;


public class DefaultCommerceUpdateCartEntryStrategyTest {
    private DefaultCommerceUpdateCartEntryStrategy commerceUpdateCartEntryStrategy;
    @Mock
    private ModelService modelService;
    @Mock
    private CalculationService calculationService;
    @Mock
    private CommerceCartCalculationStrategy commerceCartCalculationStrategy;
    private List<AbstractOrderEntryModel> entries = new ArrayList<>();
    private CommerceAbstractOrderParameter commerceAbtractOrderParameter;
    private CartModel cartModel;
    private CartEntryModel entryMergeCandidate;
    private CartEntryModel entryMergeTarget = new CartEntryModel();
    private ArgumentCaptor<ItemModel> captor = ArgumentCaptor.forClass(ItemModel.class);
    private ArgumentCaptor<CommerceAbstractOrderParameter> cartParameterArgumentCaptor;

    @Mock
    private CartEntryRepository cartEntryRepository;
    @Mock
    private CompanyClient companyClient;
    @Mock
    private InventoryService inventoryService;
    @Mock
    private ProductService productService;
    @Mock
    private ComboPriceSettingService comboPriceSettingService;

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private OrderModel orderModel;
    private OrderEntryModel orderEntry;
    private OrderEntryModel orderEntryMergeCandidate;
    private ProductStockData availableStockData;
    private SubOrderEntryModel subEntry1;
    private SubOrderEntryModel subEntry2;
    @Mock
    private PriceData priceDataMock;
    @Mock
    private LogisticService logisticService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        cartParameterArgumentCaptor = ArgumentCaptor.forClass(CommerceAbstractOrderParameter.class);
        commerceUpdateCartEntryStrategy = new DefaultCommerceUpdateCartEntryStrategy();
        commerceUpdateCartEntryStrategy.setCartEntryRepository(cartEntryRepository);
        commerceUpdateCartEntryStrategy.setCommerceCartCalculationStrategy(commerceCartCalculationStrategy);
        commerceUpdateCartEntryStrategy.setModelService(modelService);
        commerceUpdateCartEntryStrategy.setCompanyClient(companyClient);
        commerceUpdateCartEntryStrategy.setInventoryService(inventoryService);
        commerceUpdateCartEntryStrategy.setCalculationService(calculationService);
        commerceUpdateCartEntryStrategy.setProductService(productService);
        commerceUpdateCartEntryStrategy.setComboPriceSettingService(comboPriceSettingService);
        commerceUpdateCartEntryStrategy.setLogisticService(logisticService);

        commerceAbtractOrderParameter = new CommerceAbstractOrderParameter();
        cartModel = new CartModel();
        cartModel.setCompanyId(1l);
        commerceAbtractOrderParameter.setOrder(cartModel);
        commerceAbtractOrderParameter.setEntryId(1l);
        commerceAbtractOrderParameter.setWarehouseId(2l);
        commerceAbtractOrderParameter.setWeight(2.1);

        entryMergeCandidate = new CartEntryModel();
        entryMergeCandidate.setId(1l);
        entryMergeCandidate.setEntryNumber(Integer.valueOf(1));
        entryMergeCandidate.setQuantity(Long.valueOf(3));
        entryMergeCandidate.setProductId(11l);

        orderEntryMergeCandidate = new OrderEntryModel();
        orderEntryMergeCandidate.setId(1l);
        orderEntryMergeCandidate.setEntryNumber(Integer.valueOf(1));
        orderEntryMergeCandidate.setQuantity(Long.valueOf(3));


        entryMergeTarget.setId(4l);
        entryMergeTarget.setEntryNumber(Integer.valueOf(4));
        entryMergeTarget.setQuantity(Long.valueOf(2));
        entryMergeTarget.setProductId(22l);
        given(cartEntryRepository.findAllByOrderAndProductId(eq(cartModel), eq(22l)))
                .willReturn(Collections.singletonList(entryMergeTarget));
        entries.add(entryMergeCandidate);
        entries.add(entryMergeTarget);
        cartModel.setEntries(entries);

        availableStockData = new ProductStockData();
        availableStockData.setQuantity(10);

        orderModel = new OrderModel();
        orderModel.setCompanyId(1l);
        orderModel.setWarehouseId(24l);
        orderEntry = new OrderEntryModel();
        orderEntry.setId(0l);
        orderEntry.setQuantity(2l);
        orderEntry.setProductId(233948l);
        orderEntry.setOrder(orderModel);
        orderModel.getEntries().add(orderEntry);

        subEntry1 = new SubOrderEntryModel();
        subEntry2 = new SubOrderEntryModel();
        orderEntry.setSubOrderEntries(new LinkedHashSet<>(Arrays.asList(subEntry1, subEntry2)));

    }

    @Test
    public void updateQuantityForCartEntry_removeCartEntry() {
        commerceAbtractOrderParameter.setQuantity(0);

        commerceUpdateCartEntryStrategy.updateQuantityForCartEntry(commerceAbtractOrderParameter);
        verify(modelService, times(3)).save(captor.capture());
        CartModel cartModel = captor.getAllValues().stream().filter(i -> i instanceof CartModel)
                .map(item -> (CartModel) item).findFirst().get();
        assertEquals(1, cartModel.getEntries().size());
        assertEquals(entryMergeTarget, cartModel.getEntries().get(0));
        verify(commerceCartCalculationStrategy).recalculateCart(cartParameterArgumentCaptor.capture());
        CommerceAbstractOrderParameter cartParameter = cartParameterArgumentCaptor.getValue();
        assertFalse(cartParameter.getOrder().isCalculated());
    }

    @Test
    public void updateQuantityForCartEntry_UpdateCartEntryQuantity() {
        commerceAbtractOrderParameter.setQuantity(1);
        commerceUpdateCartEntryStrategy.updateQuantityForCartEntry(commerceAbtractOrderParameter);
        verify(modelService, times(3)).save(captor.capture());
        List<ItemModel> allValues = captor.getAllValues();
        CartEntryModel cartEntry = allValues.stream().filter(i -> i instanceof CartEntryModel)
                .map(e -> (CartEntryModel) e).findFirst().get();
        assertEquals(1, cartEntry.getQuantity(), 0);
        verify(commerceCartCalculationStrategy).recalculateCart(cartParameterArgumentCaptor.capture());
        CommerceAbstractOrderParameter cartParameter = cartParameterArgumentCaptor.getValue();
        assertFalse(cartParameter.getOrder().isCalculated());
    }

    @Test
    public void updateDiscountForCartEntry_InvalidDiscountType() {
        try {
            commerceAbtractOrderParameter.setDiscountType("aaa");
            commerceUpdateCartEntryStrategy.updateDiscountForCartEntry(commerceAbtractOrderParameter);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_DISCOUNT_TYPE.code(), e.getCode());
        }
    }

    @Test
    public void updateDiscountForCartEntry() {
        commerceAbtractOrderParameter.setDiscountType(CurrencyType.PERCENT.toString());
        commerceUpdateCartEntryStrategy.updateDiscountForCartEntry(commerceAbtractOrderParameter);
        verify(commerceCartCalculationStrategy).recalculateCart(cartParameterArgumentCaptor.capture());
        CommerceAbstractOrderParameter cartParameter = cartParameterArgumentCaptor.getValue();
        assertFalse(cartParameter.getOrder().isCalculated());
    }

    @Test
    public void updatePriceForCartEntry() {
        commerceUpdateCartEntryStrategy.updatePriceForCartEntry(commerceAbtractOrderParameter);
        verify(modelService).save(any(AbstractOrderEntryModel.class));
        verify(commerceCartCalculationStrategy).recalculateCart(cartParameterArgumentCaptor.capture());
        CommerceAbstractOrderParameter cartParameter = cartParameterArgumentCaptor.getValue();
        assertFalse(cartParameter.getOrder().isCalculated());
    }

    @Test
    public void updateDiscountForOrderEntry() {
        Set<SubOrderEntryModel> subOrderEntryModels = new HashSet<>();
        subOrderEntryModels.add(new SubOrderEntryModel());
        orderEntry.setSubOrderEntries(subOrderEntryModels);
        orderEntry.setEntryNumber(1);
        orderEntry.setId(1l);
        orderModel.setBillId(1L);
        cartModel.setEntries(Collections.singletonList(orderEntry));
        commerceAbtractOrderParameter.setDiscountType(CurrencyType.PERCENT.toString());
        orderModel.setBillId(1L);
        commerceUpdateCartEntryStrategy.updateDiscountForCartEntry(commerceAbtractOrderParameter);
        verify(commerceCartCalculationStrategy).recalculateCart(cartParameterArgumentCaptor.capture());
        CommerceAbstractOrderParameter cartParameter = cartParameterArgumentCaptor.getValue();
        assertFalse(cartParameter.getOrder().isCalculated());
    }

    @Test
    public void updatePriceForOrderEntry() {
        Set<SubOrderEntryModel> subOrderEntryModels = new HashSet<>();
        SubOrderEntryModel subOrderEntryModel = new SubOrderEntryModel();
        subOrderEntryModel.setQuantity(1);
        subOrderEntryModel.setOriginPrice(15000d);
        subOrderEntryModels.add(subOrderEntryModel);
        orderEntry.setSubOrderEntries(subOrderEntryModels);
        orderEntry.setEntryNumber(1);
        orderEntry.setId(1l);
        orderModel.setBillId(1L);
        orderEntry.setQuantity(1L);
        orderEntry.setBasePrice(10000d);
        cartModel.setEntries(Collections.singletonList(orderEntry));
        cartModel.setCompanyId(1L);
        commerceAbtractOrderParameter.setOrder(cartModel);
        ComboData comboData = new ComboData();
        comboData.setId(12L);
        comboData.setPrice(15000d);
        comboData.setTotalItemQuantity(1);
        PriceData priceData = new PriceData();
        priceData.setPrice(10000d);
        when(productService.getCombo(anyLong(), anyLong())).thenReturn(comboData);
        OrderSettingModel orderSettingModel = new OrderSettingModel();
        orderSettingModel.setAmount(50d);
        orderSettingModel.setAmountType(OrderSettingType.COMBO_PRICE_SETTING.code());
        orderSettingModel.setType(CurrencyType.PERCENT.name());
        when(comboPriceSettingService.findByTypeAndCompanyId(anyString(), anyLong())).thenReturn(orderSettingModel);
        when(productService.getPriceOfProduct(anyLong(), anyInt())).thenReturn(priceData);
        commerceAbtractOrderParameter.setBasePrice(10000d);
        commerceUpdateCartEntryStrategy.updatePriceForCartEntry(commerceAbtractOrderParameter);
        verify(modelService).save(any(AbstractOrderEntryModel.class));
        verify(commerceCartCalculationStrategy).recalculateCart(cartParameterArgumentCaptor.capture());
        CommerceAbstractOrderParameter cartParameter = cartParameterArgumentCaptor.getValue();
        assertFalse(cartParameter.getOrder().isCalculated());
    }

    @Test
    public void updateWeightForOrderEntry() {
        commerceUpdateCartEntryStrategy.updateWeightForOrderEntry(commerceAbtractOrderParameter);
        verify(modelService).save(any(AbstractOrderEntryModel.class));
    }

    @Test
    public void removeOrderEntryWithPreOrder_resetHolingStock() {
        orderModel.setOrderStatus(OrderStatus.PRE_ORDER.code());
        commerceAbtractOrderParameter.setOrder(orderModel);
        commerceAbtractOrderParameter.setEntryId(0l);
        commerceAbtractOrderParameter.setQuantity(0);
        orderEntry.setHolding(true);
        when(inventoryService.getAvailableStock(anyLong(), anyLong(), anyLong())).thenReturn(availableStockData);

        commerceUpdateCartEntryStrategy.updateQuantityForCartEntry(commerceAbtractOrderParameter);
        verify(modelService, times(2)).save(orderModel);
        verify(inventoryService, times(1)).resetHoldingStockOf(orderModel, orderEntry);
        verify(inventoryService, times(0)).subtractPreOrder(orderModel, orderEntry);
        verify(inventoryService, times(0)).updateHoldingStockOf(orderModel, orderEntry);
        verify(inventoryService, times(0)).updatePreOrderOf(orderModel, orderEntry);
    }

    @Test
    public void removeOrderEntryWithPreOrder_subtractPreOrder() {
        orderModel.setOrderStatus(OrderStatus.PRE_ORDER.code());
        commerceAbtractOrderParameter.setOrder(orderModel);
        commerceAbtractOrderParameter.setEntryId(0l);
        commerceAbtractOrderParameter.setQuantity(0);
        orderEntry.setPreOrder(true);
        when(inventoryService.getAvailableStock(anyLong(), anyLong(), anyLong())).thenReturn(availableStockData);

        commerceUpdateCartEntryStrategy.updateQuantityForCartEntry(commerceAbtractOrderParameter);
        verify(modelService, times(2)).save(orderModel);
        verify(inventoryService, times(0)).resetHoldingStockOf(orderModel, orderEntry);
        verify(inventoryService, times(1)).subtractPreOrder(orderModel, orderEntry);
        verify(inventoryService, times(0)).updateHoldingStockOf(orderModel, orderEntry);
        verify(inventoryService, times(0)).updatePreOrderOf(orderModel, orderEntry);
    }

    @Test
    public void updateOrderEntryWithPreOrder_updateHoldingStock() {
        orderModel.setOrderStatus(OrderStatus.PRE_ORDER.code());
        commerceAbtractOrderParameter.setOrder(orderModel);
        commerceAbtractOrderParameter.setEntryId(0l);
        commerceAbtractOrderParameter.setQuantity(10);
        orderEntry.setHolding(true);
        ComboData comboData = new ComboData();
        comboData.setId(2L);
        comboData.setPrice(15000d);
        orderModel.setPriceType(PriceType.RETAIL_PRICE.toString());
        comboData.setTotalItemQuantity(2);
        when(productService.getCombo(anyLong(), anyLong())).thenReturn(comboData);
        when(priceDataMock.getPrice()).thenReturn(15000d);
        when(productService.getPriceOfProduct(anyLong(), anyInt())).thenReturn(priceDataMock);
        when(inventoryService.getAvailableStock(anyLong(), anyLong(), anyLong())).thenReturn(availableStockData);

        commerceUpdateCartEntryStrategy.updateQuantityForCartEntry(commerceAbtractOrderParameter);
        verify(modelService, times(2)).save(orderModel);
        verify(modelService).save(orderEntry);
        verify(inventoryService, times(0)).resetHoldingStockOf(orderModel, orderEntry);
        verify(inventoryService, times(0)).subtractPreOrder(orderModel, orderEntry);
        verify(inventoryService, times(1)).updateHoldingStockOf(orderModel, orderEntry);
        verify(inventoryService, times(0)).updatePreOrderOf(orderModel, orderEntry);
    }

    @Test
    public void updateOrderEntryWithPreOrder_updatePreOrder() {
        orderModel.setOrderStatus(OrderStatus.PRE_ORDER.code());
        commerceAbtractOrderParameter.setOrder(orderModel);
        commerceAbtractOrderParameter.setEntryId(0l);
        commerceAbtractOrderParameter.setQuantity(10);
        orderEntry.setPreOrder(true);
        orderModel.setPriceType(PriceType.RETAIL_PRICE.toString());
        ComboData comboData = new ComboData();
        comboData.setId(2L);
        comboData.setPrice(15000d);
        orderModel.setPriceType(PriceType.RETAIL_PRICE.toString());
        comboData.setTotalItemQuantity(2);
        when(productService.getCombo(anyLong(), anyLong())).thenReturn(comboData);
        when(priceDataMock.getPrice()).thenReturn(15000d);
        when(productService.getPriceOfProduct(anyLong(), anyInt())).thenReturn(priceDataMock);
        when(inventoryService.getAvailableStock(anyLong(), anyLong(), anyLong())).thenReturn(availableStockData);

        commerceUpdateCartEntryStrategy.updateQuantityForCartEntry(commerceAbtractOrderParameter);
        verify(modelService, times(2)).save(orderModel);
        verify(modelService).save(orderEntry);
        verify(inventoryService, times(0)).resetHoldingStockOf(orderModel, orderEntry);
        verify(inventoryService, times(0)).subtractPreOrder(orderModel, orderEntry);
        verify(inventoryService, times(0)).updateHoldingStockOf(orderModel, orderEntry);
        verify(inventoryService, times(1)).updatePreOrderOf(orderModel, orderEntry);
    }


    @Test
    public void recalculateComboOrderEntry_UpdateQtyEntryFrom2To3() {
        orderEntry.setQuantity(3l);
        subEntry1.setQuantity(2);
        subEntry2.setQuantity(2);
        ComboData comboData = new ComboData();
        comboData.setId(2L);
        comboData.setPrice(15000d);
        orderModel.setPriceType(PriceType.RETAIL_PRICE.toString());
        comboData.setTotalItemQuantity(2);
        when(productService.getCombo(anyLong(), anyLong())).thenReturn(comboData);
        when(priceDataMock.getPrice()).thenReturn(15000d);
        when(productService.getPriceOfProduct(anyLong(), anyInt())).thenReturn(priceDataMock);

        commerceUpdateCartEntryStrategy.recalculateComboOrderEntry(orderModel, orderEntry, 2, 3);
        assertEquals(3, subEntry1.getQuantity(), 0);
        assertEquals(3, subEntry2.getQuantity(), 0);
    }

    @Test
    public void recalculateComboOrderEntry_UpdateQtyEntryFrom2To3_withAllowedDuplicateSubEntries() {
        orderEntry.setQuantity(3l);
        subEntry1.setQuantity(4);
        subEntry2.setQuantity(2);
        ComboData comboData = new ComboData();
        comboData.setId(2L);
        comboData.setPrice(15000d);
        orderModel.setPriceType(PriceType.RETAIL_PRICE.toString());
        comboData.setTotalItemQuantity(2);
        when(productService.getCombo(anyLong(), anyLong())).thenReturn(comboData);
        when(priceDataMock.getPrice()).thenReturn(15000d);
        when(productService.getPriceOfProduct(anyLong(), anyInt())).thenReturn(priceDataMock);

        commerceUpdateCartEntryStrategy.recalculateComboOrderEntry(orderModel, orderEntry, 2, 3);
        assertEquals(6, subEntry1.getQuantity(), 0);
        assertEquals(3, subEntry2.getQuantity(), 0);
    }

    @Test
    public void updateBasePriceForComboIfNeed_ComboOnlyDefaultPrice_NotChangeComboPrice() {
        orderEntry.setOriginBasePrice(15000d);
        orderEntry.setBasePrice(15000d);
        orderEntry.setQuantity(2l);
        when(productService.getPriceOfProduct(anyLong(), eq(2))).thenReturn(priceDataMock);
        when(priceDataMock.getPrice()).thenReturn(15000d);

        commerceUpdateCartEntryStrategy.updateBasePriceForComboIfNeed(orderModel, orderEntry, 2);
        verify(productService).getPriceOfProduct(anyLong(), eq(2));
        assertEquals(15000d, orderEntry.getOriginBasePrice(), 0);
        assertEquals(15000d, orderEntry.getBasePrice(), 0);
    }

    @Test
    public void updateBasePriceForComboIfNeed_NotChangePrice_ChangeComboQty_HaveOtherPriceOfComboWithQty() {
        orderEntry.setOriginBasePrice(15000d);
        orderEntry.setBasePrice(15000d);
        orderEntry.setQuantity(6l);
        when(productService.getPriceOfProduct(anyLong(), eq(6))).thenReturn(priceDataMock);
        when(priceDataMock.getPrice()).thenReturn(17000d);

        commerceUpdateCartEntryStrategy.updateBasePriceForComboIfNeed(orderModel, orderEntry, 6);
        assertEquals(17000d, orderEntry.getOriginBasePrice(), 0);
        assertEquals(17000d, orderEntry.getBasePrice(), 0);
    }

    @Test
    public void updateBasePriceForComboIfNeed_ChangePriceAndChangeComboQty_HaveOtherPriceOfComboWithQty() {
        orderEntry.setOriginBasePrice(15000d);
        orderEntry.setBasePrice(13000d);
        orderEntry.setQuantity(6l);
        orderModel.setPriceType(PriceType.RETAIL_PRICE.toString());
        when(productService.getPriceOfProduct(anyLong(), eq(6))).thenReturn(priceDataMock);
        when(priceDataMock.getPrice()).thenReturn(17000d);

        commerceUpdateCartEntryStrategy.updateBasePriceForComboIfNeed(orderModel, orderEntry, 6);
        assertEquals(17000d, orderEntry.getOriginBasePrice(), 0);
        assertEquals(17000d, orderEntry.getBasePrice(), 0);
    }

    @Test
    public void updateBasePriceForComboIfNeed_ChangePriceAndChangeComboQty_HaveNoOtherPriceOfComboWithQty() {
        orderEntry.setOriginBasePrice(15000d);
        orderEntry.setBasePrice(13000d);
        orderEntry.setQuantity(6l);
        orderModel.setPriceType(PriceType.RETAIL_PRICE.toString());

        when(productService.getPriceOfProduct(anyLong(), eq(6))).thenReturn(priceDataMock);
        when(priceDataMock.getPrice()).thenReturn(15000d);

        commerceUpdateCartEntryStrategy.updateBasePriceForComboIfNeed(orderModel, orderEntry, 6);
        assertEquals(15000d, orderEntry.getOriginBasePrice(), 0);
        assertEquals(13000d, orderEntry.getBasePrice(), 0);
    }

    /*
     * priceType: DISTRIBUTOR_PRICE,
     * entry: Combo
     * rangeQty: Yes
     * settingPrice: No
     * expect: originBasePrice: 16000
     *          basePrice: 16000
     *           recommendedRetailPrice: 16000
     * */
    @Test
    public void updateBasePriceForComboIfNeed_priceTypeDistributor_case_1() {
        orderModel.setDistributorId(2L);
        orderModel.setPriceType(PriceType.DISTRIBUTOR_PRICE.toString());
        orderEntry.setOriginBasePrice(15000d);
        orderEntry.setBasePrice(13000d);
        orderEntry.setQuantity(6l);
        when(priceDataMock.getPrice()).thenReturn(16000d);
        when(productService.getPriceOfProduct(anyLong(), eq(6))).thenReturn(priceDataMock);
        when(logisticService.getProductPriceSetting(anyLong(), anyLong(), anyList())).thenReturn(new HashMap<>());

        commerceUpdateCartEntryStrategy.updateBasePriceForComboIfNeed(orderModel, orderEntry, 6);
        assertEquals(16000d, orderEntry.getOriginBasePrice(), 0);
        assertEquals(16000d, orderEntry.getBasePrice(), 0);
    }

    /*
     * priceType: DISTRIBUTOR_PRICE,
     * entry: Combo
     * rangeQty: Yes
     * settingPrice: Yes
     * settingRecommendedRetailPrice: No
     * expect: originBasePrice: 55000
     *          basePrice: 50000
     *           recommendedRetailPrice: 55000
     * */
    @Test
    public void updateBasePriceForComboIfNeed_priceTypeDistributor_case_2() {
        orderModel.setDistributorId(2L);
        orderModel.setPriceType(PriceType.DISTRIBUTOR_PRICE.toString());
        orderEntry.setOriginBasePrice(60000d);
        orderEntry.setRecommendedRetailPrice(50000d);
        orderEntry.setBasePrice(45000d);
        orderEntry.setQuantity(6l);
        when(priceDataMock.getPrice()).thenReturn(55000d);
        Map<Long, DistributorSetingPriceData> priceDataMap = new HashMap<>();
        priceDataMap.put(233948l, new DistributorSetingPriceData());
        when(productService.getPriceOfProduct(anyLong(), eq(6))).thenReturn(priceDataMock);
        when(logisticService.getProductPriceSetting(anyLong(), anyLong(), anyList())).thenReturn(priceDataMap);
        when(logisticService.calculateDistributorSettingPrice(any(), anyDouble())).thenReturn(50000d);

        commerceUpdateCartEntryStrategy.updateBasePriceForComboIfNeed(orderModel, orderEntry, 6);
        assertEquals(55000d, orderEntry.getOriginBasePrice(), 0);
        assertEquals(50000d, orderEntry.getBasePrice(), 0);
        assertEquals(55000d, orderEntry.getRecommendedRetailPrice(), 0);
    }

    /*
     * priceType: DISTRIBUTOR_PRICE,
     * entry: Combo
     * rangeQty: Yes
     * settingPrice: Yes
     * settingRecommendedRetailPrice: 62000
     * expect: originBasePrice: 60000d
     *          basePrice: 45000d
     *           recommendedRetailPrice: 50000d
     * */
    @Test
    public void updateBasePriceForComboIfNeed_priceTypeDistributor_case_3() {
        orderModel.setDistributorId(2L);
        orderModel.setPriceType(PriceType.DISTRIBUTOR_PRICE.toString());
        orderEntry.setOriginBasePrice(60000d);
        orderEntry.setRecommendedRetailPrice(50000d);
        orderEntry.setBasePrice(45000d);
        orderEntry.setQuantity(6l);
        when(priceDataMock.getPrice()).thenReturn(55000d);
        Map<Long, DistributorSetingPriceData> priceDataMap = new HashMap<>();
        DistributorSetingPriceData setingPriceData = new DistributorSetingPriceData();
        setingPriceData.setRecommendedRetailPrice(62000d);
        priceDataMap.put(233948l, setingPriceData);
        when(productService.getPriceOfProduct(anyLong(), eq(6))).thenReturn(priceDataMock);
        when(logisticService.getProductPriceSetting(anyLong(), anyLong(), anyList())).thenReturn(priceDataMap);

        commerceUpdateCartEntryStrategy.updateBasePriceForComboIfNeed(orderModel, orderEntry, 6);
        assertEquals(60000d, orderEntry.getOriginBasePrice(), 0);
        assertEquals(45000d, orderEntry.getBasePrice(), 0);
        assertEquals(50000d, orderEntry.getRecommendedRetailPrice(), 0);
    }

    /*
     * priceType: DISTRIBUTOR_PRICE,
     * entry: Combo
     * rangeQty: Yes
     * settingPrice: No
     * settingRecommendedRetailPrice: No
     * expect: originBasePrice: 55000d
     *          basePrice: 55000d
     *           recommendedRetailPrice: 55000d
     * */
    @Test
    public void updateBasePriceForComboIfNeed_priceTypeDistributor_case_4() {
        orderModel.setDistributorId(2L);
        orderModel.setPriceType(PriceType.DISTRIBUTOR_PRICE.toString());
        orderEntry.setOriginBasePrice(60000d);
        orderEntry.setRecommendedRetailPrice(50000d);
        orderEntry.setBasePrice(45000d);
        orderEntry.setQuantity(6l);
        when(priceDataMock.getPrice()).thenReturn(55000d);
        Map<Long, DistributorSetingPriceData> priceDataMap = new HashMap<>();
        when(productService.getPriceOfProduct(anyLong(), eq(6))).thenReturn(priceDataMock);
        when(logisticService.getProductPriceSetting(anyLong(), anyLong(), anyList())).thenReturn(priceDataMap);

        commerceUpdateCartEntryStrategy.updateBasePriceForComboIfNeed(orderModel, orderEntry, 6);
        assertEquals(55000d, orderEntry.getOriginBasePrice(), 0);
        assertEquals(55000d, orderEntry.getBasePrice(), 0);
        assertEquals(55000d, orderEntry.getRecommendedRetailPrice(), 0);
    }

    @Test
    public void updateOrderEntry_recalculateDisCountEntryWithCash() {
        orderModel.setOrderStatus(OrderStatus.NEW.code());
        commerceAbtractOrderParameter.setOrder(orderModel);
        commerceAbtractOrderParameter.setEntryId(0l);
        commerceAbtractOrderParameter.setQuantity(10);
        orderEntry.setHolding(true);
        orderEntry.setDiscount(20000d);
        orderEntry.setDiscountType(DiscountType.CASH.toString());
        orderEntry.setQuantity(2l);
        ComboData comboData = new ComboData();
        comboData.setId(2L);
        comboData.setPrice(15000d);
        orderModel.setPriceType(PriceType.RETAIL_PRICE.toString());
        comboData.setTotalItemQuantity(2);
        when(productService.getCombo(anyLong(), anyLong())).thenReturn(comboData);
        when(priceDataMock.getPrice()).thenReturn(15000d);
        when(productService.getPriceOfProduct(anyLong(), anyInt())).thenReturn(priceDataMock);
        when(productService.getComboAvailableStock(anyLong(), anyLong())).thenReturn(10);
        when(inventoryService.getAvailableStock(anyLong(), anyLong(), anyLong())).thenReturn(availableStockData);

        commerceUpdateCartEntryStrategy.updateQuantityForCartEntry(commerceAbtractOrderParameter);
        verify(modelService, times(2)).save(orderModel);
        verify(modelService).save(orderEntry);
        assertEquals(100000d, orderEntry.getDiscount(), 0);
    }

    @Test
    public void updateRecommendedRetailPriceForCartEntry() {
        cartModel.setDistributorId(2L);
        cartModel.setCompanyId(2L);
        cartModel.setPriceType(PriceType.DISTRIBUTOR_PRICE.toString());
        commerceAbtractOrderParameter.setEntryId(4L);
        commerceAbtractOrderParameter.setRecommendedRetailPrice(2000d);
        when(logisticService.getProductPriceSetting(anyLong(), anyLong(), anyList())).thenReturn(new HashMap<>());
        commerceUpdateCartEntryStrategy.updateRecommendedRetailPriceForCartEntry(commerceAbtractOrderParameter);
        verify(logisticService, times(1)).getProductPriceSetting(anyLong(), anyLong(), anyList());
        verify(logisticService, times(0)).calculateDistributorSettingPrice(any(DistributorSetingPriceData.class), anyDouble());
        verify(modelService, times(1)).save(any(AbstractOrderEntryModel.class));
    }

    @Test
    public void updateRecommendedRetailPriceForCartEntry_invalidEntryNumber() {
        try {
            cartModel.setDistributorId(2L);
            cartModel.setCompanyId(2L);
            commerceAbtractOrderParameter.setEntryId(412L);
            commerceAbtractOrderParameter.setRecommendedRetailPrice(2000d);
            commerceUpdateCartEntryStrategy.updateRecommendedRetailPriceForCartEntry(commerceAbtractOrderParameter);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ENTRY_NUMBER.code(), e.getCode());
        }
    }

    @Test
    public void updateRecommendedRetailPriceForCartEntry_updateBasePrice() {
        Map<Long, DistributorSetingPriceData> priceDataMap = new HashMap<>();
        DistributorSetingPriceData priceData = new DistributorSetingPriceData();
        priceData.setType(SettingPriceType.PRICE_NET.toString());
        priceData.setPrice(12000d);
        priceDataMap.put(22l, priceData);
        cartModel.setDistributorId(2L);
        cartModel.setPriceType(PriceType.DISTRIBUTOR_PRICE.toString());
        cartModel.setCompanyId(2L);
        commerceAbtractOrderParameter.setEntryId(4L);
        commerceAbtractOrderParameter.setRecommendedRetailPrice(2000d);
        when(logisticService.getProductPriceSetting(anyLong(), anyLong(), anyList())).thenReturn(priceDataMap);
        when(logisticService.calculateDistributorSettingPrice(any(DistributorSetingPriceData.class), anyDouble())).thenReturn(12000d);
        commerceUpdateCartEntryStrategy.updateRecommendedRetailPriceForCartEntry(commerceAbtractOrderParameter);
        verify(logisticService, times(1)).getProductPriceSetting(anyLong(), anyLong(), anyList());
        verify(logisticService, times(0)).calculateDistributorSettingPrice(any(DistributorSetingPriceData.class), anyDouble());
        verify(modelService, times(1)).save(any(AbstractOrderEntryModel.class));
    }

    @Test
    public void updateRecommendedRetailPriceForCartEntry_updateBasePrice_typeSettingEqualsDiscount() {
        Map<Long, DistributorSetingPriceData> priceDataMap = new HashMap<>();
        DistributorSetingPriceData priceData = new DistributorSetingPriceData();
        priceData.setType(SettingPriceType.PRICE_BY_DISCOUNT.toString());
        priceData.setDiscount(10000d);
        priceData.setDiscountType(DiscountType.CASH.toString());
        priceDataMap.put(22l, priceData);
        cartModel.setDistributorId(2L);
        cartModel.setPriceType(PriceType.DISTRIBUTOR_PRICE.toString());
        cartModel.setCompanyId(2L);
        commerceAbtractOrderParameter.setEntryId(4L);
        commerceAbtractOrderParameter.setRecommendedRetailPrice(2000d);
        when(logisticService.getProductPriceSetting(anyLong(), anyLong(), anyList())).thenReturn(priceDataMap);
        when(logisticService.calculateDistributorSettingPrice(any(DistributorSetingPriceData.class), anyDouble())).thenReturn(12000d);
        commerceUpdateCartEntryStrategy.updateRecommendedRetailPriceForCartEntry(commerceAbtractOrderParameter);
        verify(logisticService, times(1)).getProductPriceSetting(anyLong(), anyLong(), anyList());
        verify(logisticService, times(1)).calculateDistributorSettingPrice(any(DistributorSetingPriceData.class), anyDouble());
        verify(modelService, times(2)).save(any(AbstractOrderEntryModel.class));
    }

    @Test
    public void validatePriceForCartEntry_INVALID_COMBO_PRICE_LESS_THAN() {
        try {
            orderModel.setDistributorId(2L);
            orderModel.setPriceType(PriceType.WHOLESALE_PRICE.toString());
            orderEntry.setOriginBasePrice(60000d);
            orderEntry.setRecommendedRetailPrice(50000d);
            orderEntry.setBasePrice(45000d);
            orderEntry.setQuantity(6l);
            subEntry1.setQuantity(8);
            subEntry1.setOriginPrice(10000d);
            subEntry2.setQuantity(4);
            subEntry2.setOriginPrice(10000d);
            ComboData comboData = new ComboData();
            comboData.setId(2L);
            comboData.setWholesalePrice(60000d);
            comboData.setTotalItemQuantity(2);
            when(productService.getCombo(anyLong(), anyLong())).thenReturn(comboData);
            commerceUpdateCartEntryStrategy.validatePriceForCartEntry(orderEntry, orderModel);
            fail("throw must fail exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_COMBO_PRICE_LESS_THAN.message(), e.getMessage());
        }
    }

    @Test
    public void validatePriceForCartEntry_INVALID_COMBO_PRICE_LARGER_THAN() {
        try {
            orderModel.setDistributorId(2L);
            orderModel.setPriceType(PriceType.WHOLESALE_PRICE.toString());
            orderEntry.setOriginBasePrice(60000d);
            orderEntry.setRecommendedRetailPrice(50000d);
            orderEntry.setBasePrice(45000d);
            orderEntry.setQuantity(6l);
            subEntry1.setQuantity(8);
            subEntry1.setOriginPrice(50000d);
            subEntry2.setQuantity(4);
            subEntry2.setOriginPrice(15000d);
            ComboData comboData = new ComboData();
            comboData.setId(2L);
            comboData.setWholesalePrice(60000d);
            comboData.setTotalItemQuantity(2);
            OrderSettingModel orderSettingModel = new OrderSettingModel();
            orderSettingModel.setAmount(80000d);
            orderSettingModel.setType(CurrencyType.CASH.toString());
            when(productService.getCombo(anyLong(), anyLong())).thenReturn(comboData);
            when(comboPriceSettingService.findByTypeAndCompanyId(anyString(), anyLong())).thenReturn(orderSettingModel);
            commerceUpdateCartEntryStrategy.validatePriceForCartEntry(orderEntry, orderModel);
            fail("throw must fail exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_COMBO_PRICE_LARGER_THAN.message(), e.getMessage());
        }
    }

    @Test
    public void markEntrySaleOff_cartEntry_NotValidEntryUpdate() {
        try {
            commerceAbtractOrderParameter.setSaleOff(true);
            commerceAbtractOrderParameter.setEntryId(1111l);
            commerceUpdateCartEntryStrategy.markEntrySaleOff(commerceAbtractOrderParameter);
            fail("throw must fail exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ENTRY_NUMBER.message(), e.getMessage());
        }
    }

    @Test
    public void markEntrySaleOff_cartEntry_isSaleOff_true() {
        commerceAbtractOrderParameter.setSaleOff(true);

        commerceUpdateCartEntryStrategy.markEntrySaleOff(commerceAbtractOrderParameter);
        assertEquals(true, entryMergeCandidate.isSaleOff());
        verify(modelService).save(entryMergeCandidate);
        verify(commerceCartCalculationStrategy).recalculateCart(any(CommerceAbstractOrderParameter.class));
        verify(modelService).save(cartModel);
    }

    @Test
    public void markEntrySaleOff_orderEntry_isSaleOff_true() {
        commerceAbtractOrderParameter.setSaleOff(true);
        commerceAbtractOrderParameter.setOrder(orderModel);
        orderModel.getEntries().add(orderEntryMergeCandidate);

        commerceUpdateCartEntryStrategy.markEntrySaleOff(commerceAbtractOrderParameter);
        assertEquals(true, orderEntryMergeCandidate.isSaleOff());
        verify(modelService).save(orderEntryMergeCandidate);
        verify(commerceCartCalculationStrategy).recalculateCart(any(CommerceAbstractOrderParameter.class));
        verify(modelService).save(orderModel);
    }

    @Test
    public void markEntrySaleOff_orderEntry_isSaleOff_false() {
        commerceAbtractOrderParameter.setSaleOff(false);
        commerceAbtractOrderParameter.setOrder(orderModel);
        orderEntryMergeCandidate.setDiscount(10000d);
        orderEntryMergeCandidate.setDiscountType(DiscountType.CASH.toString());
        orderModel.getEntries().add(orderEntryMergeCandidate);

        commerceUpdateCartEntryStrategy.markEntrySaleOff(commerceAbtractOrderParameter);
        assertEquals(false, orderEntryMergeCandidate.isSaleOff());
        assertEquals(10000d, orderEntryMergeCandidate.getDiscount(), 0);
        assertEquals(DiscountType.CASH.toString(), orderEntryMergeCandidate.getDiscountType());
        verify(modelService).save(orderEntryMergeCandidate);
        verify(commerceCartCalculationStrategy).recalculateCart(any(CommerceAbstractOrderParameter.class));
        verify(modelService).save(orderModel);
    }

    @Test
    public void markEntrySaleOff_cartEntry_isSaleOff_false_NotBuyNormalEntryWithTheSameProduct() {
        commerceAbtractOrderParameter.setSaleOff(false);
        entryMergeCandidate.setDiscount(10000d);
        entryMergeCandidate.setDiscountType(DiscountType.CASH.toString());
        entryMergeCandidate.setSaleOff(true);

        commerceUpdateCartEntryStrategy.markEntrySaleOff(commerceAbtractOrderParameter);
        assertEquals(false, entryMergeCandidate.isSaleOff());
        assertEquals(null, entryMergeCandidate.getDiscount());
        assertEquals(null, entryMergeCandidate.getDiscountType());
        verify(modelService).save(entryMergeCandidate);
        verify(commerceCartCalculationStrategy).recalculateCart(any(CommerceAbstractOrderParameter.class));
        verify(modelService).save(cartModel);
    }

    @Test
    public void markEntrySaleOff_cartEntry_isSaleOff_false_HasNormalEntryWithTheSameProduct_OverMaxAvailableStock() {
        commerceAbtractOrderParameter.setSaleOff(false);
        entryMergeCandidate.setDiscount(10000d);
        entryMergeCandidate.setDiscountType(DiscountType.CASH.toString());
        entryMergeCandidate.setSaleOff(true);
        entryMergeCandidate.setQuantity(3l);
        CartEntryModel normal = new CartEntryModel();
        normal.setProductId(11l);
        normal.setId(123213l);
        normal.setQuantity(3l);
        normal.setEntryNumber(2);
        cartModel.getEntries().add(normal);

        when(cartEntryRepository.findAllByOrderAndProductId(eq(cartModel), eq(11l)))
                .thenReturn(Collections.singletonList(normal));
        when(companyClient.checkSellLessZero(anyLong())).thenReturn(false);
        availableStockData.setQuantity(3);
        when(inventoryService.getAvailableStock(anyLong(), anyLong(), any())).thenReturn(availableStockData);

        commerceUpdateCartEntryStrategy.markEntrySaleOff(commerceAbtractOrderParameter);
        assertEquals(false, normal.isSaleOff());
        assertEquals(3, normal.getQuantity(), 0);
        assertEquals(2, cartModel.getEntries().size());
        assertEquals(false, cartModel.getEntries().contains(entryMergeCandidate));
        verify(modelService).save(normal);
        verify(commerceCartCalculationStrategy).recalculateCart(any(CommerceAbstractOrderParameter.class));
        verify(modelService).save(cartModel);
    }

    @Test
    public void markEntrySaleOff_cartEntry_isSaleOff_false_HasNormalEntryWithTheSameProduct_EqualsAvailableStock() {
        commerceAbtractOrderParameter.setSaleOff(false);
        entryMergeCandidate.setDiscount(10000d);
        entryMergeCandidate.setDiscountType(DiscountType.CASH.toString());
        entryMergeCandidate.setSaleOff(true);
        entryMergeCandidate.setQuantity(3l);
        CartEntryModel normal = new CartEntryModel();
        normal.setProductId(11l);
        normal.setId(123213l);
        normal.setQuantity(3l);
        normal.setEntryNumber(2);
        cartModel.getEntries().add(normal);

        when(cartEntryRepository.findAllByOrderAndProductId(eq(cartModel), eq(11l)))
                .thenReturn(Collections.singletonList(normal));
        when(companyClient.checkSellLessZero(anyLong())).thenReturn(false);
        availableStockData.setQuantity(5);
        when(inventoryService.getAvailableStock(anyLong(), anyLong(), any())).thenReturn(availableStockData);

        commerceUpdateCartEntryStrategy.markEntrySaleOff(commerceAbtractOrderParameter);
        assertEquals(false, normal.isSaleOff());
        assertEquals(5, normal.getQuantity(), 0);
        assertEquals(2, cartModel.getEntries().size());
        assertEquals(false, cartModel.getEntries().contains(entryMergeCandidate));
        verify(modelService).save(normal);
        verify(commerceCartCalculationStrategy).recalculateCart(any(CommerceAbstractOrderParameter.class));
        verify(modelService).save(cartModel);
    }

    @Test
    public void markEntrySaleOff_cartEntry_isSaleOff_false_HasNormalEntryWithTheSameProduct_AllowSellLessThanZero() {
        commerceAbtractOrderParameter.setSaleOff(false);
        entryMergeCandidate.setDiscount(10000d);
        entryMergeCandidate.setDiscountType(DiscountType.CASH.toString());
        entryMergeCandidate.setSaleOff(true);
        entryMergeCandidate.setQuantity(3l);
        CartEntryModel normal = new CartEntryModel();
        normal.setProductId(11l);
        normal.setId(123213l);
        normal.setQuantity(3l);
        normal.setEntryNumber(2);
        cartModel.getEntries().add(normal);

        when(cartEntryRepository.findAllByOrderAndProductId(eq(cartModel), eq(11l)))
                .thenReturn(Collections.singletonList(normal));
        when(companyClient.checkSellLessZero(anyLong())).thenReturn(true);

        commerceUpdateCartEntryStrategy.markEntrySaleOff(commerceAbtractOrderParameter);
        assertEquals(false, normal.isSaleOff());
        assertEquals(6, normal.getQuantity(), 0);
        assertEquals(2, cartModel.getEntries().size());
        assertEquals(false, cartModel.getEntries().contains(entryMergeCandidate));
        verify(modelService).save(normal);
        verify(commerceCartCalculationStrategy).recalculateCart(any(CommerceAbstractOrderParameter.class));
        verify(modelService).save(cartModel);
    }
}