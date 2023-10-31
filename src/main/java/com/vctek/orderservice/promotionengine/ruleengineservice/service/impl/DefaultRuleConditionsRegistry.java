package com.vctek.orderservice.promotionengine.ruleengineservice.service.impl;

import com.vctek.converter.Converter;
import com.vctek.orderservice.promotionengine.ruleengine.enums.RuleType;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleConditionDefinitionModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleConditionDefinitionService;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleConditionsRegistry;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DefaultRuleConditionsRegistry implements RuleConditionsRegistry {
    private RuleConditionDefinitionService ruleConditionDefinitionService;
    private Converter<RuleConditionDefinitionModel, RuleConditionDefinitionData> ruleConditionDefinitionConverter;

    public DefaultRuleConditionsRegistry(RuleConditionDefinitionService ruleConditionDefinitionService,
                                         Converter<RuleConditionDefinitionModel, RuleConditionDefinitionData> ruleConditionDefinitionConverter) {
        this.ruleConditionDefinitionService = ruleConditionDefinitionService;
        this.ruleConditionDefinitionConverter = ruleConditionDefinitionConverter;
    }

    @Override
    public List<RuleConditionDefinitionData> getAllConditionDefinitions() {
        List<RuleConditionDefinitionModel> models = ruleConditionDefinitionService.getAllRuleConditionDefinitions();
        if(CollectionUtils.isNotEmpty(models)) {
            return ruleConditionDefinitionConverter.convertAll(models);
        }

        return new ArrayList<>();
    }

    @Override
    public Map<String, RuleConditionDefinitionData> getAllConditionDefinitionsAsMap() {
        Map<String, RuleConditionDefinitionData> result = new HashMap();
        List<RuleConditionDefinitionData> ruleConditionDefinitions = this.getAllConditionDefinitions();
        Iterator var4 = ruleConditionDefinitions.iterator();

        while(var4.hasNext()) {
            RuleConditionDefinitionData conditionDefinition = (RuleConditionDefinitionData)var4.next();
            result.put(conditionDefinition.getCode(), conditionDefinition);
        }

        return result;
    }

    @Override
    public List<RuleConditionDefinitionData> getConditionDefinitionsForRuleType(RuleType type) {
        List<RuleConditionDefinitionModel> models = ruleConditionDefinitionService.getRuleConditionDefinitionsForRuleType(type);
        if(CollectionUtils.isNotEmpty(models)) {
            return ruleConditionDefinitionConverter.convertAll(models);
        }

        return new ArrayList<>();
    }

    @Override
    public Map<String, RuleConditionDefinitionData> getConditionDefinitionsForRuleTypeAsMap(RuleType type) {
        Map<String, RuleConditionDefinitionData> result = new HashMap();
        List<RuleConditionDefinitionData> conditionDefinitions = this.getConditionDefinitionsForRuleType(type);
        Iterator var5 = conditionDefinitions.iterator();

        while(var5.hasNext()) {
            RuleConditionDefinitionData conditionDefinition = (RuleConditionDefinitionData)var5.next();
            result.put(conditionDefinition.getCode(), conditionDefinition);
        }

        return result;
    }
}
