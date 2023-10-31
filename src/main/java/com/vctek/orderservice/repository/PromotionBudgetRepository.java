package com.vctek.orderservice.repository;

import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionBudgetModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromotionBudgetRepository extends JpaRepository<PromotionBudgetModel, Long> {

    List<PromotionBudgetModel> findAllByPromotionSourceRuleModel(PromotionSourceRuleModel sourceRuleModel);

    List<PromotionBudgetModel> findAllByCustomerGroupIdIn(List<Long> customerGroupIds);
}
