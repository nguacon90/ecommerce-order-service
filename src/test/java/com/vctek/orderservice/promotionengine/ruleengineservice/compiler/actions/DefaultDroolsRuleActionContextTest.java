package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.actions;

import com.vctek.orderservice.promotionengine.ruleengineservice.rao.CartRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.DiscountRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.RuleEngineResultRAO;
import org.apache.commons.lang3.StringUtils;
import org.drools.core.definitions.rule.impl.RuleImpl;
import org.drools.core.spi.KnowledgeHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DefaultDroolsRuleActionContextTest {

    @Mock
    private Map<String, Object> variables;

    @Mock
    private KnowledgeHelper helper;

    private DefaultDroolsRuleActionContext context;

    private Map<String, Object> parameters = new HashMap<>();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        context = new DefaultDroolsRuleActionContext(variables, helper);
        context.setParameters(parameters);
    }

    @Test
    public void getCartRao() {
        when(variables.get(CartRAO.class.getTypeName())).thenReturn(new CartRAO());
        CartRAO cartRao = context.getCartRao();
        assertNotNull(cartRao);
    }

    @Test
    public void getRuleEngineResultRao() {
        when(variables.get(RuleEngineResultRAO.class.getTypeName())).thenReturn(new RuleEngineResultRAO());
        RuleEngineResultRAO ruleEngineResultRAO = context.getRuleEngineResultRao();
        assertNotNull(ruleEngineResultRAO);
    }

    @Test
    public void updateScheduledFacts() {
        context.scheduleForUpdate(new Object[]{new CartRAO(), new RuleEngineResultRAO()});

        context.updateScheduledFacts();
        verify(helper, times(2)).update(any(Object.class));
    }

    @Test
    public void insertFacts() {
        context.insertFacts(new Object[]{new DiscountRAO()});
        verify(helper, times(1)).insert(any(Object.class));
    }

    @Test
    public void getRulesModuleName_Null() {
        RuleImpl rule = mock(RuleImpl.class);
        when(rule.getMetaData()).thenReturn(new HashMap<>());
        when(helper.getRule()).thenReturn(rule);
        Optional<String> optional = context.getRulesModuleName();
        assertFalse(optional.isPresent());
    }

    @Test
    public void getRulesModuleName() {
        RuleImpl rule = mock(RuleImpl.class);
        HashMap<String, Object> values = new HashMap<>();
        values.put("moduleName", "promotion-module");
        when(rule.getMetaData()).thenReturn(values);
        when(helper.getRule()).thenReturn(rule);
        Optional<String> optional = context.getRulesModuleName();
        assertTrue(optional.isPresent());
    }

    @Test
    public void getRuleName() {
        RuleImpl rule = mock(RuleImpl.class);
        when(rule.getName()).thenReturn("fix_order_discount");
        when(helper.getRule()).thenReturn(rule);

        String ruleName = context.getRuleName();
        assertEquals("fix_order_discount", ruleName);
    }

    @Test
    public void getValue_Set() {
        Set<DiscountRAO> discounts = new HashSet<>();
        discounts.add(new DiscountRAO());
        when(variables.get(DiscountRAO.class.getTypeName())).thenReturn(discounts);
        DiscountRAO value = context.getValue(DiscountRAO.class);
        assertNotNull(value);
    }

    @Test
    public void getValue_List() {
        List<DiscountRAO> discounts = new ArrayList<>();
        discounts.add(new DiscountRAO());
        String key = "/" + DiscountRAO.class.getTypeName();
        when(variables.get(key)).thenReturn(discounts);
        DiscountRAO value = context.getValue(DiscountRAO.class, StringUtils.EMPTY);
        assertNotNull(value);
    }

}
