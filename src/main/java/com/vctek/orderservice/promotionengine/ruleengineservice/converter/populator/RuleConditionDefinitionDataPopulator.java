package com.vctek.orderservice.promotionengine.ruleengineservice.converter.populator;

import com.vctek.converter.Converter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionCategoryData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleConditionDefinitionCategoryModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleConditionDefinitionModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleConditionDefinitionParameterModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.repository.RuleConditionDefinitionCategoryRepository;
import com.vctek.orderservice.promotionengine.ruleengineservice.repository.RuleConditionDefinitionParameterRepository;
import com.vctek.orderservice.promotionengine.ruleengineservice.strategy.RuleParameterValueConverter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RuleConditionDefinitionDataPopulator extends AbstractDefinitionPopulator
        implements Populator<RuleConditionDefinitionModel, RuleConditionDefinitionData> {
    private RuleConditionDefinitionCategoryRepository ruleConditionDefinitionCategoryRepository;
    private RuleConditionDefinitionParameterRepository ruleConditionDefinitionParameterRepository;

    private Converter<RuleConditionDefinitionCategoryModel, RuleConditionDefinitionCategoryData> conditionCategoryConverter;
    private Converter<RuleConditionDefinitionParameterModel, RuleParameterDefinitionData> ruleConditionDefinitionParameterConverter;

    public RuleConditionDefinitionDataPopulator(RuleParameterValueConverter ruleParameterValueConverter,
                                                Converter<RuleConditionDefinitionCategoryModel, RuleConditionDefinitionCategoryData> conditionCategoryConverter,
                                                Converter<RuleConditionDefinitionParameterModel, RuleParameterDefinitionData> ruleConditionDefinitionParameterConverter) {
        super(ruleParameterValueConverter);
        this.conditionCategoryConverter = conditionCategoryConverter;
        this.ruleConditionDefinitionParameterConverter = ruleConditionDefinitionParameterConverter;
    }

    @Override
    public void populate(RuleConditionDefinitionModel source, RuleConditionDefinitionData target) {
        target.setId(source.getId());
        target.setCode(source.getCode());
        target.setAllowsChildren(source.isAllowsChildren());
        target.setPriority(source.getPriority());
        target.setName(source.getName());
        target.setTranslatorId(source.getTranslatorId());
        populateTranslatorParameters(source, target);
        populateCategories(source, target);
        populateConditionParameters(source, target);
    }

    private void populateConditionParameters(RuleConditionDefinitionModel source, RuleConditionDefinitionData target) {
        List<RuleConditionDefinitionParameterModel> parameters = ruleConditionDefinitionParameterRepository.findAllByConditionDefinition(source);
        Map<String, RuleParameterDefinitionData> parameterMap = new HashMap();
        if (CollectionUtils.isNotEmpty(parameters)) {
            Iterator var4 = parameters.iterator();

            while (var4.hasNext()) {
                RuleConditionDefinitionParameterModel sourceParameter = (RuleConditionDefinitionParameterModel) var4.next();
                String parameterCode = sourceParameter.getCode();
                RuleParameterDefinitionData parameter = this.ruleConditionDefinitionParameterConverter.convert(sourceParameter);
                parameterMap.put(parameterCode, parameter);
            }
        }

        target.setParameters(parameterMap);
    }

    private void populateCategories(RuleConditionDefinitionModel source, RuleConditionDefinitionData target) {
        List<RuleConditionDefinitionCategoryModel> categories = ruleConditionDefinitionCategoryRepository.findAllByConditionDefinitions(source);
        if (CollectionUtils.isNotEmpty(categories)) {
            target.setCategories(conditionCategoryConverter.convertAll(categories));
        } else {
            target.setCategories(new ArrayList<>());
        }
    }

    private void populateTranslatorParameters(RuleConditionDefinitionModel source, RuleConditionDefinitionData target) {
        target.setTranslatorParameters(super.translatorParameters(source.getTranslatorParameters()));
    }

    @Autowired
    public void setRuleConditionDefinitionCategoryRepository(RuleConditionDefinitionCategoryRepository ruleConditionDefinitionCategoryRepository) {
        this.ruleConditionDefinitionCategoryRepository = ruleConditionDefinitionCategoryRepository;
    }

    @Autowired
    public void setRuleConditionDefinitionParameterRepository(RuleConditionDefinitionParameterRepository ruleConditionDefinitionParameterRepository) {
        this.ruleConditionDefinitionParameterRepository = ruleConditionDefinitionParameterRepository;
    }
}
