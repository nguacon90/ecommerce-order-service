package com.vctek.orderservice.promotionengine.ruleengine.model;


import com.vctek.orderservice.model.ItemModel;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "drools_kie_base")
public class DroolsKIEBaseModel extends ItemModel {

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "droolsKIEBase")
    private Set<DroolsKIESessionModel> kieSessions;

    @OneToMany(mappedBy = "kieBase")
    private Set<DroolsRuleModel> rules;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "drools_kie_module_id")
    private DroolsKIEModuleModel droolsKIEModule;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "default_drools_kie_session_id")
    private DroolsKIESessionModel defaultKieSession;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<DroolsRuleModel> getRules() {
        return rules;
    }

    public void setRules(Set<DroolsRuleModel> rules) {
        this.rules = rules;
    }

    public Set<DroolsKIESessionModel> getKieSessions() {
        return kieSessions;
    }

    public void setKieSessions(Set<DroolsKIESessionModel> kieSessions) {
        this.kieSessions = kieSessions;
    }

    public DroolsKIEModuleModel getDroolsKIEModule() {
        return droolsKIEModule;
    }

    public void setDroolsKIEModule(DroolsKIEModuleModel droolsKIEModule) {
        this.droolsKIEModule = droolsKIEModule;
    }

    public DroolsKIESessionModel getDefaultKieSession() {
        return defaultKieSession;
    }

    public void setDefaultKieSession(DroolsKIESessionModel defaultKieSession) {
        this.defaultKieSession = defaultKieSession;
    }
}
