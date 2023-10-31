package com.vctek.orderservice.promotionengine.promotionservice.strategy.impl;

import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionResultModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.RuleBasedOrderEntryAdjustActionModel;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionActionService;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.AbstractRuleActionRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.DiscountRAO;
import com.vctek.orderservice.service.CalculationService;
import com.vctek.orderservice.service.ModelService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class DefaultOrderEntryAdjustActionStrategyTest {
    private static final BigDecimal DISCOUNT_AMOUNT = BigDecimal.valueOf(5);

    private DefaultOrderEntryAdjustActionStrategy defaultOrderEntryAdjustActionStrategy;

    @Mock
    private PromotionActionService promotionActionService;

    @Mock
    private DiscountRAO discountRao;

    @Mock
    private AbstractOrderEntryModel orderEntry;

    @Mock
    private PromotionResultModel promotionResult;

    @Mock
    private CartModel cart;

    @Mock
    private ModelService modelService;

    @Mock
    private CalculationService calculationService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        defaultOrderEntryAdjustActionStrategy = new DefaultOrderEntryAdjustActionStrategy(modelService,
                promotionActionService, calculationService);
        when(promotionActionService.getOrderEntry(discountRao)).thenReturn(orderEntry);
        when(promotionActionService.createPromotionResult(discountRao)).thenReturn(promotionResult);
    }

    @Test
    public void testApplyNotDiscountRAO() {
        final List result = defaultOrderEntryAdjustActionStrategy.apply(new AbstractRuleActionRAO());
        assertTrue(result.isEmpty());
    }

    @Test
    public void testApplyOrderEntryNull() {
        when(promotionActionService.getOrderEntry(any(DiscountRAO.class))).thenReturn(null);
        final List result = defaultOrderEntryAdjustActionStrategy.apply(discountRao);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testApplyPromotionResultNull() {
        when(promotionActionService.createPromotionResult(discountRao)).thenReturn(null);
        final List result = defaultOrderEntryAdjustActionStrategy.apply(discountRao);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testApplyOrderNull() {
        when(orderEntry.getOrder()).thenReturn(null);
        final List result = defaultOrderEntryAdjustActionStrategy.apply(discountRao);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testApply() {
        when(orderEntry.getOrder()).thenReturn(cart);
        when(Boolean.valueOf(discountRao.isPerUnit())).thenReturn(Boolean.FALSE);

        final List result = defaultOrderEntryAdjustActionStrategy.apply(discountRao);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(promotionResult, result.get(0));
    }

    @Test
    public void testUndo() {
        final RuleBasedOrderEntryAdjustActionModel action = new RuleBasedOrderEntryAdjustActionModel();
        action.setPromotionResult(promotionResult);
        defaultOrderEntryAdjustActionStrategy.undo(action);
//        verify(calculationService).calculateTotals(eq(null), anyBoolean());
        verify(promotionActionService).removeDiscountValue(any(), eq(null));
    }

    @Test
    public void testCreateOrderEntryAdjustAction() {
        when(orderEntry.getEntryNumber()).thenReturn(Integer.valueOf(0));
        when(orderEntry.getProductId()).thenReturn(1l);

        final RuleBasedOrderEntryAdjustActionModel action = defaultOrderEntryAdjustActionStrategy.createOrderEntryAdjustAction(
                promotionResult, discountRao, orderEntry, DISCOUNT_AMOUNT);

        assertEquals(promotionResult, action.getPromotionResult());
        assertEquals(DISCOUNT_AMOUNT, action.getAmount());
        assertEquals(orderEntry.getEntryNumber(), action.getOrderEntryNumber());
        assertEquals(1l, action.getProductId(), 0);
    }
}
