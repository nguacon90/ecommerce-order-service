package com.vctek.orderservice.promotionengine.ruleengineservice.rao;

import java.util.Map;

public class DisplayMessageRAO extends AbstractRuleActionRAO {
    private Map<String,Object> parameters;

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}
