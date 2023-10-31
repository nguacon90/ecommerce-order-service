package com.vctek.orderservice.promotionengine.ruleengineservice.rao;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashSet;

public class RuleEngineResultRAO implements Serializable {
    private Date startTime;
    private Date endTime;
    private LinkedHashSet<AbstractRuleActionRAO> actions;

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public LinkedHashSet<AbstractRuleActionRAO> getActions() {
        return actions;
    }

    public void setActions(LinkedHashSet<AbstractRuleActionRAO> actions) {
        this.actions = actions;
    }
}
