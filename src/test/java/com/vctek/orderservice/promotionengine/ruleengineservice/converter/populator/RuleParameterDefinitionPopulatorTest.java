package com.vctek.orderservice.promotionengine.ruleengineservice.converter.populator;

import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleConditionDefinitionParameterModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class RuleParameterDefinitionPopulatorTest extends AbstractDefinitionPopulatorMock {
    private RuleParameterDefinitionPopulator populator;

    @Mock
    private RuleConditionDefinitionParameterModel source;
    private RuleParameterDefinitionData target = new RuleParameterDefinitionData();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        populator = new RuleParameterDefinitionPopulator(ruleParameterValueConverter);
    }

    @Test
    public void populate() {
        populator.populate(source, target);
        verify(source, times(2)).getType();
    }
}
