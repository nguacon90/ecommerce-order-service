package com.vctek.orderservice.promotionengine.ruleengineservice.rao;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.LinkedHashSet;

public class AbstractActionedRAO implements java.io.Serializable {
    private LinkedHashSet<AbstractRuleActionRAO> actions;

    public LinkedHashSet<AbstractRuleActionRAO> getActions() {
        return actions;
    }

    public void setActions(LinkedHashSet<AbstractRuleActionRAO> actions) {
        this.actions = actions;
    }

    @Override
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
