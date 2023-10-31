package com.vctek.orderservice.promotionengine.ruleengineservice.model;


import com.vctek.orderservice.model.ItemModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "rule_action_definition_category")
public class RuleActionDefinitionCategoryModel extends ItemModel {
    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "priority")
    private Integer priority;

    @ManyToMany(mappedBy = "categories")
    private Set<RuleActionDefinitionModel> actionDefinitions = new HashSet<>();

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Set<RuleActionDefinitionModel> getActionDefinitions() {
        return actionDefinitions;
    }

    public void setActionDefinitions(Set<RuleActionDefinitionModel> actionDefinitions) {
        this.actionDefinitions = actionDefinitions;
    }
}
