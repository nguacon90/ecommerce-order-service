package com.vctek.orderservice.promotionengine.ruleengineservice.strategy;


import com.vctek.orderservice.model.ItemModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.AbstractRuleActionRAO;

import java.util.List;

public interface RuleActionStrategy<T> {
    List<T> apply(AbstractRuleActionRAO ruleActionRAO);

    String getStrategyId();

    void undo(ItemModel model);
}
