package com.vctek.orderservice.promotionengine.ruleengineservice.service.impl;

import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.strategy.RuleParameterUuidGenerator;
import com.vctek.orderservice.promotionengine.ruleengineservice.strategy.RuleParametersConverter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

public class DefaultRuleParametersServiceTest {
    @Mock
    private RuleParametersConverter ruleParametersConverter;
    @Mock
    private RuleParameterUuidGenerator ruleParameterUuidGenerator;
    private DefaultRuleParametersService service;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new DefaultRuleParametersService(ruleParametersConverter, ruleParameterUuidGenerator);
    }

    @Test
    public void createParameterFromDefinition() {
        RuleParameterDefinitionData definition = new RuleParameterDefinitionData();
        service.createParameterFromDefinition(definition);
        verify(ruleParameterUuidGenerator).generateUuid(any(RuleParameterData.class), eq(definition));
    }
}
