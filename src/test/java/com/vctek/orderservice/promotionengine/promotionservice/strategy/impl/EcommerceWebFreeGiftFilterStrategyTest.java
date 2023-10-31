package com.vctek.orderservice.promotionengine.promotionservice.strategy.impl;

import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.AbstractRuleActionRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.FreeProductRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.OrderEntryRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.ProductRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleConditionsService;
import com.vctek.orderservice.service.InventoryService;
import com.vctek.orderservice.util.ProductDType;
import com.vctek.orderservice.util.SellSignal;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class EcommerceWebFreeGiftFilterStrategyTest {
    private DefaultEcommerceWebFreeGiftFilterStrategy strategy;
    @Mock
    private InventoryService inventoryService;
    private Set<PromotionSourceRuleModel> sourceRules;
    private List<FreeProductRAO> freeGiftActions;
    @Mock
    private AbstractOrderModel order;
    @Mock
    private PromotionSourceRuleModel rule1;
    @Mock
    private PromotionSourceRuleModel rule2;
    @Mock
    private PromotionSourceRuleModel rule3;
    @Mock
    private FreeProductRAO freeP1;
    @Mock
    private FreeProductRAO freeP2;
    @Mock
    private FreeProductRAO freeP3;
    @Mock
    private OrderEntryRAO addEntry1;
    @Mock
    private OrderEntryRAO addEntry2;
    @Mock
    private OrderEntryRAO addEntry3;
    @Mock
    private ProductRAO p1;
    @Mock
    private ProductRAO p2;
    @Mock
    private ProductRAO p3;
    private Map<Long, Integer> stockMap;
    @Mock
    private RuleConditionsService ruleConditionsService;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        stockMap = new HashMap<>();
        strategy = new DefaultEcommerceWebFreeGiftFilterStrategy();
        strategy.setInventoryService(inventoryService);
        strategy.setRuleConditionsService(ruleConditionsService);
        sourceRules = new HashSet<>();
        sourceRules.add(rule1);
        sourceRules.add(rule2);
        sourceRules.add(rule3);
        when(rule1.getId()).thenReturn(1l);
        when(rule1.getCode()).thenReturn("r1");
        when(rule2.getId()).thenReturn(2l);
        when(rule2.getCode()).thenReturn("r2");
        when(rule3.getId()).thenReturn(3l);
        when(rule3.getCode()).thenReturn("r3");
        freeGiftActions = new ArrayList<>();
        freeGiftActions.add(freeP1);
        freeGiftActions.add(freeP2);
        freeGiftActions.add(freeP3);
        when(order.getAppliedPromotionSourceRuleId()).thenReturn(null);
        when(order.getCompanyId()).thenReturn(2l);
        when(order.getSellSignal()).thenReturn(SellSignal.ECOMMERCE_WEB.toString());
        when(freeP1.getFiredRuleCode()).thenReturn("r1");
        when(freeP1.getAddedOrderEntry()).thenReturn(addEntry1);
        when(addEntry1.getProduct()).thenReturn(p1);
        when(p1.getId()).thenReturn(111l);
        when(p1.getDtype()).thenReturn(ProductDType.PRODUCT_MODEL.code());

        when(freeP2.getFiredRuleCode()).thenReturn("r2");
        when(freeP2.getAddedOrderEntry()).thenReturn(addEntry2);
        when(addEntry2.getProduct()).thenReturn(p2);
        when(p2.getId()).thenReturn(222l);
        when(p2.getDtype()).thenReturn(ProductDType.PRODUCT_MODEL.code());

        when(freeP3.getFiredRuleCode()).thenReturn("r3");
        when(freeP3.getAddedOrderEntry()).thenReturn(addEntry3);
        when(addEntry3.getProduct()).thenReturn(p3);
        when(p3.getId()).thenReturn(333l);
        when(p3.getDtype()).thenReturn(ProductDType.VARIANT_PRODUCT_MODEL.code());
        when(inventoryService.getStoreFrontAvailableStockOfProductList(anyLong(), anyList())).thenReturn(stockMap);
    }

    @Test
    public void filterNotSupportFreeGiftComboProduct_notEcommerceWeb() {
        when(order.getSellSignal()).thenReturn(SellSignal.WEB.toString());
        strategy.filterNotSupportFreeGiftComboProduct(sourceRules, freeGiftActions, order);
        assertEquals(3, sourceRules.size());
        assertEquals(3, freeGiftActions.size());
    }

    @Test
    public void filterNotSupportFreeGiftComboProduct_NotContainCombo() {
        when(order.getSellSignal()).thenReturn(SellSignal.WEB.toString());
        strategy.filterNotSupportFreeGiftComboProduct(sourceRules, freeGiftActions, order);
        assertEquals(3, sourceRules.size());
        assertEquals(3, freeGiftActions.size());
    }

    @Test
    public void filterNotSupportFreeGiftComboProduct_Contain1Combo() {
        when(p2.getDtype()).thenReturn(ProductDType.COMBO_MODEL.code());
        strategy.filterNotSupportFreeGiftComboProduct(sourceRules, freeGiftActions, order);
        assertEquals(2, sourceRules.size());
        assertTrue(sourceRules.contains(rule1));
        assertTrue(sourceRules.contains(rule3));

        assertEquals(2, freeGiftActions.size());
        assertTrue(freeGiftActions.contains(freeP1));
        assertTrue(freeGiftActions.contains(freeP3));
    }

    @Test
    public void filterNotSupportFreeGiftComboProduct_ContainAllCombo() {
        when(freeP1.getAddedOrderEntry()).thenReturn(null);
        when(p2.getDtype()).thenReturn(ProductDType.COMBO_MODEL.code());
        when(p3.getDtype()).thenReturn(ProductDType.COMBO_MODEL.code());
        strategy.filterNotSupportFreeGiftComboProduct(sourceRules, freeGiftActions, order);
        assertEquals(0, sourceRules.size());
        assertEquals(0, freeGiftActions.size());
    }

    @Test
    public void filterFreeGiftAppliedAction_notCommerceWeb() {
        when(order.getSellSignal()).thenReturn(SellSignal.WEB.toString());
        AbstractRuleActionRAO abstractRuleActionRAO = strategy.filterFreeGiftAppliedAction(sourceRules, freeGiftActions, order);
        assertNull(abstractRuleActionRAO);
        assertEquals(3, sourceRules.size());
        assertEquals(3, freeGiftActions.size());
    }

    @Test
    public void filterFreeGiftAppliedAction_EmptyFreeGiftActions() {
        AbstractRuleActionRAO abstractRuleActionRAO = strategy.filterFreeGiftAppliedAction(sourceRules, new ArrayList<>(), order);
        assertNull(abstractRuleActionRAO);
        assertEquals(0, sourceRules.size());
    }

    @Test
    public void filterFreeGiftAppliedAction_AllProductGift_outOfStock() {
        AbstractRuleActionRAO abstractRuleActionRAO = strategy.filterFreeGiftAppliedAction(sourceRules, freeGiftActions, order);
        assertEquals(freeP1, abstractRuleActionRAO);
        assertEquals(0, sourceRules.size());
    }

    @Test
    public void filterFreeGiftAppliedAction_HasOneValidProduct() {
        stockMap.put(111l, 3);
        AbstractRuleActionRAO abstractRuleActionRAO = strategy.filterFreeGiftAppliedAction(sourceRules, freeGiftActions, order);
        assertEquals(freeP1, abstractRuleActionRAO);
        assertEquals(0, sourceRules.size());
    }

    @Test
    public void filterFreeGiftAppliedAction_Has2ValidProducts() {
        stockMap.put(222l, 3);
        stockMap.put(333l, 3);
        AbstractRuleActionRAO abstractRuleActionRAO = strategy.filterFreeGiftAppliedAction(sourceRules, freeGiftActions, order);
        assertEquals(null, abstractRuleActionRAO);
        assertEquals(2, sourceRules.size());
        assertEquals(3, freeGiftActions.size());
    }

    @Test
    public void filterFreeGiftAppliedAction_SelectedGiftIsTheSameOneAvailableGift() {
        when(order.getAppliedPromotionSourceRuleId()).thenReturn(1l);
        stockMap.put(111l, 3);
        stockMap.put(222l, 0);
        stockMap.put(333l, 0);
        AbstractRuleActionRAO abstractRuleActionRAO = strategy.filterFreeGiftAppliedAction(sourceRules, freeGiftActions, order);
        assertEquals(freeP1, abstractRuleActionRAO);
        assertEquals(0, sourceRules.size());
        verify(order, times(0)).setHasChangeGift(true);
        verify(order, times(0)).setAppliedPromotionSourceRuleId(null);
    }

    @Test
    public void filterFreeGiftAppliedAction_SelectedGift_IsNotTheSameOneAvailableGift() {
        when(order.getAppliedPromotionSourceRuleId()).thenReturn(1l);
        stockMap.put(111l, 0);
        stockMap.put(222l, 1);
        stockMap.put(333l, 0);
        AbstractRuleActionRAO abstractRuleActionRAO = strategy.filterFreeGiftAppliedAction(sourceRules, freeGiftActions, order);
        assertEquals(freeP2, abstractRuleActionRAO);
        assertEquals(0, sourceRules.size());
        verify(order).setHasChangeGift(true);
        verify(order).setAppliedPromotionSourceRuleId(null);
    }

    @Test
    public void filterFreeGiftAppliedAction_HasMoreThanOneAvailableGift_NotContainSelectedGift() {
        when(order.getAppliedPromotionSourceRuleId()).thenReturn(1l);
        stockMap.put(111l, 1);
        stockMap.put(222l, 1);
        stockMap.put(333l, 1);
        sourceRules.clear();
        sourceRules.add(rule2);
        sourceRules.add(rule3);

        AbstractRuleActionRAO abstractRuleActionRAO = strategy.filterFreeGiftAppliedAction(sourceRules, Arrays.asList(freeP2, freeP3), order);
        assertEquals(null, abstractRuleActionRAO);
        assertEquals(2, sourceRules.size());
        verify(order).setHasChangeGift(true);
        verify(order).setAppliedPromotionSourceRuleId(null);
    }

    @Test
    public void filterFreeGiftAppliedAction_HasMoreThanOneAvailableGift_ContainSelectedGift_ButOutOfStock() {
        when(order.getAppliedPromotionSourceRuleId()).thenReturn(1l);
        stockMap.put(111l, 0);
        stockMap.put(222l, 1);
        stockMap.put(333l, 1);

        AbstractRuleActionRAO abstractRuleActionRAO = strategy.filterFreeGiftAppliedAction(sourceRules, freeGiftActions, order);
        assertEquals(null, abstractRuleActionRAO);
        assertEquals(2, sourceRules.size());
        verify(order).setHasChangeGift(true);
        verify(order).setAppliedPromotionSourceRuleId(null);
    }

    @Test
    public void filterFreeGiftAppliedAction_AllGiftOutOfStock() {
        when(order.getAppliedPromotionSourceRuleId()).thenReturn(1l);
        stockMap.put(111l, 0);
        stockMap.put(222l, 0);
        stockMap.put(333l, 0);

        AbstractRuleActionRAO abstractRuleActionRAO = strategy.filterFreeGiftAppliedAction(sourceRules, freeGiftActions, order);
        assertEquals(freeP1, abstractRuleActionRAO);
        assertEquals(0, sourceRules.size());
        verify(order, times(0)).setHasChangeGift(true);
        verify(order, times(0)).setAppliedPromotionSourceRuleId(null);
    }
}
