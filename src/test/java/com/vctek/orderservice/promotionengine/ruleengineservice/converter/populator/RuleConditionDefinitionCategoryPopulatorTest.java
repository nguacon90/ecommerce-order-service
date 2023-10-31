package com.vctek.orderservice.promotionengine.ruleengineservice.converter.populator;

import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionCategoryData;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleConditionDefinitionCategoryModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class RuleConditionDefinitionCategoryPopulatorTest {
    private RuleConditionDefinitionCategoryPopulator populator;
    @Mock
    private RuleConditionDefinitionCategoryModel source;
    private RuleConditionDefinitionCategoryData target;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        target = new RuleConditionDefinitionCategoryData();
        populator = new RuleConditionDefinitionCategoryPopulator();
    }

    @Test
    public void populate() {
        when(source.getCode()).thenReturn("cat_code");
        populator.populate(source, target);
        assertEquals("cat_code", target.getCode());
    }
}
