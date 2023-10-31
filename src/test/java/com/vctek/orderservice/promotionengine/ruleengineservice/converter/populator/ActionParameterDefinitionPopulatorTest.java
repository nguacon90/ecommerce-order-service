package com.vctek.orderservice.promotionengine.ruleengineservice.converter.populator;

import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleActionDefinitionParameterModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;

public class ActionParameterDefinitionPopulatorTest extends AbstractDefinitionPopulatorMock {
    private ActionParameterDefinitionPopulator populator;

    @Mock
    private RuleActionDefinitionParameterModel source;
    private RuleParameterDefinitionData target = new RuleParameterDefinitionData();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        populator = new ActionParameterDefinitionPopulator(ruleParameterValueConverter);
    }

    @Test
    public void populate() {
        populator.populate(source, target);
        verify(source).getName();
    }
}
