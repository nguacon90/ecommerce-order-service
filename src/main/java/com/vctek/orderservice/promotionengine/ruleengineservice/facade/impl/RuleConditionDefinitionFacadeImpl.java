package com.vctek.orderservice.promotionengine.ruleengineservice.facade.impl;

import com.vctek.converter.Converter;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.facade.RuleConditionDefinitionFacade;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleConditionDefinitionModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleConditionDefinitionService;
import org.springframework.stereotype.Component;

@Component
public class RuleConditionDefinitionFacadeImpl implements RuleConditionDefinitionFacade {
    private RuleConditionDefinitionService ruleConditionDefinitionService;
    private Converter<RuleConditionDefinitionModel, RuleConditionDefinitionData> ruleConditionDefinitionDataConverter;

    public RuleConditionDefinitionFacadeImpl(RuleConditionDefinitionService ruleConditionDefinitionService,
              Converter<RuleConditionDefinitionModel, RuleConditionDefinitionData> ruleConditionDefinitionDataConverter) {
        this.ruleConditionDefinitionService = ruleConditionDefinitionService;
        this.ruleConditionDefinitionDataConverter = ruleConditionDefinitionDataConverter;
    }

    @Override
    public RuleConditionDefinitionData findByDefinitionId(String definitionId) {
        RuleConditionDefinitionModel model = ruleConditionDefinitionService.findByCode(definitionId);
        if(model !=  null) {
            return ruleConditionDefinitionDataConverter.convert(model);
        }

        return null;
    }
}
