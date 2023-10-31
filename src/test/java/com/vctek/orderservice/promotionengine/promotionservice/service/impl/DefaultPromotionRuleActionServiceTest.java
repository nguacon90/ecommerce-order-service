package com.vctek.orderservice.promotionengine.promotionservice.service.impl;

import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.model.ItemModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionResultModel;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionActionService;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.AbstractRuleActionRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.DiscountRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.RuleEngineResultRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.strategy.RuleActionStrategy;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultPromotionRuleActionServiceTest {
    @Mock
    private PromotionActionService promotionActionService;
    @Mock
    private RuleActionStrategy ruleActionStrategy;
    @Mock
    private DiscountRAO discountAction;
    @Mock
    private RuleEngineResultRAO ruleEngineResultRAO;
    @Mock
    private CartModel cart;

    private Map<String, RuleActionStrategy> actionStrategiesMapping = new HashMap<>();
    private DefaultPromotionRuleActionService service;
    private LinkedHashSet<AbstractRuleActionRAO> actions = new LinkedHashSet<>();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        actionStrategiesMapping.put("actionRAO", ruleActionStrategy);
        actions.add(discountAction);
        final List<ItemModel> results = new ArrayList<>();
        PromotionResultModel promotionResultModel = new PromotionResultModel();
        promotionResultModel.setOrder(cart);
        results.add(promotionResultModel);
        when(ruleActionStrategy.getStrategyId()).thenReturn("testAction");
        when(ruleEngineResultRAO.getActions()).thenReturn(actions);
        Mockito.doReturn(results).when(ruleActionStrategy).apply(discountAction);
        Mockito.doReturn("actionRAO").when(discountAction).getActionStrategyKey();
        service = new DefaultPromotionRuleActionService(actionStrategiesMapping, promotionActionService);
    }

    @Test
    public void test() {
        service.applyAllActions(ruleEngineResultRAO);
        verify(promotionActionService).recalculateTotals(any(AbstractOrderModel.class));
    }
}
