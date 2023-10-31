package com.vctek.orderservice.promotionengine.ruleengine.model;

import com.vctek.orderservice.model.ItemModel;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class AbstractRuleEngineContextModel extends ItemModel {
    @Column(name = "name")
    protected String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
