package com.vctek.orderservice.promotionengine.ruleengineservice.service.impl;

import com.vctek.orderservice.promotionengine.ruleengine.enums.RuleType;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleActionDefinitionModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.repository.RuleActionDefinitionRepository;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleActionDefinitionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultRuleActionDefinitionService implements RuleActionDefinitionService {
    private RuleActionDefinitionRepository actionDefinitionRepository;

    public DefaultRuleActionDefinitionService(RuleActionDefinitionRepository actionDefinitionRepository) {
        this.actionDefinitionRepository = actionDefinitionRepository;
    }

    @Override
    public List<RuleActionDefinitionModel> getAllRuleActionDefinitions() {
        return actionDefinitionRepository.findAll();
    }

    @Override
    public List<RuleActionDefinitionModel> getRuleActionDefinitionsForRuleType(RuleType type) {
        return actionDefinitionRepository.findByRuleType(type.toString());
    }

    @Override
    public RuleActionDefinitionModel findByCode(String definitionId) {
        return actionDefinitionRepository.findByCode(definitionId);
    }
}
