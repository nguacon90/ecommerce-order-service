package com.vctek.orderservice.promotionengine.ruleengine.util;

import com.google.common.base.Preconditions;
import com.vctek.orderservice.promotionengine.ruleengine.model.AbstractRuleEngineRuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;

import java.util.Objects;

public class EngineRulePreconditions {

    public static <T extends AbstractRuleEngineRuleModel> void checkRuleHasKieModule(T rule) {
        Preconditions.checkArgument(Objects.nonNull(rule), "Rule should not be null");
        Preconditions.checkArgument(rule instanceof DroolsRuleModel, "Rule must be instance of DroolsRuleModel");
        DroolsRuleModel droolsRule = (DroolsRuleModel) rule;
        if (Objects.isNull(droolsRule.getKieBase())) {
            throw new IllegalStateException("Rule [" + droolsRule.getCode() + "] has no KieBase assigned to it");
        } else if (Objects.isNull(droolsRule.getKieBase().getDroolsKIEModule())) {
            throw new IllegalStateException("Rule [" + droolsRule.getCode() + "] has no KieModule assigned to it");
        }
    }
}
