package com.vctek.orderservice.promotionengine.ruleengineservice.model;


import com.vctek.orderservice.model.ItemModel;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "rule_action_definition")
public class RuleActionDefinitionModel extends ItemModel {

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "translator_id")
    private String translatorId;

    @Column(name = "rule_type")
    private String ruleType;

    @ManyToMany
    @JoinTable(
            name = "rule_action_definition_has_category",
            joinColumns = { @JoinColumn(name = "rule_action_definition_id") },
            inverseJoinColumns = { @JoinColumn(name = "rule_action_definition_category_id") }
    )
    private Set<RuleActionDefinitionCategoryModel> categories = new HashSet<>();

    @Column(name = "translator_parameter_map")
    private String translatorParameters;

    @OneToMany(mappedBy = "actionDefinition")
    private Set<RuleActionDefinitionParameterModel> parameters = new HashSet<>();

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

    public String getTranslatorId() {
        return translatorId;
    }

    public void setTranslatorId(String translatorId) {
        this.translatorId = translatorId;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public Set<RuleActionDefinitionCategoryModel> getCategories() {
        return categories;
    }

    public void setCategories(Set<RuleActionDefinitionCategoryModel> categories) {
        this.categories = categories;
    }

    public String getTranslatorParameters() {
        return translatorParameters;
    }

    public void setTranslatorParameters(String translatorParameters) {
        this.translatorParameters = translatorParameters;
    }

    public Set<RuleActionDefinitionParameterModel> getParameters() {
        return parameters;
    }

    public void setParameters(Set<RuleActionDefinitionParameterModel> parameters) {
        this.parameters = parameters;
    }
}
