package com.vctek.orderservice.promotionengine.ruleengineservice.repository;

import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleConditionDefinitionModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RuleConditionDefinitionRepository extends JpaRepository<RuleConditionDefinitionModel, Long> {
    List<RuleConditionDefinitionModel> findByRuleType(String ruleType);

    RuleConditionDefinitionModel findByCode(String code);
}
