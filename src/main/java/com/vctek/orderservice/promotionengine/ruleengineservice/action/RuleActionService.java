package com.vctek.orderservice.promotionengine.ruleengineservice.action;


import com.vctek.orderservice.model.ItemModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.RuleEngineResultRAO;

import java.util.List;

public interface RuleActionService {
    List<ItemModel> applyAllActions(RuleEngineResultRAO ruleEngineResultRAO);
}
