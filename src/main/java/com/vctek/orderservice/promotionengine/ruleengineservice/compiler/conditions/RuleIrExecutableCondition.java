package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions;

import java.util.Map;

public class RuleIrExecutableCondition extends AbstractRuleIrBooleanCondition {
    private String conditionId;
    private Map<String,Object> conditionParameters;

    public String getConditionId() {
        return conditionId;
    }

    public void setConditionId(String conditionId) {
        this.conditionId = conditionId;
    }

    public Map<String, Object> getConditionParameters() {
        return conditionParameters;
    }

    public void setConditionParameters(Map<String, Object> conditionParameters) {
        this.conditionParameters = conditionParameters;
    }
}
