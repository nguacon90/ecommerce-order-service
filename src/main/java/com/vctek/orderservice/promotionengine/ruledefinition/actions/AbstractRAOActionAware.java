package com.vctek.orderservice.promotionengine.ruledefinition.actions;


import com.vctek.orderservice.promotionengine.ruleengine.model.eveluation.RuleActionContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.actions.RuleExecutableAction;
import com.vctek.orderservice.promotionengine.ruleengineservice.evaluate.RAOAction;

import java.util.Map;

public abstract class AbstractRAOActionAware implements RuleExecutableAction {

    private RAOAction raoAction;

    public void executeAction(RuleActionContext context, Map<String, Object> parameters) {
        context.setParameters(parameters);
        this.getRaoAction().performAction(context);
    }

    protected RAOAction getRaoAction() {
        return this.raoAction;
    }

    public void setRaoAction(RAOAction raoAction) {
        this.raoAction = raoAction;
    }
}
