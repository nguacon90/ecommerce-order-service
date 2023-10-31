package com.vctek.orderservice.promotionengine.ruleengineservice.model;


import com.vctek.orderservice.model.ItemModel;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.util.Date;

@MappedSuperclass
public class AbstractRuleModel extends ItemModel {
    @Column(name = "uuid")
    protected String uuid;

    @Column(name = "code")
    protected String code;

    @Column(name = "name")
    protected String name;

    @Column(name = "description")
    protected String description;

    @Column(name = "start_date")
    protected Date startDate;

    @Column(name = "end_date")
    protected Date endDate;

    @Column(name = "priority")
    protected Integer priority;

    @Column(name = "status")
    protected String status;

    @Column(name = "active")
    private boolean active;

    @Column(name = "message_fired")
    protected String messageFired;

    @Column(name = "max_allow_runs")
    protected Integer maxAllowedRuns;

    @Column(name = "version")
    private Long version;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
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

    public Integer getMaxAllowedRuns() {
        return maxAllowedRuns;
    }

    public void setMaxAllowedRuns(Integer maxAllowedRuns) {
        this.maxAllowedRuns = maxAllowedRuns;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
