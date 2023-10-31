package com.vctek.orderservice.promotionengine.ruleengineservice.repository;

import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleConditionDefinitionCategoryModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleConditionDefinitionModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RuleConditionDefinitionCategoryRepository extends JpaRepository<RuleConditionDefinitionCategoryModel, Long> {

    List<RuleConditionDefinitionCategoryModel> findAllByConditionDefinitions(RuleConditionDefinitionModel conditionDefinition);
}
