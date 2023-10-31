package com.vctek.orderservice.promotionengine.ruleengineservice.maintenance;

import com.vctek.orderservice.promotionengine.ruleengine.model.AbstractRuleEngineRuleModel;

import java.util.List;

public interface RuleMaintenanceService {
    <T extends AbstractRuleEngineRuleModel> RulePublisherResult publishDroolsRules(List<T> rules, String moduleName,
                                                               boolean enableIncrementalUpdate, boolean isBlocking);
}
