package com.vctek.orderservice.promotionengine.ruleengineservice.data;

import java.util.Map;

public class AbstractRuleDefinitionData {
    protected Long id;
    protected String code;
    protected String name;
    protected Map<String, RuleParameterDefinitionData> parameters;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Map<String, RuleParameterDefinitionData> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, RuleParameterDefinitionData> parameters) {
        this.parameters = parameters;
    }
}
