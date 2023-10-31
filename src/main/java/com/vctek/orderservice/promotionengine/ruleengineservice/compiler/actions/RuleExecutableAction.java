package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.actions;


import com.vctek.orderservice.promotionengine.ruleengine.model.eveluation.RuleActionContext;

import java.util.Map;

public interface RuleExecutableAction {
    void executeAction(RuleActionContext context, Map<String, Object> parameters);
}
