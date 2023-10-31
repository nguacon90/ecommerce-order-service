package com.vctek.orderservice.promotionengine.promotionservice.strategy.impl;

import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionResultModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.RuleBasedOrderAdjustTotalActionModel;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionActionService;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;


public class DefaultOrderAdjustTotalActionStrategyTest {
    private static final BigDecimal DISCOUNT_VALUE = BigDecimal.valueOf(20);
    private static final String BEAN_NAME = "defaultOrderAdjustTotalActionStrategy";

    private DefaultOrderAdjustTotalActionStrategy defaultOrderAdjustTotalActionStrategy;

    @Mock
    private PromotionActionService promotionActionService;

    @Mock
    private ModelService modelService;

    @Mock
    private PromotionResultModel promotionResult;

    @Mock
    private DiscountRAO discountRao;

    @Mock
    private CartModel cart;

    @Mock
    private RuleBasedOrderAdjustTotalActionModel ruleBasedOrderAdjustTotalAction;

    @Mock
    private DroolsRuleModel rule;

    @Mock
    private CalculationService calculationService;

    private Class<RuleBasedOrderAdjustTotalActionModel> promotionAction;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        defaultOrderAdjustTotalActionStrategy = new DefaultOrderAdjustTotalActionStrategy(modelService,
                promotionActionService, calculationService);
        defaultOrderAdjustTotalActionStrategy.setBeanName(BEAN_NAME);
        when(promotionResult.getOrder()).thenReturn(cart);
        when(promotionActionService.getRule(discountRao)).thenReturn(rule);
    }

    @Test
    public void testApplyNotDiscountRAO() {
        final List result = defaultOrderAdjustTotalActionStrategy.apply(new AbstractRuleActionRAO());
        assertTrue(result.isEmpty());
    }

    @Test
    public void testApplyPromotionResultNull() {
        when(promotionActionService.createPromotionResult(any(DiscountRAO.class))).thenReturn(null);
        final List result = defaultOrderAdjustTotalActionStrategy.apply(new DiscountRAO());
        assertTrue(result.isEmpty());
    }

    @Test
    public void testApplyOrderNull() {
        when(promotionActionService.createPromotionResult(any(DiscountRAO.class))).thenReturn(promotionResult);
        when(promotionResult.getOrder()).thenReturn(null);
        final List result = defaultOrderAdjustTotalActionStrategy.apply(discountRao);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testApply() {
        when(promotionActionService.createPromotionResult(discountRao)).thenReturn(promotionResult);
        doNothing().when(promotionActionService).createDiscountValue(any(DiscountRAO.class), anyString(),
                any(AbstractOrderModel.class));

        final List result = defaultOrderAdjustTotalActionStrategy.apply(discountRao);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(promotionResult, result.get(0));
    }

    @Test
    public void testCreateOrderAdjustTotalAction() {
        when(discountRao.getValue()).thenReturn(DISCOUNT_VALUE);

        final RuleBasedOrderAdjustTotalActionModel action = defaultOrderAdjustTotalActionStrategy
                .createOrderAdjustTotalAction(promotionResult, discountRao);

        assertEquals(promotionResult, action.getPromotionResult());
        assertEquals(rule, action.getRule());
        assertEquals(BEAN_NAME, action.getStrategyId());
        assertEquals(DISCOUNT_VALUE, action.getAmount());
    }

    @Test
    public void testUndo() {
        final RuleBasedOrderAdjustTotalActionModel action = new RuleBasedOrderAdjustTotalActionModel();

        action.setPromotionResult(promotionResult);
        defaultOrderAdjustTotalActionStrategy.undo(action);
//        verify(calculationService).calculateTotals(cart, true);
        verify(promotionActionService).removeDiscountValue(any(), eq(cart));
    }
}
