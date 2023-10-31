package com.vctek.orderservice.promotionengine.ruleengine.model;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "drool_rule_engine_context")
public class DroolsRuleEngineContextModel extends AbstractRuleEngineContextModel {
    @Column(name = "code")
    private String code;

    @Column(name = "rule_firing_limit")
    private Long ruleFiringLimit;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "drools_kie_session_id" )
    private DroolsKIESessionModel kieSession;

    public Long getRuleFiringLimit() {
        return ruleFiringLimit;
    }

    public void setRuleFiringLimit(Long ruleFiringLimit) {
        this.ruleFiringLimit = ruleFiringLimit;
    }

    public DroolsKIESessionModel getKieSession() {
        return kieSession;
    }

    public void setKieSession(DroolsKIESessionModel kieSession) {
        this.kieSession = kieSession;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DroolsRuleEngineContextModel)) return false;
        DroolsRuleEngineContextModel that = (DroolsRuleEngineContextModel) o;
        if(this.getId() == null && that.getId() == null) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
