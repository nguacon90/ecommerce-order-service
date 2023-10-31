package com.vctek.orderservice.converter.promotion;

import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.enums.RuleType;
import com.vctek.orderservice.promotionengine.ruleengineservice.converter.RuleActionsConverter;
import com.vctek.orderservice.promotionengine.ruleengineservice.converter.RuleConditionsConverter;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.PromotionSourceRuleData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleActionsRegistry;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleConditionsRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PromotionSourceRulePopulatorTest {
    private PromotionSourceRulePopulator populator;

    @Mock
    private RuleConditionsConverter ruleConditionsConverter;
    @Mock
    private RuleActionsConverter ruleActionsConverter;
    @Mock
    private RuleConditionsRegistry ruleConditionsRegistry;
    @Mock
    private RuleActionsRegistry ruleActionsRegistry;
    private PromotionSourceRuleData source = new PromotionSourceRuleData();
    private PromotionSourceRuleModel target = new PromotionSourceRuleModel();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        populator = new PromotionSourceRulePopulator();
        populator.setRuleActionsConverter(ruleActionsConverter);
        populator.setRuleActionsRegistry(ruleActionsRegistry);
        populator.setRuleConditionsConverter(ruleConditionsConverter);
        populator.setRuleConditionsRegistry(ruleConditionsRegistry);
        when(ruleConditionsRegistry.getConditionDefinitionsForRuleTypeAsMap(RuleType.PROMOTION)).thenReturn(new HashMap<>());
        when(ruleActionsRegistry.getActionDefinitionsForRuleTypeAsMap(RuleType.PROMOTION)).thenReturn(new HashMap<>());
    }

    @Test
    public void populate() {
        source.setCode("code");
        source.setConditions(Arrays.asList(new RuleConditionData()));
        source.setActions(Arrays.asList(new RuleActionData()));

        populator.populate(source, target);
        verify(ruleConditionsConverter).toString(anyList(), anyMap());
        verify(ruleActionsConverter).toString(anyList(), anyMap());
    }
}
