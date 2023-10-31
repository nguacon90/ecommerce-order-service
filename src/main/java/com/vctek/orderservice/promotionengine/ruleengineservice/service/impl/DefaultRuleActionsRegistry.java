package com.vctek.orderservice.promotionengine.ruleengineservice.service.impl;

import com.vctek.converter.Converter;
import com.vctek.orderservice.promotionengine.ruleengine.enums.RuleType;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleActionDefinitionModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleActionDefinitionService;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleActionsRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DefaultRuleActionsRegistry implements RuleActionsRegistry {

    private RuleActionDefinitionService ruleActionDefinitionService;
    private Converter<RuleActionDefinitionModel, RuleActionDefinitionData> ruleActionDefinitionConverter;

    public List<RuleActionDefinitionData> getAllActionDefinitions() {
        return this.convertActionDefinitions(this.ruleActionDefinitionService.getAllRuleActionDefinitions());
    }

    public Map<String, RuleActionDefinitionData> getAllActionDefinitionsAsMap() {
        List<RuleActionDefinitionData> actionDefinitions = this.convertActionDefinitions(this.ruleActionDefinitionService.getAllRuleActionDefinitions());
        Map<String, RuleActionDefinitionData> result = new HashMap();
        Iterator var4 = actionDefinitions.iterator();

        while(var4.hasNext()) {
            RuleActionDefinitionData actionDefinition = (RuleActionDefinitionData)var4.next();
            result.put(actionDefinition.getCode(), actionDefinition);
        }

        return result;
    }

    public List<RuleActionDefinitionData> getActionDefinitionsForRuleType(RuleType ruleType) {
        return this.convertActionDefinitions(this.ruleActionDefinitionService.getRuleActionDefinitionsForRuleType(ruleType));
    }

    public Map<String, RuleActionDefinitionData> getActionDefinitionsForRuleTypeAsMap(RuleType ruleType) {
        List<RuleActionDefinitionData> actionDefinitions = this.convertActionDefinitions(this.ruleActionDefinitionService.getRuleActionDefinitionsForRuleType(ruleType));
        Map<String, RuleActionDefinitionData> result = new HashMap();
        Iterator var5 = actionDefinitions.iterator();

        while(var5.hasNext()) {
            RuleActionDefinitionData actionDefinition = (RuleActionDefinitionData)var5.next();
            result.put(actionDefinition.getCode(), actionDefinition);
        }

        return result;
    }

    protected List<RuleActionDefinitionData> convertActionDefinitions(List<RuleActionDefinitionModel> definitions) {
        List<RuleActionDefinitionData> definitionsData = new ArrayList();
        definitions.stream().forEach((model) -> {
            definitionsData.add(this.ruleActionDefinitionConverter.convert(model));
        });
        return definitionsData;
    }

    @Autowired
    public void setRuleActionDefinitionService(RuleActionDefinitionService ruleActionDefinitionService) {
        this.ruleActionDefinitionService = ruleActionDefinitionService;
    }

    @Autowired
    public void setRuleActionDefinitionConverter(Converter<RuleActionDefinitionModel, RuleActionDefinitionData> ruleActionDefinitionConverter) {
        this.ruleActionDefinitionConverter = ruleActionDefinitionConverter;
    }
}
