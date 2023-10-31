package com.vctek.orderservice.promotionengine.ruleengine.cache;

import com.vctek.orderservice.promotionengine.ruleengine.model.AbstractRuleEngineRuleModel;

public interface KIEModuleCacheBuilder {
    <T extends AbstractRuleEngineRuleModel> void processRule(T rule);
}
