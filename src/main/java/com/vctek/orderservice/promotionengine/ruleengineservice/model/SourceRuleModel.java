package com.vctek.orderservice.promotionengine.ruleengineservice.model;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class SourceRuleModel extends AbstractRuleModel {

    @Column(name = "conditions")
    protected String conditions;

    @Column(name = "actions")
    protected String actions;

    public String getConditions() {
        return conditions;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public String getActions() {
        return actions;
    }

    public void setActions(String actions) {
        this.actions = actions;
    }

}
