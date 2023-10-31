package com.vctek.orderservice.promotionengine.ruleengineservice.compiler;


import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;

public interface RuleCompilerService {
    DroolsRuleModel compile(PromotionSourceRuleModel sourceRule, String moduleName);
}
