package com.vctek.orderservice.promotionengine.ruleengineservice.data;

import java.util.List;
import java.util.Map;

public class RuleActionDefinitionData extends AbstractRuleDefinitionData {
    private Integer priority;
    private String breadcrumb;
    private String translatorId;
    private Map<String,String> translatorParameters;
    private List<RuleActionDefinitionCategoryData> categories;

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

    public List<RuleActionDefinitionCategoryData> getCategories() {
        return categories;
    }

    public void setCategories(List<RuleActionDefinitionCategoryData> categories) {
        this.categories = categories;
    }
}
