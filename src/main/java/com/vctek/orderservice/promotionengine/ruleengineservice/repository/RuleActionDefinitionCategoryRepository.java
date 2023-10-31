package com.vctek.orderservice.promotionengine.ruleengineservice.repository;

import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleActionDefinitionCategoryModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleActionDefinitionModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RuleActionDefinitionCategoryRepository extends JpaRepository<RuleActionDefinitionCategoryModel, Long> {

    List<RuleActionDefinitionCategoryModel> findAllByActionDefinitions(RuleActionDefinitionModel actionDefinition);
}
