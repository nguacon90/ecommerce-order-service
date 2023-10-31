package com.vctek.orderservice.promotionengine.promotionservice.strategy.impl;

import com.vctek.orderservice.promotionengine.promotionservice.strategy.ConditionResolutionStrategy;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import org.springframework.stereotype.Component;

@Component("categoryConditionResolutionStrategy")
public class DefaultCategoryConditionResolutionStrategy implements ConditionResolutionStrategy {
    @Override
    public void cleanStoredParameterValues(RuleCompilerContext context) {
        //NOSONAR
    }
}
