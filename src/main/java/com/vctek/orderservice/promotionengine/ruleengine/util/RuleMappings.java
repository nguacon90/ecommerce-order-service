package com.vctek.orderservice.promotionengine.ruleengine.util;

import com.vctek.orderservice.promotionengine.ruleengine.model.AbstractRuleModuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;

public class RuleMappings {
    private RuleMappings() {
    }

    public static <R extends DroolsRuleModel, M extends AbstractRuleModuleModel> M module(R rule) {
        EngineRulePreconditions.checkRuleHasKieModule(rule);
        return (M) rule.getKieBase().getDroolsKIEModule();
    }

    public static <T extends DroolsRuleModel> String moduleName(T rule) {
        return module(rule).getName();
    }
}
