package com.vctek.orderservice.promotionengine.ruleengineservice.data;

import java.util.List;
import java.util.Map;

public class RuleConditionDefinitionData extends AbstractRuleDefinitionData {
    private Integer priority;
    private String breadcrumb;
    private Boolean allowsChildren;
    private String translatorId;
    private Map<String,String> translatorParameters;
    private List<RuleConditionDefinitionCategoryData> categories;

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getBreadcrumb() {
        return breadcrumb;
    }

    public void setBreadcrumb(String breadcrumb) {
        this.breadcrumb = breadcrumb;
    }

    public Boolean getAllowsChildren() {
        return allowsChildren;
    }

    public void setAllowsChildren(Boolean allowsChildren) {
        this.allowsChildren = allowsChildren;
    }

    public List<RuleConditionDefinitionCategoryData> getCategories() {
        return categories;
    }

    public void setCategories(List<RuleConditionDefinitionCategoryData> categories) {
        this.categories = categories;
    }

    public String getTranslatorId() {
        return translatorId;
    }

    public void setTranslatorId(String translatorId) {
        this.translatorId = translatorId;
    }

    public Map<String, String> getTranslatorParameters() {
        return translatorParameters;
    }

    public void setTranslatorParameters(Map<String, String> translatorParameters) {
        this.translatorParameters = translatorParameters;
    }
}
