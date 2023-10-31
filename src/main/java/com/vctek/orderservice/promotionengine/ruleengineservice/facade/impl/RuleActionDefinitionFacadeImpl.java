package com.vctek.orderservice.promotionengine.ruleengineservice.facade.impl;

import com.vctek.converter.Converter;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.facade.RuleActionDefinitionFacade;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleActionDefinitionModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleActionDefinitionService;
import org.springframework.stereotype.Component;

@Component
public class RuleActionDefinitionFacadeImpl implements RuleActionDefinitionFacade {
    private RuleActionDefinitionService ruleActionDefinitionService;
    private Converter<RuleActionDefinitionModel, RuleActionDefinitionData> ruleActionDefinitionDataConverter;

    public RuleActionDefinitionFacadeImpl(RuleActionDefinitionService ruleActionDefinitionService,
                  Converter<RuleActionDefinitionModel, RuleActionDefinitionData> ruleActionDefinitionDataConverter) {
        this.ruleActionDefinitionService = ruleActionDefinitionService;
        this.ruleActionDefinitionDataConverter = ruleActionDefinitionDataConverter;
    }

    @Override
    public RuleActionDefinitionData findByDefinitionId(String definitionId) {
        RuleActionDefinitionModel model = ruleActionDefinitionService.findByCode(definitionId);
        if(model != null) {
            return ruleActionDefinitionDataConverter.convert(model);
        }

        return null;
    }
}
