package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions;

import java.io.Serializable;
import java.util.Map;

public abstract class RuleIrCondition implements Serializable {
    private Map<String,Object> metadata;

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
