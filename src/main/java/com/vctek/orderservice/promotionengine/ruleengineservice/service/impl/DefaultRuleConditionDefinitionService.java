package com.vctek.orderservice.promotionengine.ruleengineservice.service.impl;

import com.vctek.orderservice.promotionengine.ruleengine.enums.RuleType;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleConditionDefinitionModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.repository.RuleConditionDefinitionRepository;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleConditionDefinitionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultRuleConditionDefinitionService implements RuleConditionDefinitionService {
    private RuleConditionDefinitionRepository ruleConditionDefinitionRepository;

    public DefaultRuleConditionDefinitionService(RuleConditionDefinitionRepository ruleConditionDefinitionRepository) {
        this.ruleConditionDefinitionRepository = ruleConditionDefinitionRepository;
    }

    @Override
    public List<RuleConditionDefinitionModel> getAllRuleConditionDefinitions() {
        return ruleConditionDefinitionRepository.findAll();
    }

    @Override
    public List<RuleConditionDefinitionModel> getRuleConditionDefinitionsForRuleType(RuleType type) {
        return ruleConditionDefinitionRepository.findByRuleType(type.toString());
    }

    @Override
    public RuleConditionDefinitionModel findByCode(String code) {
        return ruleConditionDefinitionRepository.findByCode(code);
    }
}
