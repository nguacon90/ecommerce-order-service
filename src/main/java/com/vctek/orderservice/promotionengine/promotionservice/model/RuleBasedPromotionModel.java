package com.vctek.orderservice.promotionengine.promotionservice.model;


import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;

import javax.persistence.*;

@Entity
@DiscriminatorValue("RuleBasedPromotionModel")
public class RuleBasedPromotionModel extends AbstractPromotionModel {

    @Column(name = "message_fired")
    private String messageFired;

    @Column(name = "promotion_description")
    private String promotionDescription;

    @Column(name = "rule_version")
    private Long ruleVersion;

    @OneToOne(mappedBy = "promotion", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private DroolsRuleModel rule;

    public String getMessageFired() {
        return messageFired;
    }

    public void setMessageFired(String messageFired) {
        this.messageFired = messageFired;
    }

    public String getPromotionDescription() {
        return promotionDescription;
    }

    public void setPromotionDescription(String promotionDescription) {
        this.promotionDescription = promotionDescription;
    }

    public Long getRuleVersion() {
        return ruleVersion;
    }

    public void setRuleVersion(Long ruleVersion) {
        this.ruleVersion = ruleVersion;
    }

    public DroolsRuleModel getRule() {
        return rule;
    }

    public void setRule(DroolsRuleModel rule) {
        this.rule = rule;
    }
}
