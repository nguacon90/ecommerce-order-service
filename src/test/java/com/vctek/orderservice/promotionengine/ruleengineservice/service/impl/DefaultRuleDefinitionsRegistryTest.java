package com.vctek.orderservice.promotionengine.ruleengineservice.service.impl;

import com.vctek.converter.Converter;
import com.vctek.orderservice.promotionengine.ruleengine.enums.RuleType;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleConditionDefinitionModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleConditionDefinitionService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultRuleDefinitionsRegistryTest {

    public static final String CODE = "definition1";
    private DefaultRuleConditionsRegistry registry;

    @Mock
    private Converter<RuleConditionDefinitionModel, RuleConditionDefinitionData> converter;

    @Mock
    private RuleConditionDefinitionService service;

    private RuleConditionDefinitionModel model = new RuleConditionDefinitionModel();
    private RuleConditionDefinitionData data = new RuleConditionDefinitionData();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        registry = new DefaultRuleConditionsRegistry(service, converter);
    }

    @Test
    public void getAllActionDefinitions() {
        when(service.getAllRuleConditionDefinitions()).thenReturn(Arrays.asList(model));
        registry.getAllConditionDefinitions();
        verify(converter).convertAll(anyList());
    }

    @Test
    public void getAllActionDefinitionsAsMap() {
        model.setCode(CODE);
        data.setCode(CODE);
        when(service.getAllRuleConditionDefinitions()).thenReturn(Arrays.asList(model));
        when(converter.convertAll(anyList())).thenReturn(Arrays.asList(data));

        Map<String, RuleConditionDefinitionData> map = registry.getAllConditionDefinitionsAsMap();
        assertEquals(CODE, map.get(CODE).getCode());
    }

    @Test
    public void getActionDefinitionsForRuleTypeAsMap() {
        model.setCode(CODE);
        data.setCode(CODE);
        when(service.getRuleConditionDefinitionsForRuleType(RuleType.PROMOTION)).thenReturn(Arrays.asList(model));
        when(converter.convertAll(anyList())).thenReturn(Arrays.asList(data));

        Map<String, RuleConditionDefinitionData> map = registry.getConditionDefinitionsForRuleTypeAsMap(RuleType.PROMOTION);
        assertEquals(CODE, map.get(CODE).getCode());
    }

    @Test
    public void getActionDefinitionsForRuleType() {
        when(service.getRuleConditionDefinitionsForRuleType(RuleType.PROMOTION)).thenReturn(Arrays.asList(model));

        registry.getConditionDefinitionsForRuleType(RuleType.PROMOTION);
        verify(converter).convertAll(anyList());
    }
}
