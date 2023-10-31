package com.vctek.orderservice.promotionengine.ruleengine.model;

import com.vctek.orderservice.model.ItemModel;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class AbstractRuleEngineRuleModel extends ItemModel {
    @Column(name = "code")
    protected String code;

    @Column(name = "active")
    protected boolean active;

    @Column(name = "max_allowed_runs")
    protected Integer maxAllowedRuns;

    @Column(name = "message_fired")
    protected String messageFired;

    @Column(name = "rule_content")
    protected String ruleContent;

    @Column(name = "rule_group_code")
    protected String ruleGroupCode;

    @Column(name = "rule_parameters")
    protected String ruleParameters;

    @Column(name = "rule_type")
    protected String ruleType;

    @Column(name = "uuid")
    protected String uuid;

    @Column(name = "version")
    private Long version;

    @Column(name = "current_version")
    private Boolean currentVersion;


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Integer getMaxAllowedRuns() {
        return maxAllowedRuns;
    }

    public void setMaxAllowedRuns(Integer maxAllowedRuns) {
        this.maxAllowedRuns = maxAllowedRuns;
    }

    public String getMessageFired() {
        return messageFired;
    }

    public void setMessageFired(String messageFired) {
        this.messageFired = messageFired;
    }

    public String getRuleContent() {
        return ruleContent;
    }

    public void setRuleContent(String ruleContent) {
        this.ruleContent = ruleContent;
    }

    public String getRuleGroupCode() {
        return ruleGroupCode;
    }

    public void setRuleGroupCode(String ruleGroupCode) {
        this.ruleGroupCode = ruleGroupCode;
    }

    public String getRuleParameters() {
        return ruleParameters;
    }

    public void setRuleParameters(String ruleParameters) {
        this.ruleParameters = ruleParameters;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Boolean isCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(Boolean currentVersion) {
        this.currentVersion = currentVersion;
    }
}
