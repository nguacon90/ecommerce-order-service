package com.vctek.orderservice.promotionengine.ruleengineservice.evaluate;


import com.vctek.orderservice.promotionengine.ruleengine.model.eveluation.RuleActionContext;

public interface RAOAction {
    void performAction(RuleActionContext context);
}
