package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.actions;

import java.util.Map;

public class RuleIrExecutableAction extends RuleIrAction {

    private String actionId;

    private Map<String,Object> actionParameters;

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    public Map<String, Object> getActionParameters() {
        return actionParameters;
    }

    public void setActionParameters(Map<String, Object> actionParameters) {
        this.actionParameters = actionParameters;
    }
}
