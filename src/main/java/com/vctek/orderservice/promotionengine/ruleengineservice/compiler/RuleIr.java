package com.vctek.orderservice.promotionengine.ruleengineservice.compiler;

import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.actions.RuleIrAction;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrVariablesContainer;

import java.io.Serializable;
import java.util.List;

public class RuleIr implements Serializable {
    private RuleIrVariablesContainer variablesContainer;
    private List<RuleIrCondition> conditions;
    private List<RuleIrAction> actions;

    public List<RuleIrCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<RuleIrCondition> conditions) {
        this.conditions = conditions;
    }

    public List<RuleIrAction> getActions() {
        return actions;
    }

    public void setActions(List<RuleIrAction> actions) {
        this.actions = actions;
    }

    public RuleIrVariablesContainer getVariablesContainer() {
        return variablesContainer;
    }

    public void setVariablesContainer(RuleIrVariablesContainer variablesContainer) {
        this.variablesContainer = variablesContainer;
    }
}
