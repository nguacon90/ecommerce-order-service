package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions;


import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleIrVariable;

import java.io.Serializable;
import java.util.Map;

public class RuleIrVariablesContainer implements Serializable {

    private String name;
    private RuleIrVariablesContainer parent;
    private Map<String, RuleIrVariablesContainer> children;
    private Map<String, RuleIrVariable> variables;
    private String[] path;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RuleIrVariablesContainer getParent() {
        return parent;
    }

    public void setParent(RuleIrVariablesContainer parent) {
        this.parent = parent;
    }

    public Map<String, RuleIrVariablesContainer> getChildren() {
        return children;
    }

    public void setChildren(Map<String, RuleIrVariablesContainer> children) {
        this.children = children;
    }

    public Map<String, RuleIrVariable> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, RuleIrVariable> variables) {
        this.variables = variables;
    }

    public String[] getPath() {
        return path;
    }

    public void setPath(String[] path) {
        this.path = path;
    }
}
