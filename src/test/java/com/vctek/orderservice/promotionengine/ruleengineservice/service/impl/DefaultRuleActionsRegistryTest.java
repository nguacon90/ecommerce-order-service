package com.vctek.orderservice.promotionengine.ruleengineservice.service.impl;

import com.vctek.converter.Converter;
import com.vctek.orderservice.promotionengine.ruleengine.enums.RuleType;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleActionDefinitionModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleActionDefinitionService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultRuleActionsRegistryTest {

    public static final String CODE = "action1";
    private DefaultRuleActionsRegistry registry;
    @Mock
    private Converter<RuleActionDefinitionModel, RuleActionDefinitionData> converter;
    @Mock
    private RuleActionDefinitionService service;

    private RuleActionDefinitionModel model = new RuleActionDefinitionModel();
    private RuleActionDefinitionData data = new RuleActionDefinitionData();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        registry = new DefaultRuleActionsRegistry();
        registry.setRuleActionDefinitionConverter(converter);
        registry.setRuleActionDefinitionService(service);
    }

    @Test
    public void getAllActionDefinitions() {
        when(service.getAllRuleActionDefinitions()).thenReturn(Arrays.asList(model));
        registry.getAllActionDefinitions();
        verify(converter).convert(any(RuleActionDefinitionModel.class));
    }

    @Test
    public void getAllActionDefinitionsAsMap() {
        model.setCode(CODE);
        data.setCode(CODE);
        when(service.getAllRuleActionDefinitions()).thenReturn(Arrays.asList(model));
        when(converter.convert(model)).thenReturn(data);

        Map<String, RuleActionDefinitionData> map = registry.getAllActionDefinitionsAsMap();
        assertEquals(CODE, map.get(CODE).getCode());
    }

    @Test
    public void getActionDefinitionsForRuleTypeAsMap() {
        model.setCode(CODE);
        data.setCode(CODE);
        when(service.getRuleActionDefinitionsForRuleType(RuleType.PROMOTION)).thenReturn(Arrays.asList(model));
        when(converter.convert(model)).thenReturn(data);

        Map<String, RuleActionDefinitionData> map = registry.getActionDefinitionsForRuleTypeAsMap(RuleType.PROMOTION);
        assertEquals(CODE, map.get(CODE).getCode());
    }

    @Test
    public void getActionDefinitionsForRuleType() {
        when(service.getRuleActionDefinitionsForRuleType(RuleType.PROMOTION)).thenReturn(Arrays.asList(model));

        registry.getActionDefinitionsForRuleType(RuleType.PROMOTION);
        verify(converter).convert(any(RuleActionDefinitionModel.class));
    }
}
