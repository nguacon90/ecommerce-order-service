package com.vctek.orderservice.promotionengine.ruleengine.model;


import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.RuleBasedPromotionModel;

import javax.persistence.*;

@Entity
@Table(name = "drools_rule")
public class DroolsRuleModel extends AbstractRuleEngineRuleModel {

    @Column(name = "rule_package")
    private String rulePackage;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "drools_kie_base_id" )
    private DroolsKIEBaseModel kieBase;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "promotion_source_rule_id" )
    private PromotionSourceRuleModel promotionSourceRule;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private RuleBasedPromotionModel promotion;

    private String globals;

    public String getRulePackage() {
        return rulePackage;
    }

    public void setRulePackage(String rulePackage) {
        this.rulePackage = rulePackage;
    }

    public DroolsKIEBaseModel getKieBase() {
        return kieBase;
    }

    public void setKieBase(DroolsKIEBaseModel kieBase) {
        this.kieBase = kieBase;
    }

    public String getGlobals() {
        return globals;
    }

    public void setGlobals(String globals) {
        this.globals = globals;
    }

    public PromotionSourceRuleModel getPromotionSourceRule() {
        return promotionSourceRule;
    }

    public void setPromotionSourceRule(PromotionSourceRuleModel promotionSourceRule) {
        this.promotionSourceRule = promotionSourceRule;
    }

    public RuleBasedPromotionModel getPromotion() {
        return promotion;
    }

    public void setPromotion(RuleBasedPromotionModel promotion) {
        this.promotion = promotion;
    }
}
