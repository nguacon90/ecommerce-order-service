package com.vctek.orderservice.promotionengine.ruleengine;

import java.io.Serializable;
import java.util.Map;

public class ExecutionContext implements Serializable {
    private Map<String, Long> ruleVersions;

    public Map<String, Long> getRuleVersions() {
        return ruleVersions;
    }

    public void setRuleVersions(Map<String, Long> ruleVersions) {
        this.ruleVersions = ruleVersions;
    }
}
