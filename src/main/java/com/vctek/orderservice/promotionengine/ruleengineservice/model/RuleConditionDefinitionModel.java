package com.vctek.orderservice.promotionengine.ruleengineservice.model;


import com.vctek.orderservice.model.ItemModel;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "rule_condition_definition")
public class RuleConditionDefinitionModel extends ItemModel {

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "allows_children")
    private boolean allowsChildren;

    @Column(name = "translator_id")
    private String translatorId;

    @Column(name = "rule_type")
    private String ruleType;

    @Column(name = "translator_parameters_map")
    private String translatorParameters;

    @OneToMany(mappedBy = "conditionDefinition")
    private Set<RuleConditionDefinitionParameterModel> parameters = new HashSet<>();

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "rule_condition_definition_has_category",
            joinColumns = { @JoinColumn(name = "rule_condition_definition_id") },
            inverseJoinColumns = { @JoinColumn(name = "rule_condition_definition_category_id") }
    )
    private Set<RuleConditionDefinitionCategoryModel> categories = new HashSet<>();

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

    public boolean isAllowsChildren() {
        return allowsChildren;
    }

    public void setAllowsChildren(boolean allowsChildren) {
        this.allowsChildren = allowsChildren;
    }

    public String getTranslatorId() {
        return translatorId;
    }

    public void setTranslatorId(String translatorId) {
        this.translatorId = translatorId;
    }

    public String getTranslatorParameters() {
        return translatorParameters;
    }

    public void setTranslatorParameters(String translatorParameters) {
        this.translatorParameters = translatorParameters;
    }

    public Set<RuleConditionDefinitionParameterModel> getParameters() {
        return parameters;
    }

    public void setParameters(Set<RuleConditionDefinitionParameterModel> parameters) {
        this.parameters = parameters;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Set<RuleConditionDefinitionCategoryModel> getCategories() {
        return categories;
    }

    public void setCategories(Set<RuleConditionDefinitionCategoryModel> categories) {
        this.categories = categories;
    }
}
