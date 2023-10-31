package com.vctek.orderservice.promotionengine.ruleengine.model;


import com.vctek.orderservice.model.ItemModel;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "drools_kie_session")
public class DroolsKIESessionModel extends ItemModel {

    @Column(name = "name")
    private String name;

    @Column(name = "session_type")
    private String sessionType;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "drools_kie_base_id" )
    private DroolsKIEBaseModel droolsKIEBase;

    @OneToMany(mappedBy = "kieSession", cascade = CascadeType.ALL)
    private Set<DroolsRuleEngineContextModel> droolsRuleEngineContexts = new HashSet<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public DroolsKIEBaseModel getDroolsKIEBase() {
        return droolsKIEBase;
    }

    public void setDroolsKIEBase(DroolsKIEBaseModel droolsKIEBase) {
        this.droolsKIEBase = droolsKIEBase;
    }

    public Set<DroolsRuleEngineContextModel> getDroolsRuleEngineContexts() {
        return droolsRuleEngineContexts;
    }

    public void setDroolsRuleEngineContexts(Set<DroolsRuleEngineContextModel> droolsRuleEngineContexts) {
        this.droolsRuleEngineContexts = droolsRuleEngineContexts;
    }
}
