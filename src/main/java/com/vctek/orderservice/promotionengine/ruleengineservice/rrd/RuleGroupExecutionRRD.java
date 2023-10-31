package com.vctek.orderservice.promotionengine.ruleengineservice.rrd;

import java.util.Map;

public class RuleGroupExecutionRRD {
    private String code;
    private Map<String, Integer> executedRules;
    private boolean exclusive;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Map<String, Integer> getExecutedRules() {
        return executedRules;
    }

    public void setExecutedRules(Map<String, Integer> executedRules) {
        this.executedRules = executedRules;
    }

    public boolean isExclusive() {
        return exclusive;
    }

    public void setExclusive(boolean exclusive) {
        this.exclusive = exclusive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RuleGroupExecutionRRD)) return false;
        RuleGroupExecutionRRD that = (RuleGroupExecutionRRD) o;
        return new org.apache.commons.lang.builder.EqualsBuilder()
                .append(getCode(), that.getCode())
                .append(isExclusive(), that.isExclusive())
                .isEquals();
    }

    @Override
    public int hashCode() {

        return new org.apache.commons.lang.builder.HashCodeBuilder()
                .append(getCode())
                .append(isExclusive())
                .toHashCode();
    }

    public boolean allowedToExecute(final RuleConfigurationRRD ruleConfig) {
        if (this.executedRules == null) {
            // first execution of the group
            return true;
        }

        if (this.executedRules.entrySet().isEmpty()) {
            // first execution of the group
            return true;
        }

        // unless this rule has been triggered already and has more than 1 executions allowed
        final Integer current = this.executedRules.get(ruleConfig.getRuleCode());
        if (current == null) {
            if (isExclusive()) {
                // if a rule from the exclusive rule group already triggered but not for this rule
                return false;
            } else {
                // this rule hasn't been yet tracked
                return true;
            }
        }

        Integer max = ruleConfig.getMaxAllowedRuns();
        if (max == null) {
            max = Integer.valueOf(1);
        }
        return current.compareTo(max) < 0;
    }
}
