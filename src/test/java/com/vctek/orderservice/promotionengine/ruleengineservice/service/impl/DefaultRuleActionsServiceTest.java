package com.vctek.orderservice.promotionengine.ruleengineservice.service.impl;

import com.vctek.orderservice.promotionengine.ruleengineservice.converter.RuleActionsConverter;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleParametersService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultRuleActionsServiceTest {
    @Mock
    private RuleParametersService ruleParametersService;
    @Mock
    private RuleActionsConverter ruleActionsConverter;

    private DefaultRuleActionsService service;

    @Mock
    private RuleActionDefinitionData data;
    private Map<String, RuleParameterDefinitionData> parameters;
    private RuleParameterData param = new RuleParameterData();
    private Map<String, RuleActionDefinitionData> actionDefinitions = new HashMap<>();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        parameters = new HashMap<>();
        parameters.put("param1", new RuleParameterDefinitionData());
        when(ruleParametersService.createParameterFromDefinition(any(RuleParameterDefinitionData.class))).thenReturn(param);
        service = new DefaultRuleActionsService(ruleActionsConverter, ruleParametersService);
    }

    @Test
    public void createConditionFromDefinition() {
        when(data.getParameters()).thenReturn(parameters);
        RuleActionData condition = service.createActionFromDefinition(data);
        assertNotNull(condition.getParameters());
        assertTrue(condition.getParameters().containsKey("param1"));
    }

    @Test
    public void convertConditionsFromString() {
        service.convertActionsFromString("condition", actionDefinitions);
        verify(ruleActionsConverter).fromString(anyString(), anyMap());
    }
}
