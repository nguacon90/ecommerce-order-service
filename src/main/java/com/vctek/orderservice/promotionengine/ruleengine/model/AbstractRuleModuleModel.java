package com.vctek.orderservice.promotionengine.ruleengine.model;


import com.vctek.orderservice.model.ItemModel;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class AbstractRuleModuleModel extends ItemModel {
    @Column(name = "name")
    protected String name;

    @Column(name = "rule_type")
    protected String ruleType;

    @Column(name = "active")
    protected Boolean active;

    @Column(name = "lock_acquired")
    protected Boolean lockAcquired;

    @Column(name = "version")
    private Long version;

    @Column(name = "company_id")
    private Long companyId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getLockAcquired() {
        return lockAcquired;
    }

    public void setLockAcquired(Boolean lockAcquired) {
        this.lockAcquired = lockAcquired;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getCompanyId() {
        return companyId;
    }
}
