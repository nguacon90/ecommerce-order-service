package com.vctek.orderservice.promotionengine.ruleengineservice.compiler;

import java.io.Serializable;

public class RuleIrVariable implements Serializable {
    private String name;
    private Class<?> type;
    private String[] path;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public String[] getPath() {
        return path;
    }

    public void setPath(String[] path) {
        this.path = path;
    }
}
