package com.vctek.orderservice.promotionengine.ruleengineservice.repository;

import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleActionDefinitionModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleActionDefinitionParameterModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RuleActionDefinitionParameterRepository extends JpaRepository<RuleActionDefinitionParameterModel, Long> {

    List<RuleActionDefinitionParameterModel> findAllByActionDefinition(RuleActionDefinitionModel conditionDefinition);
}
