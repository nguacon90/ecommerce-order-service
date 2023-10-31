package com.vctek.orderservice.service;

import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionBudgetModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.PromotionSourceRuleData;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.PromotionBudgetRAO;

import java.util.List;

public interface PromotionBudgetService {
    void createPromotionBudget(PromotionSourceRuleModel sourceRuleModel, PromotionSourceRuleData promotionSourceRuleData);

    List<PromotionBudgetModel> findAllBy(PromotionSourceRuleModel rule);

    List<PromotionBudgetRAO> findAllOf(List<Long> userGroupIds);
}
