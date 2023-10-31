package com.vctek.orderservice.promotionengine.ruleengineservice.converter.populator;

import com.vctek.converter.Converter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionDefinitionCategoryData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleActionDefinitionCategoryModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleActionDefinitionModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleActionDefinitionParameterModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.repository.RuleActionDefinitionCategoryRepository;
import com.vctek.orderservice.promotionengine.ruleengineservice.repository.RuleActionDefinitionParameterRepository;
import com.vctek.orderservice.promotionengine.ruleengineservice.strategy.RuleParameterValueConverter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ActionDefinitionDataPopulator extends AbstractDefinitionPopulator
        implements Populator<RuleActionDefinitionModel, RuleActionDefinitionData> {
    private RuleActionDefinitionCategoryRepository ruleActionDefinitionCategoryRepository;
    private RuleActionDefinitionParameterRepository ruleActionDefinitionParameterRepository;
    private Converter<RuleActionDefinitionCategoryModel, RuleActionDefinitionCategoryData> actionDefinitionCategoryConverter;
    private Converter<RuleActionDefinitionParameterModel, RuleParameterDefinitionData> actionDefinitionParameterConverter;

    public ActionDefinitionDataPopulator(RuleParameterValueConverter ruleParameterValueConverter,
            Converter<RuleActionDefinitionCategoryModel, RuleActionDefinitionCategoryData> actionDefinitionCategoryConverter,
            Converter<RuleActionDefinitionParameterModel, RuleParameterDefinitionData> actionDefinitionParameterConverter) {
        super(ruleParameterValueConverter);
        this.actionDefinitionCategoryConverter = actionDefinitionCategoryConverter;
        this.actionDefinitionParameterConverter = actionDefinitionParameterConverter;
    }

    @Override
    public void populate(RuleActionDefinitionModel source, RuleActionDefinitionData target) {
        target.setId(source.getId());
        target.setCode(source.getCode());
        target.setPriority(source.getPriority());
        target.setName(source.getName());
        target.setTranslatorId(source.getTranslatorId());
        populateTranslatorParameters(source, target);
        populateCategories(source, target);
        populateConditionParameters(source, target);
    }

    private void populateConditionParameters(RuleActionDefinitionModel source, RuleActionDefinitionData target) {
        List<RuleActionDefinitionParameterModel> parameters = ruleActionDefinitionParameterRepository.findAllByActionDefinition(source);
        Map<String, RuleParameterDefinitionData> parameterMap = new HashMap();
        if(CollectionUtils.isNotEmpty(parameters)) {
            Iterator var4 = parameters.iterator();
            while (var4.hasNext()) {
                RuleActionDefinitionParameterModel sourceParameter = (RuleActionDefinitionParameterModel) var4.next();
                String parameterCode = sourceParameter.getCode();
                RuleParameterDefinitionData parameter = this.actionDefinitionParameterConverter.convert(sourceParameter);
                parameterMap.put(parameterCode, parameter);
            }
        }

        target.setParameters(parameterMap);
    }

    private void populateCategories(RuleActionDefinitionModel source, RuleActionDefinitionData target) {
        List<RuleActionDefinitionCategoryModel> categories = ruleActionDefinitionCategoryRepository.findAllByActionDefinitions(source);
        if(CollectionUtils.isNotEmpty(categories)) {
            target.setCategories(actionDefinitionCategoryConverter.convertAll(categories));
        } else {
            target.setCategories(new ArrayList<>());
        }
    }

    private void populateTranslatorParameters(RuleActionDefinitionModel source, RuleActionDefinitionData target) {
        target.setTranslatorParameters(super.translatorParameters(source.getTranslatorParameters()));
    }

    @Autowired
    public void setRuleActionDefinitionCategoryRepository(RuleActionDefinitionCategoryRepository ruleActionDefinitionCategoryRepository) {
        this.ruleActionDefinitionCategoryRepository = ruleActionDefinitionCategoryRepository;
    }

    @Autowired
    public void setRuleActionDefinitionParameterRepository(RuleActionDefinitionParameterRepository ruleActionDefinitionParameterRepository) {
        this.ruleActionDefinitionParameterRepository = ruleActionDefinitionParameterRepository;
    }
}
