package com.vctek.orderservice.promotionengine.ruleengineservice.converter.populator;

import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionDefinitionCategoryData;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleActionDefinitionCategoryModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class ActionConditionCategoryPopulatorTest {
    private ActionDefinitionCategoryPopulator populator;
    @Mock
    private RuleActionDefinitionCategoryModel source;
    private RuleActionDefinitionCategoryData target;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        target = new RuleActionDefinitionCategoryData();
        populator = new ActionDefinitionCategoryPopulator();
    }

    @Test
    public void populate() {
        when(source.getCode()).thenReturn("cat_code");
        populator.populate(source, target);
        assertEquals("cat_code", target.getCode());
    }
}
