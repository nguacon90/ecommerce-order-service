package com.vctek.orderservice.promotionengine.ruleengineservice.repository;

import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleConditionDefinitionModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleConditionDefinitionParameterModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RuleConditionDefinitionParameterRepository extends JpaRepository<RuleConditionDefinitionParameterModel, Long> {

    List<RuleConditionDefinitionParameterModel> findAllByConditionDefinition(RuleConditionDefinitionModel conditionDefinition);
}
