package com.vctek.orderservice.promotionengine.ruleengineservice.repository;

import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleActionDefinitionModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RuleActionDefinitionRepository extends JpaRepository<RuleActionDefinitionModel, Long> {
    List<RuleActionDefinitionModel> findByRuleType(String ruleType);

    RuleActionDefinitionModel findByCode(String code);
}
