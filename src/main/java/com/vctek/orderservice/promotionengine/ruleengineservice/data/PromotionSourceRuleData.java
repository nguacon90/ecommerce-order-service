package com.vctek.orderservice.promotionengine.ruleengineservice.data;

import com.vctek.orderservice.promotionengine.promotionservice.model.CampaignModel;

import java.util.Date;
import java.util.List;
import java.util.Set;

public class PromotionSourceRuleData {
    private Long id;
    private String code;
    private Long companyId;
    private Date startDate;
    private Date endDate;
    private String description;
    private String messageFired;
    private Integer priority;
    private String name;
    private String status;
    private boolean active;
    private boolean appliedOnlyOne;
    private String appliedWarehouseIds;
    private String appliedOrderTypes;
    private String appliedPriceTypes;
    private String excludeOrderSources;
    private String uuid;
    private Set<CampaignModel> campaigns;
    private List<RuleConditionData> conditions;
    private List<RuleActionData> actions;
    private boolean allowReward;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public List<RuleConditionData> getConditions() {
        return conditions;
    }

    public void setConditions(List<RuleConditionData> conditions) {
        this.conditions = conditions;
    }

    public List<RuleActionData> getActions() {
        return actions;
    }

    public void setActions(List<RuleActionData> actions) {
        this.actions = actions;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessageFired() {
        return messageFired;
    }

    public void setMessageFired(String messageFired) {
        this.messageFired = messageFired;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Set<CampaignModel> getCampaigns() {
        return campaigns;
    }

    public void setCampaigns(Set<CampaignModel> campaigns) {
        this.campaigns = campaigns;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getAppliedWarehouseIds() {
        return appliedWarehouseIds;
    }

    public void setAppliedWarehouseIds(String appliedWarehouseIds) {
        this.appliedWarehouseIds = appliedWarehouseIds;
    }

    public String getAppliedOrderTypes() {
        return appliedOrderTypes;
    }

    public void setAppliedOrderTypes(String appliedOrderTypes) {
        this.appliedOrderTypes = appliedOrderTypes;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isAllowReward() {
        return allowReward;
    }

    public void setAllowReward(boolean allowReward) {
        this.allowReward = allowReward;
    }

    public String getAppliedPriceTypes() {
        return appliedPriceTypes;
    }

    public void setAppliedPriceTypes(String appliedPriceTypes) {
        this.appliedPriceTypes = appliedPriceTypes;
    }

    public boolean isAppliedOnlyOne() {
        return appliedOnlyOne;
    }

    public void setAppliedOnlyOne(boolean appliedOnlyOne) {
        this.appliedOnlyOne = appliedOnlyOne;
    }

    public String getExcludeOrderSources() {
        return excludeOrderSources;
    }

    public void setExcludeOrderSources(String excludeOrderSources) {
        this.excludeOrderSources = excludeOrderSources;
    }
}
