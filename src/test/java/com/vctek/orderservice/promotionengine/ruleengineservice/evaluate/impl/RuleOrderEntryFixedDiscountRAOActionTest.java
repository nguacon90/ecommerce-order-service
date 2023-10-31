package com.vctek.orderservice.promotionengine.ruleengineservice.evaluate.impl;

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

import static com.vctek.orderservice.promotionengine.ruleengineservice.evaluate.impl.AbstractRuleExecutableSupport.MAXIMUM_QUANTITY;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class RuleOrderEntryFixedDiscountRAOActionTest {
    private RuleOrderEntryFixedDiscountRAOAction action;

    @Mock
    private RuleEngineCalculationService ruleEngineCalculationService;;
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
    @Mock
    private OrderEntryRAO orderEntryRAO;
    @Mock
    private OrderEntryRAO orderEntryRAOMock2;
    @Mock
    private OrderEntryRAO orderEntryRAOMock3;

    private ArgumentCaptor<Integer> consumableQuantityEntryCaptor;
    private ArgumentCaptor<Integer> consumableQuantityEntry2Captor;
    private ArgumentCaptor<Integer> consumableQuantityEntry3Captor;
    private Map<String, BigDecimal> mapDiscount = new HashMap<>();
    private Set<OrderEntryRAO> entries = new HashSet<>();
    private RuleEngineResultRAO ruleEngineResultRAO = new RuleEngineResultRAO();
    @Mock
    private CouponRAO couponRAOMock;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        consumableQuantityEntryCaptor = ArgumentCaptor.forClass(Integer.class);
        consumableQuantityEntry2Captor = ArgumentCaptor.forClass(Integer.class);
        consumableQuantityEntry3Captor = ArgumentCaptor.forClass(Integer.class);
        action = new RuleOrderEntryFixedDiscountRAOAction();
        action.setValidateRuleCode(true);
        action.setValidateRulesModuleName(true);
        action.setRuleEngineCalculationService(ruleEngineCalculationService);
        when(context.getValues(OrderEntryRAO.class)).thenReturn(entries);
        when(context.getParameters()).thenReturn(params);
        when(context.getRuleMetadata()).thenReturn(metaData);
        when(metaData.get(AbstractRuleExecutableSupport.RULE_CODE)).thenReturn("ruleCode");
        when(metaData.get(AbstractRuleExecutableSupport.MODULE_NAME)).thenReturn("promotion-module");
        when(context.getDelegate()).thenReturn(knowledgeHelper);
        when(knowledgeHelper.getWorkingMemory()).thenReturn(workingMemmory);
        when(workingMemmory.getFactHandles()).thenReturn(facts);
        when(context.getCartRao()).thenReturn(cartRAO);
        when(cartRAO.getCurrencyIsoCode()).thenReturn(CurrencyIsoCode.VND.toString());
        mapDiscount.put(CurrencyIsoCode.VND.toString(), new BigDecimal(30000));
        when(context.getParameter("value")).thenReturn(mapDiscount);
        when(ruleEngineCalculationService.getConsumedQuantityForOrderEntry(orderEntryRAO)).thenReturn(0);
        when(cartRAO.getEntries()).thenReturn(entries);
        when(this.ruleEngineCalculationService.addOrderEntryLevelDiscount(any(), eq(true), any(BigDecimal.class))).thenReturn(new DiscountRAO());
        when(this.ruleEngineCalculationService.getAdjustedUnitPrice(anyInt(), any())).thenReturn(new BigDecimal(10000));
        when(this.ruleEngineCalculationService.addOrderEntryLevelDiscountWithConsumableQty(any(), eq(true), any(), anyInt()))
                .thenReturn(new DiscountRAO());

        ruleEngineResultRAO.setActions(new LinkedHashSet<>());
        when(context.getRuleEngineResultRao()).thenReturn(ruleEngineResultRAO);
    }

    @Test
    public void performAction_withNotConfigMaximumQuantityToApplied() {
        when(orderEntryRAO.getQuantity()).thenReturn(1);
        when(orderEntryRAO.getBasePrice()).thenReturn(new BigDecimal(200000));
        entries.add(orderEntryRAO);
        action.performAction(context);
        verify(this.ruleEngineCalculationService)
                .addOrderEntryLevelDiscount(eq(orderEntryRAO), eq(true), any(BigDecimal.class));
    }

    @Test
    public void performAction_withConfigMaximumQuantityToAppliedIsOne_HasNotCoupon() {
        when(context.getParameter(MAXIMUM_QUANTITY)).thenReturn(1l);
        when(orderEntryRAO.getQuantity()).thenReturn(1);
        when(orderEntryRAO.getBasePrice()).thenReturn(new BigDecimal(200000));
        when(orderEntryRAOMock2.getQuantity()).thenReturn(2);
        when(orderEntryRAOMock2.getBasePrice()).thenReturn(new BigDecimal(340000));
        when(ruleEngineCalculationService.getConsumedQuantityForOrderEntry(orderEntryRAO)).thenReturn(0);
        when(ruleEngineCalculationService.getConsumedQuantityForOrderEntry(orderEntryRAOMock2)).thenReturn(0);
        entries.add(orderEntryRAO);
        entries.add(orderEntryRAOMock2);

        action.performAction(context);

        verify(this.ruleEngineCalculationService).addOrderEntryLevelDiscountWithConsumableQty(eq(orderEntryRAO),
                eq(true), any(), consumableQuantityEntryCaptor.capture());
        assertEquals(1, consumableQuantityEntryCaptor.getValue(), 0);

        verify(this.ruleEngineCalculationService, times(1)).addOrderEntryLevelDiscountWithConsumableQty(eq(orderEntryRAOMock2),
                eq(true), any(), consumableQuantityEntry2Captor.capture());
        assertEquals(1, consumableQuantityEntry2Captor.getAllValues().get(0), 0);

    }

    @Test
    public void performAction_withConfigMaximumQuantityToAppliedIs4_SubtotalQtyIsSmallThan4_HasNotCoupon() {
        when(context.getParameter(MAXIMUM_QUANTITY)).thenReturn(4l);
        when(orderEntryRAO.getQuantity()).thenReturn(1);
        when(orderEntryRAO.getBasePrice()).thenReturn(new BigDecimal(200000));
        when(orderEntryRAOMock2.getQuantity()).thenReturn(2);
        when(orderEntryRAOMock2.getBasePrice()).thenReturn(new BigDecimal(340000));
        when(ruleEngineCalculationService.getConsumedQuantityForOrderEntry(orderEntryRAO)).thenReturn(0);
        when(ruleEngineCalculationService.getConsumedQuantityForOrderEntry(orderEntryRAOMock2)).thenReturn(0);
        entries.add(orderEntryRAO);
        entries.add(orderEntryRAOMock2);

        action.performAction(context);

        verify(this.ruleEngineCalculationService).addOrderEntryLevelDiscountWithConsumableQty(eq(orderEntryRAO),
                eq(true), any(), consumableQuantityEntryCaptor.capture());
        assertEquals(1, consumableQuantityEntryCaptor.getValue(), 0);

        verify(this.ruleEngineCalculationService).addOrderEntryLevelDiscountWithConsumableQty(eq(orderEntryRAOMock2),
                eq(true), any(), consumableQuantityEntry2Captor.capture());
        assertEquals(2, consumableQuantityEntry2Captor.getValue(), 0);

    }

    @Test
    public void performAction_MaximumQuantity2_HadBeenConsumedBefore_HasNotCoupon() {
        when(context.getParameter(MAXIMUM_QUANTITY)).thenReturn(4l);
        when(orderEntryRAO.getQuantity()).thenReturn(1);
        when(orderEntryRAO.getBasePrice()).thenReturn(new BigDecimal(200000));
        when(orderEntryRAOMock2.getQuantity()).thenReturn(2);
        when(orderEntryRAOMock2.getBasePrice()).thenReturn(new BigDecimal(340000));
        when(ruleEngineCalculationService.getConsumedQuantityForOrderEntry(orderEntryRAO)).thenReturn(1);
        when(ruleEngineCalculationService.getConsumedQuantityForOrderEntry(orderEntryRAOMock2)).thenReturn(1);
        entries.add(orderEntryRAO);
        entries.add(orderEntryRAOMock2);

        action.performAction(context);

        verify(this.ruleEngineCalculationService).addOrderEntryLevelDiscountWithConsumableQty(eq(orderEntryRAO),
                eq(true), any(), consumableQuantityEntryCaptor.capture());
        assertEquals(0, consumableQuantityEntryCaptor.getValue(), 0);

        verify(this.ruleEngineCalculationService).addOrderEntryLevelDiscountWithConsumableQty(eq(orderEntryRAOMock2),
                eq(true), any(), consumableQuantityEntry2Captor.capture());
        assertEquals(1, consumableQuantityEntry2Captor.getValue(), 0);

    }

    @Test
    public void performAction_MaximumQuantity2_HadBeenConsumedAllBefore_HasNotCoupon() {
        when(context.getParameter(MAXIMUM_QUANTITY)).thenReturn(4l);
        when(orderEntryRAO.getQuantity()).thenReturn(1);
        when(orderEntryRAO.getBasePrice()).thenReturn(new BigDecimal(200000));
        when(orderEntryRAOMock2.getQuantity()).thenReturn(2);
        when(orderEntryRAOMock2.getBasePrice()).thenReturn(new BigDecimal(340000));
        when(ruleEngineCalculationService.getConsumedQuantityForOrderEntry(orderEntryRAO)).thenReturn(1);
        when(ruleEngineCalculationService.getConsumedQuantityForOrderEntry(orderEntryRAOMock2)).thenReturn(2);
        entries.add(orderEntryRAO);
        entries.add(orderEntryRAOMock2);

        action.performAction(context);

        verify(this.ruleEngineCalculationService).addOrderEntryLevelDiscountWithConsumableQty(eq(orderEntryRAO),
                eq(true), any(), consumableQuantityEntryCaptor.capture());
        assertEquals(0, consumableQuantityEntryCaptor.getValue(), 0);

        verify(this.ruleEngineCalculationService).addOrderEntryLevelDiscountWithConsumableQty(eq(orderEntryRAOMock2),
                eq(true), any(), consumableQuantityEntry2Captor.capture());
        assertEquals(0, consumableQuantityEntry2Captor.getValue(), 0);

    }

    @Test
    public void performAction_MaximumQuantity1_With3Entries_2EntriesConsumedAll_HasNotCoupon() {
        when(context.getParameter(MAXIMUM_QUANTITY)).thenReturn(1l);
        when(orderEntryRAO.getQuantity()).thenReturn(1);
        when(orderEntryRAO.getBasePrice()).thenReturn(new BigDecimal(200000));
        when(orderEntryRAOMock2.getQuantity()).thenReturn(2);
        when(orderEntryRAOMock2.getBasePrice()).thenReturn(new BigDecimal(340000));
        when(orderEntryRAOMock3.getQuantity()).thenReturn(2);
        when(orderEntryRAOMock3.getBasePrice()).thenReturn(new BigDecimal(240000));
        when(ruleEngineCalculationService.getConsumedQuantityForOrderEntry(orderEntryRAO)).thenReturn(1);
        when(ruleEngineCalculationService.getConsumedQuantityForOrderEntry(orderEntryRAOMock2)).thenReturn(2);
        when(ruleEngineCalculationService.getConsumedQuantityForOrderEntry(orderEntryRAOMock3)).thenReturn(0);
        entries.add(orderEntryRAO);
        entries.add(orderEntryRAOMock2);
        entries.add(orderEntryRAOMock3);

        action.performAction(context);

        verify(this.ruleEngineCalculationService).addOrderEntryLevelDiscountWithConsumableQty(eq(orderEntryRAO),
                eq(true), any(), consumableQuantityEntryCaptor.capture());
        assertEquals(0, consumableQuantityEntryCaptor.getValue(), 0);

        verify(this.ruleEngineCalculationService).addOrderEntryLevelDiscountWithConsumableQty(eq(orderEntryRAOMock2),
                eq(true), any(), consumableQuantityEntry2Captor.capture());
        assertEquals(0, consumableQuantityEntry2Captor.getValue(), 0);

        verify(this.ruleEngineCalculationService, times(1)).addOrderEntryLevelDiscountWithConsumableQty(eq(orderEntryRAOMock3),
                eq(true), any(), consumableQuantityEntry3Captor.capture());
        assertEquals(1, consumableQuantityEntry3Captor.getAllValues().get(0), 0);

    }

    @Test
    public void performAction_MaximumQuantity1_With3Entries_2EntriesConsumedAll_CouponConfigIs4() {
        when(context.getParameter(MAXIMUM_QUANTITY)).thenReturn(1l);
        when(orderEntryRAO.getQuantity()).thenReturn(1);
        when(orderEntryRAO.getBasePrice()).thenReturn(new BigDecimal(200000));
        when(orderEntryRAOMock2.getQuantity()).thenReturn(2);
        when(orderEntryRAOMock2.getBasePrice()).thenReturn(new BigDecimal(340000));
        when(orderEntryRAOMock3.getQuantity()).thenReturn(2);
        when(orderEntryRAOMock3.getBasePrice()).thenReturn(new BigDecimal(240000));
        when(ruleEngineCalculationService.getConsumedQuantityForOrderEntry(orderEntryRAO)).thenReturn(1);
        when(ruleEngineCalculationService.getConsumedQuantityForOrderEntry(orderEntryRAOMock2)).thenReturn(2);
        when(ruleEngineCalculationService.getConsumedQuantityForOrderEntry(orderEntryRAOMock3)).thenReturn(0);
        entries.add(orderEntryRAO);
        entries.add(orderEntryRAOMock2);
        entries.add(orderEntryRAOMock3);
        when(context.getValues(CouponRAO.class)).thenReturn(new HashSet<>(Arrays.asList(couponRAOMock)));
        when(couponRAOMock.getTotalRedemption()).thenReturn(4);
        action.performAction(context);

        verify(this.ruleEngineCalculationService).addOrderEntryLevelDiscountWithConsumableQty(eq(orderEntryRAO),
                eq(true), any(), consumableQuantityEntryCaptor.capture());
        assertEquals(0, consumableQuantityEntryCaptor.getValue(), 0);

        verify(this.ruleEngineCalculationService).addOrderEntryLevelDiscountWithConsumableQty(eq(orderEntryRAOMock2),
                eq(true), any(), consumableQuantityEntry2Captor.capture());
        assertEquals(0, consumableQuantityEntry2Captor.getValue(), 0);

        verify(this.ruleEngineCalculationService).addOrderEntryLevelDiscountWithConsumableQty(eq(orderEntryRAOMock3),
                eq(true), any(), consumableQuantityEntry3Captor.capture());
        assertEquals(1, consumableQuantityEntry3Captor.getValue(), 0);

    }
}
