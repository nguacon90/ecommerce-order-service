package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions;

public class RuleIrAttributeRelCondition extends AbstractRuleIrAttributeCondition {
    private String targetVariable;
    private String targetAttribute;

    public String getTargetVariable() {
        return targetVariable;
    }

    public void setTargetVariable(String targetVariable) {
        this.targetVariable = targetVariable;
    }

    public String getTargetAttribute() {
        return targetAttribute;
    }

    public void setTargetAttribute(String targetAttribute) {
        this.targetAttribute = targetAttribute;
    }
}
