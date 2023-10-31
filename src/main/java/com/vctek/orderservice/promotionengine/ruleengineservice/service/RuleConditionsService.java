package com.vctek.orderservice.promotionengine.ruleengineservice.service;


import com.vctek.orderservice.dto.PromotionResultData;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RuleConditionsService {
    RuleConditionData createConditionFromDefinition(RuleConditionDefinitionData definition);

    String convertConditionsToString(List<RuleConditionData> conditions, Map<String, RuleConditionDefinitionData> conditionDefinitions);

    List<RuleConditionData> convertConditionsFromString(String conditions, Map<String, RuleConditionDefinitionData> conditionDefinitions);

    List<PromotionResultData> sortSourceRulesByOrderTotalCondition(Set<PromotionSourceRuleModel> promotionSourceRuleModelList);

    double getMinOrderTotalValueCondition(Map<String, RuleConditionDefinitionData> conditionDefinitions, PromotionSourceRuleModel sourceRuleModel);

}
