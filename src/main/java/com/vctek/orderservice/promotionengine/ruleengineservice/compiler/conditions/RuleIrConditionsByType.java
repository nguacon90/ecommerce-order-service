package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions;

import org.apache.commons.collections4.ListValuedMap;

import java.io.Serializable;
import java.util.List;

public class RuleIrConditionsByType implements Serializable {
    private ListValuedMap<Boolean, AbstractRuleIrBooleanCondition> booleanConditions;
    private ListValuedMap<String, AbstractRuleIrPatternCondition> patternConditions;
    private List<RuleIrExecutableCondition> executableConditions;
    private List<RuleIrGroupCondition> groupConditions;
    private List<RuleIrExistsCondition> existsConditions;
    private List<RuleIrNotCondition> notConditions;

    public void setBooleanConditions(ListValuedMap<Boolean, AbstractRuleIrBooleanCondition> booleanConditions) {
        this.booleanConditions = booleanConditions;
    }

    public ListValuedMap<Boolean, AbstractRuleIrBooleanCondition> getBooleanConditions() {
        return this.booleanConditions;
    }

    public void setPatternConditions(ListValuedMap<String, AbstractRuleIrPatternCondition> patternConditions) {
        this.patternConditions = patternConditions;
    }

    public ListValuedMap<String, AbstractRuleIrPatternCondition> getPatternConditions() {
        return this.patternConditions;
    }

    public void setGroupConditions(List<RuleIrGroupCondition> groupConditions) {
        this.groupConditions = groupConditions;
    }

    public List<RuleIrGroupCondition> getGroupConditions() {
        return this.groupConditions;
    }

    public void setExecutableConditions(List<RuleIrExecutableCondition> executableConditions) {
        this.executableConditions = executableConditions;
    }

    public List<RuleIrExecutableCondition> getExecutableConditions() {
        return this.executableConditions;
    }

    public void setExistsConditions(List<RuleIrExistsCondition> existsConditions) {
        this.existsConditions = existsConditions;
    }

    public List<RuleIrExistsCondition> getExistsConditions() {
        return this.existsConditions;
    }

    public void setNotConditions(List<RuleIrNotCondition> notConditions) {
        this.notConditions = notConditions;
    }

    public List<RuleIrNotCondition> getNotConditions() {
        return this.notConditions;
    }
}
