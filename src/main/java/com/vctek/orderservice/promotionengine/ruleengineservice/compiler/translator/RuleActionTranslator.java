package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator;

import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.actions.RuleIrAction;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionDefinitionData;

public interface RuleActionTranslator {
    RuleIrAction translate(RuleCompilerContext context, RuleActionData action, RuleActionDefinitionData actionDefinition);
}
