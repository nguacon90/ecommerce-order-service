package com.vctek.orderservice.promotionengine.ruleengineservice.rao;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class AbstractRuleActionRAO implements Serializable {
    private String firedRuleCode;
    private String moduleName;
    private String actionStrategyKey;
    private Map<String,String> metadata;
    private AbstractActionedRAO appliedToObject;
    private Set<OrderEntryConsumedRAO> consumedEntries;

    private BudgetConsumedRAO consumedBudget;
    public String getFiredRuleCode() {
        return firedRuleCode;
    }

    public void setFiredRuleCode(String firedRuleCode) {
        this.firedRuleCode = firedRuleCode;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getActionStrategyKey() {
        return actionStrategyKey;
    }

    public void setActionStrategyKey(String actionStrategyKey) {
        this.actionStrategyKey = actionStrategyKey;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public AbstractActionedRAO getAppliedToObject() {
        return appliedToObject;
    }

    public void setAppliedToObject(AbstractActionedRAO appliedToObject) {
        this.appliedToObject = appliedToObject;
    }

    public Set<OrderEntryConsumedRAO> getConsumedEntries() {
        return consumedEntries;
    }

    public void setConsumedEntries(Set<OrderEntryConsumedRAO> consumedEntries) {
        this.consumedEntries = consumedEntries;
    }

    public BudgetConsumedRAO getConsumedBudget() {
        return consumedBudget;
    }

    public void setConsumedBudget(BudgetConsumedRAO consumedBudget) {
        this.consumedBudget = consumedBudget;
    }
}
