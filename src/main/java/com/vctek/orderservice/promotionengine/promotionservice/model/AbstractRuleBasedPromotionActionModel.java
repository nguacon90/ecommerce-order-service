package com.vctek.orderservice.promotionengine.promotionservice.model;


import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;

import javax.persistence.*;

@MappedSuperclass
public class AbstractRuleBasedPromotionActionModel extends AbstractPromotionActionModel {

    @Column(name = "strategy_id")
    protected String strategyId;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "drools_rule_id")
    protected DroolsRuleModel rule;

    public String getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(String strategyId) {
        this.strategyId = strategyId;
    }

    public DroolsRuleModel getRule() {
        return rule;
    }

    public void setRule(DroolsRuleModel rule) {
        this.rule = rule;
    }
}
