package com.vctek.orderservice.promotionengine.ruleengineservice.service.impl;

import com.vctek.orderservice.promotionengine.ruleengineservice.converter.RuleActionsConverter;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleActionsService;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleParametersService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DefaultRuleActionsService extends AbstractRuleService implements RuleActionsService {
    private RuleActionsConverter ruleActionsConverter;

    public DefaultRuleActionsService(RuleActionsConverter ruleActionsConverter, RuleParametersService ruleParametersService) {
        super(ruleParametersService);
        this.ruleActionsConverter = ruleActionsConverter;
    }

    @Override
    public RuleActionData createActionFromDefinition(RuleActionDefinitionData definition) {
        RuleActionData action = new RuleActionData();
        action.setDefinitionId(definition.getCode());
        action.setParameters(populateRuleParams(definition.getParameters()));
        return action;
    }

    @Override
    public List<RuleActionData> convertActionsFromString(String actions, Map<String, RuleActionDefinitionData> actionDefinitions) {
        return this.ruleActionsConverter.fromString(actions, actionDefinitions);
    }
}
