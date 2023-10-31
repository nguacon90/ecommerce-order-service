package com.vctek.orderservice.promotionengine.ruleengineservice.data;

import java.io.Serializable;

public class RuleParameterData implements Serializable {
    private String uuid;
    private String type;
    private Object value;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
