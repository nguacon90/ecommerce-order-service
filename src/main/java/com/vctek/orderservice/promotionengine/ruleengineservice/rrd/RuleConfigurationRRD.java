package com.vctek.orderservice.promotionengine.ruleengineservice.rrd;

import java.io.Serializable;

public class RuleConfigurationRRD implements Serializable {
    private String ruleCode;
    private Integer maxAllowedRuns;
    private Integer currentRuns;
    private String ruleGroupCode;

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public Integer getMaxAllowedRuns() {
        return maxAllowedRuns;
    }

    public void setMaxAllowedRuns(Integer maxAllowedRuns) {
        this.maxAllowedRuns = maxAllowedRuns;
    }

    public Integer getCurrentRuns() {
        return currentRuns;
    }

    public void setCurrentRuns(Integer currentRuns) {
        this.currentRuns = currentRuns;
    }

    public String getRuleGroupCode() {
        return ruleGroupCode;
    }

    public void setRuleGroupCode(String ruleGroupCode) {
        this.ruleGroupCode = ruleGroupCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RuleConfigurationRRD)) return false;
        RuleConfigurationRRD that = (RuleConfigurationRRD) o;
        return new org.apache.commons.lang.builder.EqualsBuilder()
                .append(getRuleCode(), that.getRuleCode())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new org.apache.commons.lang.builder.HashCodeBuilder()
                .append(getRuleCode())
                .toHashCode();
    }
}
