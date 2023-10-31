package com.vctek.orderservice.promotionengine.ruleengineservice.converter.populator;

import com.vctek.converter.Converter;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionCategoryData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleConditionDefinitionCategoryModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleConditionDefinitionModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleConditionDefinitionParameterModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.repository.RuleConditionDefinitionCategoryRepository;
import com.vctek.orderservice.promotionengine.ruleengineservice.repository.RuleConditionDefinitionParameterRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RuleConditionDefinitionDataPopulatorTest extends AbstractDefinitionPopulatorMock {

    @Mock
    private RuleConditionDefinitionCategoryRepository ruleConditionDefinitionCategoryRepository;
    @Mock
    private RuleConditionDefinitionParameterRepository ruleConditionDefinitionParameterRepository;
    @Mock
    private Converter<RuleConditionDefinitionCategoryModel, RuleConditionDefinitionCategoryData> conditionCategoryConverter;
    @Mock
    private Converter<RuleConditionDefinitionParameterModel, RuleParameterDefinitionData> ruleConditionDefinitionParameterConverter;

    private RuleConditionDefinitionDataPopulator populator;
    @Mock
    private RuleConditionDefinitionModel source;

    private List<RuleConditionDefinitionCategoryModel> categories = new ArrayList<>();
    private RuleConditionDefinitionData target;
    private List<RuleConditionDefinitionParameterModel> parameters = new ArrayList<>();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        target = new RuleConditionDefinitionData();
        populator = new RuleConditionDefinitionDataPopulator(ruleParameterValueConverter, conditionCategoryConverter,
                ruleConditionDefinitionParameterConverter);
        populator.setRuleConditionDefinitionCategoryRepository(ruleConditionDefinitionCategoryRepository);
        populator.setRuleConditionDefinitionParameterRepository(ruleConditionDefinitionParameterRepository);
        when(ruleConditionDefinitionCategoryRepository.findAllByConditionDefinitions(source)).thenReturn(categories);
        when(ruleConditionDefinitionParameterRepository.findAllByConditionDefinition(source)).thenReturn(parameters);
    }

    @Test
    public void populate() {
        categories.add(new RuleConditionDefinitionCategoryModel());
        parameters.add(new RuleConditionDefinitionParameterModel());
        when(source.getId()).thenReturn(2l);

        populator.populate(source, target);
        assertEquals(2l, target.getId(), 0);
        verify(conditionCategoryConverter).convertAll(categories);
        verify(ruleConditionDefinitionParameterConverter).convert(any(RuleConditionDefinitionParameterModel.class));
    }
}
