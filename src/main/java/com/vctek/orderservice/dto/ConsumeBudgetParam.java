package com.vctek.orderservice.dto;

import java.util.Date;
import java.util.Set;

public class ConsumeBudgetParam {
    private Long customerId;
    private String ruleCode;
    private Date createdOrderDate;
    private Set<Long> sourceRuleIds;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public Date getCreatedOrderDate() {
        return createdOrderDate;
    }

    public void setCreatedOrderDate(Date createdOrderDate) {
        this.createdOrderDate = createdOrderDate;
    }

    public Set<Long> getSourceRuleIds() {
        return sourceRuleIds;
    }

    public void setSourceRuleIds(Set<Long> sourceRuleIds) {
        this.sourceRuleIds = sourceRuleIds;
    }
}
