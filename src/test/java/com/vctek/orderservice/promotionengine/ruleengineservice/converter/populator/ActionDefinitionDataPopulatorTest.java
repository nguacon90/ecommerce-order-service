package com.vctek.orderservice.promotionengine.ruleengineservice.converter.populator;

import com.vctek.converter.Converter;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionDefinitionCategoryData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleActionDefinitionCategoryModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleActionDefinitionModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleActionDefinitionParameterModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.repository.RuleActionDefinitionCategoryRepository;
import com.vctek.orderservice.promotionengine.ruleengineservice.repository.RuleActionDefinitionParameterRepository;
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

public class ActionDefinitionDataPopulatorTest extends AbstractDefinitionPopulatorMock {
    @Mock
    private Converter<RuleActionDefinitionCategoryModel, RuleActionDefinitionCategoryData> actionDefinitionCategoryConverter;
    @Mock
    private Converter<RuleActionDefinitionParameterModel, RuleParameterDefinitionData> actionDefinitionParameterConverter;
    @Mock
    private RuleActionDefinitionModel source;

    @Mock
    private RuleActionDefinitionCategoryRepository ruleActionDefinitionCategoryRepository;
    @Mock
    private RuleActionDefinitionParameterRepository ruleActionDefinitionParameterRepository;

    private ActionDefinitionDataPopulator populator;
    private List<RuleActionDefinitionCategoryModel> categories = new ArrayList<>();
    private RuleActionDefinitionData target;
    private List<RuleActionDefinitionParameterModel> parameters = new ArrayList<>();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        target = new RuleActionDefinitionData();
        populator = new ActionDefinitionDataPopulator(ruleParameterValueConverter, actionDefinitionCategoryConverter,
                actionDefinitionParameterConverter);
        populator.setRuleActionDefinitionCategoryRepository(ruleActionDefinitionCategoryRepository);
        populator.setRuleActionDefinitionParameterRepository(ruleActionDefinitionParameterRepository);
        when(ruleActionDefinitionCategoryRepository.findAllByActionDefinitions(source)).thenReturn(categories);
        when(ruleActionDefinitionParameterRepository.findAllByActionDefinition(source)).thenReturn(parameters);
    }

    @Test
    public void populate() {
        categories.add(new RuleActionDefinitionCategoryModel());
        parameters.add(new RuleActionDefinitionParameterModel());
        when(source.getId()).thenReturn(2l);

        populator.populate(source, target);
        assertEquals(2l, target.getId(), 0);
        verify(actionDefinitionCategoryConverter).convertAll(categories);
        verify(actionDefinitionParameterConverter).convert(any(RuleActionDefinitionParameterModel.class));
    }
}
