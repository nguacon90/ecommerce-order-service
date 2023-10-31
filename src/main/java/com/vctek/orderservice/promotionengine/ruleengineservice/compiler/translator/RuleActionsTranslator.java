package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator;

import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.actions.RuleIrAction;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionData;

import java.util.List;

public interface RuleActionsTranslator {
    List<RuleIrAction> translate(RuleCompilerContext context, List<RuleActionData> ruleActions);

    void validate(RuleCompilerContext context, List<RuleActionData> actions);
}
