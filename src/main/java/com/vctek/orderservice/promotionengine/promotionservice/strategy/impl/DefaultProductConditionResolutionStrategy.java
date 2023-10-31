package com.vctek.orderservice.promotionengine.promotionservice.strategy.impl;

import com.vctek.orderservice.promotionengine.promotionservice.strategy.ConditionResolutionStrategy;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import org.springframework.stereotype.Component;

@Component("productConditionResolutionStrategy")
public class DefaultProductConditionResolutionStrategy implements ConditionResolutionStrategy {
//    private ProductPromotionSourceRuleService productPromotionSourceRuleService;
    @Override
    public void cleanStoredParameterValues(RuleCompilerContext context) {
//        List<ProductForPromotionSourceRuleModel> productForPromotionModels = productPromotionSourceRuleService
//                .findAllBy(context.getRule(), context.getModuleName());
//        productPromotionSourceRuleService.removeAll(productForPromotionModels);
    }
}
