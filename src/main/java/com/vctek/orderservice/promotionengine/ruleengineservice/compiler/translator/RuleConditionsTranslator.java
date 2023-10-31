package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator;


import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;

import java.util.List;

public interface RuleConditionsTranslator {
    List<RuleIrCondition> translate(RuleCompilerContext context, List<RuleConditionData> conditions);

    void validate(RuleCompilerContext context, List<RuleConditionData> conditions);
}
