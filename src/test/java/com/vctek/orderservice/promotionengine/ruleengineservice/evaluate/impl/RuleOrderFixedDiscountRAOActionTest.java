package com.vctek.orderservice.promotionengine.ruleengineservice.evaluate.impl;

import com.vctek.orderservice.promotionengine.ruleengine.model.eveluation.RuleActionContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.RuleEngineCalculationService;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.CartRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.OrderEntryRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.RuleEngineResultRAO;
import com.vctek.orderservice.promotionengine.util.CurrencyIsoCode;
import org.drools.core.WorkingMemory;
import org.drools.core.spi.KnowledgeHelper;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.rule.FactHandle;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RuleOrderFixedDiscountRAOActionTest {
    private RuleOrderFixedDiscountRAOAction action;

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
    private Map<String, BigDecimal> mapDiscount = new HashMap<>();
    private Set<OrderEntryRAO> entries = new HashSet<>();
    private RuleEngineResultRAO ruleEngineResultRAO = new RuleEngineResultRAO();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        action = new RuleOrderFixedDiscountRAOAction();
        action.setValidateRuleCode(true);
        action.setValidateRulesModuleName(true);
        action.setRuleEngineCalculationService(ruleEngineCalculationService);
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
        when(cartRAO.getEntries()).thenReturn(entries);
        entries.add(new OrderEntryRAO());
        ruleEngineResultRAO.setActions(new LinkedHashSet<>());
        when(context.getRuleEngineResultRao()).thenReturn(ruleEngineResultRAO);
    }

    @Test
    public void performAction() {
        action.performAction(context);
        verify(this.ruleEngineCalculationService)
                .addOrderLevelDiscount(eq(cartRAO), eq(true), any(BigDecimal.class));
    }
}
