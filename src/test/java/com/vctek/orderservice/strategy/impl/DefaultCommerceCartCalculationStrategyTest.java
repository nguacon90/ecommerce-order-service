package com.vctek.orderservice.strategy.impl;

import com.vctek.orderservice.dto.CommerceAbstractOrderParameter;
import com.vctek.orderservice.dto.ProductCanRewardDto;
import com.vctek.orderservice.feignclient.dto.CustomerData;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionEngineService;
import com.vctek.orderservice.repository.EntryRepository;
import com.vctek.orderservice.repository.ToppingItemRepository;
import com.vctek.orderservice.service.CalculationService;
import com.vctek.orderservice.service.CustomerService;
import com.vctek.orderservice.service.LoyaltyService;
import com.vctek.orderservice.service.OrderService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DefaultCommerceCartCalculationStrategyTest {
    @Mock
    private OrderService orderService;
    @Mock
    private CalculationService calculationService;
    @Mock
    private LoyaltyService loyaltyService;
    @Mock
    private CustomerService customerService;
    @Mock
    private PromotionEngineService promotionEngineService;
    @Mock
    private EntryRepository entryRepository;
    @Mock
    private ToppingItemRepository toppingItemRepository;
    private DefaultCommerceCartCalculationStrategy strategy;
    private CommerceAbstractOrderParameter param = new CommerceAbstractOrderParameter();
    private CartModel cart = new CartModel();
    private OrderModel order = new OrderModel();
    private List<AbstractOrderEntryModel> entries = new ArrayList<>();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        param.setOrder(cart);
        strategy = new DefaultCommerceCartCalculationStrategy();
        strategy.setCalculationService(calculationService);
        strategy.setPromotionsService(promotionEngineService);
        strategy.setOrderService(orderService);
        strategy.setLoyaltyService(loyaltyService);
        strategy.setCustomerService(customerService);
        strategy.setEntryRepository(entryRepository);
        strategy.setToppingItemRepository(toppingItemRepository);
        order.setEntries(entries);
    }

    @Test
    public void calculateCart_CartAlreadyCalculate_ShouldIgnoreToCalculate() {
        cart.setCalculated(true);
        boolean calculated = strategy.calculateCart(param);
        assertTrue(calculated);
        verify(promotionEngineService, times(0)).updatePromotions(Collections.emptyList(), cart);
        verify(calculationService, times(0)).recalculate(cart);
    }

    @Test
    public void calculateCart() {
        boolean calculated = strategy.calculateCart(param);
        assertTrue(calculated);
        verify(promotionEngineService).updatePromotions(Collections.emptyList(), cart);
        verify(calculationService).calculate(cart);
    }

    @Test
    public void reCalculateCart() {
        boolean calculated = strategy.recalculateCart(param);
        assertTrue(calculated);
        verify(promotionEngineService).updatePromotions(Collections.emptyList(), cart);
        verify(calculationService).recalculate(cart);
    }

    @Test
    public void splitOrderPromotionToEntries_HasNotOrderDiscount() {
        OrderEntryModel entry1 = generateEntry(1l, 60000d);
        OrderEntryModel entry2 = generateEntry(2l, 100000d);
        OrderEntryModel entry3 = generateEntry(3l, 500000d);
        order.setTotalDiscount(0d);
        order.setSubTotal(660000d);
        entries.add(entry1);
        entries.add(entry2);
        entries.add(entry3);
        when(entryRepository.findAllByOrder(any())).thenReturn(entries);
        strategy.splitOrderPromotionToEntries(order);
        assertEquals(0, entry1.getDiscountOrderToItem(), 0);
        assertEquals(0, entry2.getDiscountOrderToItem(), 0);
        assertEquals(0, entry3.getDiscountOrderToItem(), 0);
    }

    /**
     * order with promotion amount: 100.000
     * order with entries:
     * subtotal order = 20.000 * 3 + 100.000 * 1 + 250.000 * 2 = 660.000
     * entry1: price 20.000 , qty 3, orderPromotion = 100000 - 75758 - 15152 = 9090
     * entry2: price 100.000, qty 1, orderPromotion = 100000 / 660000 * 100000 = 15152
     * entry3: price 250.000, qty 2, orderPromotion = 500000 / 660000 * 100000 = 75758
     */
    @Test
    public void splitOrderPromotionToEntries() {
        OrderEntryModel entry1 = generateEntry(1l,60000d);
        OrderEntryModel entry2 = generateEntry(2l,100000d);
        OrderEntryModel entry3 = generateEntry(3l, 500000d);
        OrderEntryModel comboEntry = new OrderEntryModel();
        comboEntry.setProductId(222l);
        comboEntry.setBasePrice(340000d);
        comboEntry.setSubOrderEntries(new LinkedHashSet(Arrays.asList(new SubOrderEntryModel())));

        order.setTotalDiscount(100000d);
        order.setSubTotal(660000d);
        entries.add(entry1);
        entries.add(entry2);
        entries.add(entry3);
        entries.add(comboEntry);
        when(orderService.isValidEntryForPromotion(comboEntry)).thenReturn(false);
        when(orderService.isValidEntryForPromotion(entry1)).thenReturn(true);
        when(orderService.isValidEntryForPromotion(entry2)).thenReturn(true);
        when(orderService.isValidEntryForPromotion(entry3)).thenReturn(true);
        when(entryRepository.findAllByOrder(any())).thenReturn(entries);

        strategy.splitOrderPromotionToEntries(order);
        assertNotNull(entry1.getDiscountOrderToItem());
        assertNotNull(entry2.getDiscountOrderToItem());
        assertNotNull(entry3.getDiscountOrderToItem());
        assertEquals(0, comboEntry.getDiscountOrderToItem(), 0);
        assertEquals(9090, entry1.getDiscountOrderToItem(), 0);
        assertEquals(15152, entry2.getDiscountOrderToItem(), 0);
        assertEquals(75758, entry3.getDiscountOrderToItem(), 0);
    }


    /**
     * order with promotion amount: 10.000
     * order with entries:
     * subtotal order = 200 * 1 + 1.000.000 * 5 + 2.500.000 * 2 = 10.000.200
     * entry1: price 200 , qty 1, orderPromotion = 100000 - 5000 - 5000 = 0
     * entry2: price 1.000.000, qty 5, orderPromotion = 5000000 / 10000200 * 10000 = 5000
     * entry3: price 2.500.000, qty 2, orderPromotion = 5000000 / 10000200 * 10000 = 5000
     */
    @Test
    public void splitOrderPromotionToEntries_Case2() {
        OrderEntryModel entry1 = generateEntry(1l, 200d);
        OrderEntryModel entry2 = generateEntry(2l, 5000000d);
        OrderEntryModel entry3 = generateEntry(3l, 5000000d);
        order.setTotalDiscount(10000d);
        order.setSubTotal(10000200d);
        entries.add(entry1);
        entries.add(entry2);
        entries.add(entry3);
        when(orderService.isValidEntryForPromotion(any())).thenReturn(true);
        when(entryRepository.findAllByOrder(any())).thenReturn(entries);
        strategy.splitOrderPromotionToEntries(order);
        assertNotNull(entry1.getDiscountOrderToItem());
        assertNotNull(entry2.getDiscountOrderToItem());
        assertNotNull(entry3.getDiscountOrderToItem());
        assertEquals(0, entry1.getDiscountOrderToItem(), 0);
        assertEquals(5000, entry2.getDiscountOrderToItem(), 0);
        assertEquals(5000, entry3.getDiscountOrderToItem(), 0);
    }

    /**
     * Order contains combo should ignore combo when splitting promotion amount
     * order with promotion amount: 10.000
     * order with entries:
     * subtotal order = 200.000 * 1 + 1.000.000 * 5 + 2.500.000 * 2 + 100.000 = 10.300.000
     * entry0: comboEntry: price: 100.000, qty: 1
     * entry1: price 200.000 , qty 1, orderPromotion = 10000 - 4902 - 4902 = 196
     * entry2: price 1.000.000, qty 5, orderPromotion = 5000000 / 10200000 * 10000 = 4902
     * entry3: price 2.500.000, qty 2, orderPromotion = 5000000 / 10200000 * 10000 = 4902
     */
    @Test
    public void splitOrderPromotionToEntries_Case3() {
        OrderEntryModel entry0 = generateEntry(11l, 100000d);
        OrderEntryModel entry1 = generateEntry(1l, 200000d);
        OrderEntryModel entry2 = generateEntry(2l, 5000000d);
        OrderEntryModel entry3 = generateEntry(3l, 5000000d);
        when(orderService.isValidEntryForPromotion(entry0)).thenReturn(false);
        when(orderService.isComboEntry(entry0)).thenReturn(true);
        when(orderService.isValidEntryForPromotion(entry1)).thenReturn(true);
        when(orderService.isValidEntryForPromotion(entry2)).thenReturn(true);
        when(orderService.isValidEntryForPromotion(entry3)).thenReturn(true);
        order.setTotalDiscount(10000d);
        order.setSubTotal(10300000d);
        entries.add(entry0);
        entries.add(entry1);
        entries.add(entry2);
        entries.add(entry3);
        when(entryRepository.findAllByOrder(any())).thenReturn(entries);
        strategy.splitOrderPromotionToEntries(order);
        assertNotNull(entry1.getDiscountOrderToItem());
        assertNotNull(entry2.getDiscountOrderToItem());
        assertNotNull(entry3.getDiscountOrderToItem());
        assertEquals(0, entry0.getDiscountOrderToItem(), 0);
        assertEquals(196, entry1.getDiscountOrderToItem(), 0);
        assertEquals(4902, entry2.getDiscountOrderToItem(), 0);
        assertEquals(4902, entry3.getDiscountOrderToItem(), 0);
    }

    /**
     * Order contains combo should ignore combo when splitting promotion amount
     * order with promotion amount: 10.000
     * order with entries:
     * subtotal order = 200.000 * 1 + 1.000.000 * 5 + 2.500.000 * 2 + 100.000 = 10.300.000
     * entry0: comboEntry: price: 100.000, qty: 1, orderPromotion = 10000 - 4854 - 4854 - 194 = 98
     * entry1: price 200.000 , qty 1, orderPromotion = 200000/ 10300000 * 10000 = 194
     * entry2: price 1.000.000, qty 5, orderPromotion = 5000000 / 10300000 * 10000 = 4854
     * entry3: price 2.500.000, qty 2, orderPromotion = 5000000 / 10300000 * 10000 = 4854
     */
    @Test
    public void splitOrderPromotionToEntries_Case4_ComboValidAppliedPromotion() {
        OrderEntryModel entry0 = generateEntry(11l, 100000d);
        OrderEntryModel entry1 = generateEntry(1l, 200000d);
        OrderEntryModel entry2 = generateEntry(2l, 5000000d);
        OrderEntryModel entry3 = generateEntry(3l, 5000000d);
        when(orderService.isComboEntry(entry0)).thenReturn(true);
        when(orderService.isValidEntryForPromotion(entry0)).thenReturn(true);
        when(orderService.isValidEntryForPromotion(entry1)).thenReturn(true);
        when(orderService.isValidEntryForPromotion(entry2)).thenReturn(true);
        when(orderService.isValidEntryForPromotion(entry3)).thenReturn(true);
        order.setTotalDiscount(10000d);
        order.setSubTotal(10300000d);
        entries.add(entry0);
        entries.add(entry1);
        entries.add(entry2);
        entries.add(entry3);
        when(entryRepository.findAllByOrder(any())).thenReturn(entries);
        strategy.splitOrderPromotionToEntries(order);
        assertNotNull(entry1.getDiscountOrderToItem());
        assertNotNull(entry2.getDiscountOrderToItem());
        assertNotNull(entry3.getDiscountOrderToItem());
        assertEquals(98, entry0.getDiscountOrderToItem(), 0);
        assertEquals(194, entry1.getDiscountOrderToItem(), 0);
        assertEquals(4854, entry2.getDiscountOrderToItem(), 0);
        assertEquals(4854, entry3.getDiscountOrderToItem(), 0);
    }

    /**
     * order with promotion amount: 100.000
     * order with entries:
     * subtotal entry order = 25.000 * 4 + 30.000 * 5 + 196.000 * 2 = 642.000
     * toppings: entry1:
     * --- option 1: qty = 2 => 2* 10.000 + 2 * 2 * 5.000 = 40.000
     *              --topping: 1 * 10.000, 2*5.000
     * --- option 2: qt = 1 => 1*2*15.0000 + 1*1*10.000 = 40.000
     *              --topping: 2* 15.000, 1*10.000
     * totalSub = 642.000 + 80.000 = 722.000
     * entry1: price 100.000 , qty 4, orderPromotion = 100000 / 722000 * 100000 = 13850
     * entry2: price 150.000, qty 5, orderPromotion = 150000 / 722000 * 100000 = 20776
     * entry3: price 392.000, qty 2, orderPromotion = 392000 / 722000 * 100000 = 54294
     * topping1: price 20.000, qty 2, orderPromotion = 20000 / 722000 * 100000 = 2770
     * topping2: price 20.000, qty 4, orderPromotion = 20000 / 722000 * 100000 = 2770
     * topping3: price 30.000, qty 2, orderPromotion = 30000 / 722000 * 100000 = 4155
     * topping4: price 10.000, qty 1, orderPromotion = 100000 - 2770 - 13850 - 20775 - 54293 - 2770 - 4155 = 1385
     */
    @Test
    public void splitOrderPromotionToEntries_Case3_hasOptionsWithTopping() {
        OrderEntryModel entry1 = generateEntry(1l, 100000d);
        entry1.setQuantity(4l);
        ToppingOptionModel toppingOption1 = getToppingOptionModel(11l, 2);
        ToppingItemModel item1 = getToppingItemModel(111l, 1, 10000d, toppingOption1);
        ToppingItemModel item2 = getToppingItemModel(112l, 2, 5000d, toppingOption1);
        toppingOption1.setToppingItemModels(new HashSet<>(Arrays.asList(item1, item2)));
        ToppingOptionModel toppingOption2 = getToppingOptionModel(22l, 1);
        ToppingItemModel item21 = getToppingItemModel(221l, 2, 15000d, toppingOption2);
        ToppingItemModel item22 = getToppingItemModel(222l, 1, 10000d, toppingOption2);
        toppingOption2.setToppingItemModels(new HashSet<>(Arrays.asList(item21, item22)));
        entry1.getToppingOptionModels().add(toppingOption1);
        entry1.getToppingOptionModels().add(toppingOption2);
        OrderEntryModel entry2 = generateEntry(2l, 150000d);
        entry2.setQuantity(5l);
        OrderEntryModel entry3 = generateEntry(3l, 392000d);
        entry3.setQuantity(2l);
        order.setTotalDiscount(100000d);
        order.setSubTotal(722000d);
        entries.add(entry1);
        entries.add(entry2);
        entries.add(entry3);
        Set<ToppingItemModel> toppingItemModels = new HashSet<>();
        toppingItemModels.add(item1);
        toppingItemModels.add(item2);
        toppingItemModels.add(item21);
        toppingItemModels.add(item22);

        when(orderService.isValidEntryForPromotion(entry1)).thenReturn(true);
        when(orderService.isValidEntryForPromotion(entry2)).thenReturn(true);
        when(orderService.isValidEntryForPromotion(entry3)).thenReturn(true);
        when(entryRepository.findAllByOrder(any())).thenReturn(entries);
        when(toppingItemRepository.findAllByEntryId(eq(1L))).thenReturn(toppingItemModels);
        strategy.splitOrderPromotionToEntries(order);
        assertNotNull(entry1.getDiscountOrderToItem());
        assertNotNull(entry2.getDiscountOrderToItem());
        assertNotNull(entry3.getDiscountOrderToItem());
        assertNotNull(item1.getDiscountOrderToItem());
        assertNotNull(item2.getDiscountOrderToItem());
        assertNotNull(item21.getDiscountOrderToItem());
        assertNotNull(item22.getDiscountOrderToItem());
        assertEquals(13850, entry1.getDiscountOrderToItem(), 0);
        assertEquals(20776, entry2.getDiscountOrderToItem(), 0);
        assertEquals(54294, entry3.getDiscountOrderToItem(), 0);
        assertEquals(2770, item1.getDiscountOrderToItem(), 0);
        assertEquals(2770, item2.getDiscountOrderToItem(), 0);
        assertEquals(4155, item21.getDiscountOrderToItem(), 0);
        assertEquals(1385, item22.getDiscountOrderToItem(), 0);
    }

    private ToppingOptionModel getToppingOptionModel(Long id, int qty) {
        ToppingOptionModel toppingOption1 = new ToppingOptionModel();
        toppingOption1.setId(id);
        toppingOption1.setQuantity(qty);
        return toppingOption1;
    }

    private ToppingItemModel getToppingItemModel(Long id, int qty, double price, ToppingOptionModel option) {
        ToppingItemModel item1 = new ToppingItemModel();
        item1.setId(id);
        item1.setQuantity(qty);
        item1.setBasePrice(price);
        item1.setToppingOptionModel(option);
        return item1;
    }

    private OrderEntryModel generateEntry(Long id, Double finalPrice) {
        OrderEntryModel model = new OrderEntryModel();
        model.setId(id);
        model.setFinalPrice(finalPrice);
        return model;
    }

    /**
     * order with promotion amount: 100.000
     * order with entries:
     * subtotal order = 250.000 * 2 = 500.000
     * combo entry1: price 250.000 , qty 2, orderPromotion = 100000
     */
    @Test
    public void splitOrderPromotionToEntries_OrderHasOnlyOneComboEntry() {
        OrderEntryModel comboEntry = new OrderEntryModel();
        comboEntry.setProductId(222l);
        comboEntry.setBasePrice(250000d);
        comboEntry.setQuantity(2l);
        comboEntry.setFinalPrice(500000d);
        comboEntry.setSubOrderEntries(new LinkedHashSet(Arrays.asList(new SubOrderEntryModel())));

        order.setTotalDiscount(100000d);
        order.setSubTotal(500000d);
        entries.add(comboEntry);
        when(orderService.isComboEntry(comboEntry)).thenReturn(true);
        when(entryRepository.findAllByOrder(any())).thenReturn(entries);

        strategy.splitOrderPromotionToEntries(order);
        assertEquals(100000, comboEntry.getDiscountOrderToItem(), 0);
    }

    /**
     * order with promotion amount: 100.000
     * order with 2 entries:
     * subtotal order = 250.000 * 2 + 2 * 150,000= 800.000
     * combo entry1: price 250.000 , qty 2, orderPromotion = 62,500
     * combo entry2: price 150.000 , qty 2, orderPromotion = 37,500
     */
    @Test
    public void splitOrderPromotionToEntries_OrderHas2ComboEntries() {
        OrderEntryModel comboEntry = new OrderEntryModel();
        comboEntry.setId(111l);
        comboEntry.setProductId(222l);
        comboEntry.setBasePrice(250000d);
        comboEntry.setQuantity(2l);
        comboEntry.setFinalPrice(500000d);
        comboEntry.setSubOrderEntries(new LinkedHashSet(Arrays.asList(new SubOrderEntryModel())));

        OrderEntryModel comboEntry2 = new OrderEntryModel();
        comboEntry2.setId(112l);
        comboEntry2.setProductId(223l);
        comboEntry2.setBasePrice(150000d);
        comboEntry2.setQuantity(2l);
        comboEntry2.setFinalPrice(300000d);
        comboEntry2.setSubOrderEntries(new LinkedHashSet(Arrays.asList(new SubOrderEntryModel())));

        order.setTotalDiscount(100000d);
        order.setSubTotal(800000d);
        entries.add(comboEntry);
        entries.add(comboEntry2);
        when(orderService.isComboEntry(comboEntry)).thenReturn(true);
        when(orderService.isComboEntry(comboEntry2)).thenReturn(true);
        when(entryRepository.findAllByOrder(any())).thenReturn(entries);

        strategy.splitOrderPromotionToEntries(order);
        assertEquals(62500, comboEntry.getDiscountOrderToItem(), 0);
        assertEquals(37500, comboEntry2.getDiscountOrderToItem(), 0);
    }

    /**
     * order with discount amount: 30.000
     * order with entries:
     * subtotal order = 250.000 * 2 + 1 * 20000 = 520.000
     * combo entry1: price 250.000 , qty 2,
     * normal entry2: price 20.000, qty: 1
     * orderTotalDiscount = 30000
     * Expected: discount to entry1: 10.000, entry2: 20.000
     */
    @Test
    public void splitOrderPromotionToEntries_OrderHasComboEntry_AndNormalEntry_AndDiscountOverFinalPriceOfNormalEntry() {
        OrderEntryModel comboEntry = new OrderEntryModel();
        comboEntry.setProductId(222l);
        comboEntry.setBasePrice(250000d);
        comboEntry.setQuantity(2l);
        comboEntry.setFinalPrice(500000d);
        comboEntry.setSubOrderEntries(new LinkedHashSet(Arrays.asList(new SubOrderEntryModel())));
        OrderEntryModel normalEntry = generateEntry(1l, 20000d);
        order.setTotalDiscount(30000d);
        order.setSubTotal(520000d);
        entries.add(comboEntry);
        entries.add(normalEntry);
        when(orderService.isComboEntry(comboEntry)).thenReturn(true);
        when(orderService.isComboEntry(normalEntry)).thenReturn(false);
        when(orderService.isValidEntryForPromotion(comboEntry)).thenReturn(false);
        when(orderService.isValidEntryForPromotion(normalEntry)).thenReturn(true);
        when(entryRepository.findAllByOrder(any())).thenReturn(entries);

        strategy.splitOrderPromotionToEntries(order);
        assertEquals(20000, normalEntry.getDiscountOrderToItem(), 0);
        assertEquals(10000, comboEntry.getDiscountOrderToItem(), 0);
    }

    /**
     * order with discount amount: 30.000
     * order with entries:
     * subtotal order = 250.000 * 2 + 1 * 20000 = 520.000
     * combo entry1: price 250.000 , qty 2,
     * normal entry2: price 20.000, qty: 1
     * orderTotalDiscount = 30000
     * Expected: discount to entry1: 10.000, entry2: 20.000
     */
    @Test
    public void splitOrderPromotionToEntries_OrderHasComboEntry_AndSaleOffEntry_AndDiscountOverFinalPriceOfNormalEntry() {
        OrderEntryModel comboEntry = new OrderEntryModel();
        comboEntry.setProductId(222l);
        comboEntry.setBasePrice(250000d);
        comboEntry.setQuantity(2l);
        comboEntry.setFinalPrice(500000d);
        comboEntry.setSubOrderEntries(new LinkedHashSet(Arrays.asList(new SubOrderEntryModel())));
        OrderEntryModel normalEntry = generateEntry(1l, 20000d);
        normalEntry.setSaleOff(true);
        order.setTotalDiscount(30000d);
        order.setSubTotal(520000d);
        entries.add(comboEntry);
        entries.add(normalEntry);
        when(orderService.isComboEntry(comboEntry)).thenReturn(true);
        when(orderService.isComboEntry(normalEntry)).thenReturn(false);
        when(orderService.isValidEntryForPromotion(comboEntry)).thenReturn(false);
        when(orderService.isValidEntryForPromotion(normalEntry)).thenReturn(false);
        when(entryRepository.findAllByOrder(any())).thenReturn(entries);

        strategy.splitOrderPromotionToEntries(order);
        assertEquals(1154, normalEntry.getDiscountOrderToItem(), 0);
        assertEquals(28846, comboEntry.getDiscountOrderToItem(), 0);
    }

    @Test
    public void calculateLoyaltyRewardOrder_emptyCardNumberAndCustomerId() {
        strategy.calculateLoyaltyRewardOrder(order);
        verify(loyaltyService, times(0)).getAwardProducts(any());
        verify(loyaltyService, times(0)).isApplied(any());
        verify(customerService, times(0)).getCustomerById(anyLong(), anyLong());
        verify(calculationService, times(0)).calculateLoyaltyAmount(anyList(), anyLong());
        verify(loyaltyService, times(0)).convertAmountToPoint(anyDouble(), anyLong());
    }

    @Test
    public void calculateLoyaltyRewardOrder_emptyProductReward() {
        order.setCompanyId(2L);
        order.setCardNumber("1122334455");
        order.setCustomerId(2L);
        when(customerService.getBasicCustomerInfo(anyLong(), anyLong())).thenReturn(new CustomerData());
        when(loyaltyService.getAwardProducts(any())).thenReturn(new ArrayList<>());
        strategy.calculateLoyaltyRewardOrder(order);
        verify(loyaltyService, times(1)).getAwardProducts(any());
        verify(loyaltyService, times(0)).isApplied(any());
        verify(customerService, times(0)).getCustomerById(anyLong(), anyLong());
        verify(calculationService, times(0)).calculateLoyaltyAmount(anyList(), anyLong());
        verify(loyaltyService, times(0)).convertAmountToPoint(anyDouble(), anyLong());
    }

    @Test
    public void calculateLoyaltyRewardOrder_withCardNumber_invalid() {
        order.setCompanyId(2L);
        order.setCardNumber("1122334455");
        order.setCustomerId(2L);
        when(customerService.getBasicCustomerInfo(anyLong(), anyLong())).thenReturn(new CustomerData());
        when(loyaltyService.getAwardProducts(any())).thenReturn(Arrays.asList(new ProductCanRewardDto()));
        when(loyaltyService.isApplied(any())).thenReturn(false);
        strategy.calculateLoyaltyRewardOrder(order);
        verify(loyaltyService, times(1)).getAwardProducts(any());
        verify(loyaltyService, times(1)).isApplied(any());
        verify(customerService, times(0)).getCustomerById(anyLong(), anyLong());
        verify(calculationService, times(0)).calculateLoyaltyAmount(anyList(), anyLong());
        verify(loyaltyService, times(0)).convertAmountToPoint(anyDouble(), anyLong());
    }

    @Test
    public void calculateLoyaltyRewardOrder_withCardNumber_RewardAmountSmallThan_0() {
        order.setCompanyId(2L);
        order.setCardNumber("1122334455");
        order.setCustomerId(2L);
        when(customerService.getBasicCustomerInfo(anyLong(), anyLong())).thenReturn(new CustomerData());
        when(loyaltyService.getAwardProducts(any())).thenReturn(Arrays.asList(new ProductCanRewardDto()));
        when(loyaltyService.isApplied(any())).thenReturn(true);
        when(calculationService.calculateLoyaltyAmount(anyList(), anyLong())).thenReturn(0d);
        strategy.calculateLoyaltyRewardOrder(order);
        verify(loyaltyService, times(1)).getAwardProducts(any());
        verify(loyaltyService, times(1)).isApplied(any());
        verify(customerService, times(0)).getCustomerById(anyLong(), anyLong());
        verify(calculationService, times(1)).calculateLoyaltyAmount(anyList(), anyLong());
        verify(loyaltyService, times(0)).convertAmountToPoint(anyDouble(), anyLong());
    }

    @Test
    public void calculateLoyaltyRewardOrder_withCardNumber_RewardAmountLargeThan_0() {
        order.setCompanyId(2L);
        order.setCardNumber("1122334455");
        order.setCustomerId(2L);
        when(customerService.getBasicCustomerInfo(anyLong(), anyLong())).thenReturn(new CustomerData());
        when(loyaltyService.getAwardProducts(any())).thenReturn(Arrays.asList(new ProductCanRewardDto()));
        when(loyaltyService.isApplied(any())).thenReturn(true);
        when(calculationService.calculateLoyaltyAmount(anyList(), anyLong())).thenReturn(10000d);
        when(loyaltyService.convertAmountToPoint(anyDouble(), anyLong())).thenReturn(10d);
        strategy.calculateLoyaltyRewardOrder(order);
        verify(loyaltyService, times(1)).getAwardProducts(any());
        verify(loyaltyService, times(1)).isApplied(any());
        verify(customerService, times(0)).getCustomerById(anyLong(), anyLong());
        verify(calculationService, times(1)).calculateLoyaltyAmount(anyList(), anyLong());
        verify(loyaltyService, times(1)).convertAmountToPoint(anyDouble(), anyLong());
    }

    @Test
    public void calculateLoyaltyRewardOrder_withCustomerId_RewardAmountLargeThan_0() {
        order.setCompanyId(2L);
        order.setCustomerId(1234L);
        CustomerData customerData = new CustomerData();
        customerData.setPhone("0978686865");
        when(loyaltyService.getAwardProducts(any())).thenReturn(Arrays.asList(new ProductCanRewardDto()));
        when(customerService.getBasicCustomerInfo(anyLong(), anyLong())).thenReturn(customerData);
        when(calculationService.calculateLoyaltyAmount(anyList(), anyLong())).thenReturn(10000d);
        when(loyaltyService.convertAmountToPoint(anyDouble(), anyLong())).thenReturn(10d);
        strategy.calculateLoyaltyRewardOrder(order);
        verify(loyaltyService, times(1)).getAwardProducts(any());
        verify(loyaltyService, times(0)).isApplied(any());
        verify(customerService, times(1)).getBasicCustomerInfo(anyLong(), anyLong());
        verify(calculationService, times(1)).calculateLoyaltyAmount(anyList(), anyLong());
        verify(loyaltyService, times(1)).convertAmountToPoint(anyDouble(), anyLong());
    }

    @Test
    public void calculateTotalRewardAmount_emptyCustomer() {
        Double totalReward = strategy.calculateTotalRewardAmount(order);
        assertNull(totalReward);
        verify(loyaltyService, times(0)).getAwardProducts(any());
        verify(calculationService, times(0)).calculateLoyaltyAmount(any(), anyLong());
    }

    @Test
    public void calculateTotalRewardAmount() {
        order.setCompanyId(2L);
        order.setCustomerId(2L);
        order.setCustomerId(2L);
        when(customerService.getBasicCustomerInfo(anyLong(), anyLong())).thenReturn(new CustomerData());
        when(loyaltyService.getAwardProducts(any())).thenReturn(Arrays.asList(new ProductCanRewardDto()));
        when(calculationService.calculateLoyaltyAmount(any(), anyLong())).thenReturn(1000d);
        Double totalReward = strategy.calculateTotalRewardAmount(order);
        verify(loyaltyService).getAwardProducts(any());
        verify(calculationService).calculateLoyaltyAmount(any(), anyLong());
        assertEquals(1000d, totalReward, 0);
    }
}
