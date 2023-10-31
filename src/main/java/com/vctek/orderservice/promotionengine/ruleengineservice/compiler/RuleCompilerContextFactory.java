package com.vctek.orderservice.promotionengine.ruleengineservice.compiler;

import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.maintenance.RuleCompilationContext;

public interface RuleCompilerContextFactory<T extends RuleCompilerContext> {
    T createContext(RuleCompilationContext ruleCompilationContext, PromotionSourceRuleModel rule, String moduleName, RuleIrVariablesGenerator variablesGenerator);
}
