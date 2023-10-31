package com.vctek.orderservice.promotionengine.ruleengineservice.data;

import java.util.Map;

public abstract class AbstractRuleData {
    protected String definitionId;
    protected Map<String, RuleParameterData> parameters;

    public String getDefinitionId() {
        return definitionId;
    }

    public void setDefinitionId(String definitionId) {
        this.definitionId = definitionId;
    }

    public Map<String, RuleParameterData> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, RuleParameterData> parameters) {
        this.parameters = parameters;
    }
}
