package com.vctek.orderservice.promotionengine.ruleengineservice.evaluate.impl;

import com.vctek.orderservice.dto.ConsumeBudgetParam;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionBudgetConsumeService;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionSourceRuleService;
import com.vctek.orderservice.promotionengine.ruleengine.model.eveluation.RuleActionContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.RuleEngineCalculationService;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.*;
import com.vctek.orderservice.promotionengine.util.CurrencyIsoCode;
import org.drools.core.WorkingMemory;
import org.drools.core.spi.KnowledgeHelper;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.rule.FactHandle;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RuleEmployeeOrderPercentageDiscountRAOActionTest {
    private RuleEmployeeOrderPercentageDiscountRAOAction action;

    @Mock
    private RuleEngineCalculationService ruleEngineCalculationService;
    @Mock
    private PromotionBudgetConsumeService promotionBudgetConsumeService;
    @Mock
    private PromotionSourceRuleService promotionSourceRuleService;
    @Mock
    private RuleActionContext context;
    @Mock
    private Map<String, Object> params;
    @Mock
    private Map<String, Object> metaData;
    @Mock
    private KnowledgeHelper knowledgeHelper;
    @Mock
    private WorkingMemory workingMemmory;
    private List<FactHandle> facts = new ArrayList<>();

    @Mock
    private CartRAO cartRAO;
    private Map<String, BigDecimal> mapDiscount = new HashMap<>();
    private Set<OrderEntryRAO> entries = new HashSet<>();
    private RuleEngineResultRAO ruleEngineResultRAO = new RuleEngineResultRAO();
    @Mock
    private UserRAO userRao;
    private ArgumentCaptor<BigDecimal> captor;
    @Mock
    private DiscountRAO discountRao;
    @Mock
    private PromotionSourceRuleModel sourceRuleModel;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        captor = ArgumentCaptor.forClass(BigDecimal.class);
        action = new RuleEmployeeOrderPercentageDiscountRAOAction();
        action.setValidateRuleCode(true);
        action.setValidateRulesModuleName(true);
        action.setRuleEngineCalculationService(ruleEngineCalculationService);
        action.setPromotionSourceRuleService(promotionSourceRuleService);
        action.setPromotionBudgetConsumeService(promotionBudgetConsumeService);
        when(context.getParameters()).thenReturn(params);
        when(context.getRuleMetadata()).thenReturn(metaData);
        when(context.getParameter(AbstractRuleExecutableSupport.MAX_DISCOUNT_AMOUNT)).thenReturn(new BigDecimal(100000d));
        when(metaData.get(AbstractRuleExecutableSupport.RULE_CODE)).thenReturn("ruleCode");
        when(metaData.get(AbstractRuleExecutableSupport.MODULE_NAME)).thenReturn("promotion-module");
        when(context.getDelegate()).thenReturn(knowledgeHelper);
        when(knowledgeHelper.getWorkingMemory()).thenReturn(workingMemmory);
        when(workingMemmory.getFactHandles()).thenReturn(facts);
        when(context.getCartRao()).thenReturn(cartRAO);
        when(cartRAO.getCurrencyIsoCode()).thenReturn(CurrencyIsoCode.VND.toString());
        when(context.getParameter("value")).thenReturn(new BigDecimal(10));
        when(cartRAO.getEntries()).thenReturn(entries);
        when(cartRAO.getUser()).thenReturn(userRao);
        when(cartRAO.getCreatedDate()).thenReturn(Calendar.getInstance().getTime());
        when(userRao.getId()).thenReturn(1l);
        when(discountRao.getValue()).thenReturn(BigDecimal.ZERO);
        when(promotionSourceRuleService.findByCode(anyString())).thenReturn(sourceRuleModel);
        when(this.ruleEngineCalculationService.addOrderLevelDiscount(eq(cartRAO), anyBoolean(), any())).thenReturn(discountRao);
        entries.add(new OrderEntryRAO());
        ruleEngineResultRAO.setActions(new LinkedHashSet<>());
        when(context.getRuleEngineResultRao()).thenReturn(ruleEngineResultRAO);
    }

    @Test
    public void performAction_remainAmountSmallerThanActualDiscount() {
        when(promotionBudgetConsumeService.calculateConsumeBudgetAmount(any(ConsumeBudgetParam.class))).thenReturn(BigDecimal.valueOf(90000d));
        when(cartRAO.getTotal()).thenReturn(new BigDecimal(300000d));
        action.performAction(context);
        verify(this.ruleEngineCalculationService)
                .addOrderLevelDiscount(eq(cartRAO), eq(true), captor.capture());
        assertEquals(10000d, captor.getValue().doubleValue(), 0);
    }

    @Test
    public void performAction_remainAmountLargerThanActualDiscount() {
        when(promotionBudgetConsumeService.calculateConsumeBudgetAmount(any(ConsumeBudgetParam.class))).thenReturn(BigDecimal.valueOf(90000d));
        when(cartRAO.getTotal()).thenReturn(new BigDecimal(30000d));
        action.performAction(context);
        verify(this.ruleEngineCalculationService)
                .addOrderLevelDiscount(eq(cartRAO), eq(true), captor.capture());
        assertEquals(3000d, captor.getValue().doubleValue(), 0);
    }

    @Test
    public void performAction_FirstActualDiscountLargerThanMaximumDiscount() {
        when(promotionBudgetConsumeService.calculateConsumeBudgetAmount(any(ConsumeBudgetParam.class))).thenReturn(BigDecimal.valueOf(0));
        when(cartRAO.getTotal()).thenReturn(new BigDecimal(3000000d));
        action.performAction(context);
        verify(this.ruleEngineCalculationService)
                .addOrderLevelDiscount(eq(cartRAO), eq(true), captor.capture());
        assertEquals(100000d, captor.getValue().doubleValue(), 0);
    }

    @Test
    public void performAction_FirstActualDiscountLargerThanMaximumDiscount_case2() {
        when(promotionBudgetConsumeService.calculateConsumeBudgetAmount(any(ConsumeBudgetParam.class))).thenReturn(BigDecimal.valueOf(0));
        when(cartRAO.getTotal()).thenReturn(new BigDecimal(150000d));
        action.performAction(context);
        verify(this.ruleEngineCalculationService)
                .addOrderLevelDiscount(eq(cartRAO), eq(true), captor.capture());
        assertEquals(15000d, captor.getValue().doubleValue(), 0);
    }
}
