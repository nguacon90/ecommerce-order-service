package com.vctek.orderservice.promotionengine.ruleengine.service;


import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;

import java.util.List;

public interface DroolsRuleService {
    DroolsRuleModel getRuleForCodeAndModule(String code, String moduleName);

    DroolsRuleModel save(DroolsRuleModel droolsRule);

    DroolsRuleModel findByCodeAndModuleNameAndActive(String code, String moduleName, boolean active);

    List<DroolsRuleModel> getDeployedEngineRulesForModule(String moduleName);

    long countDeployedEngineRulesForModule(String moduleName);

    List<DroolsRuleModel> getRulesByUuids(List<String> ruleUuids);

    List<DroolsRuleModel> getRulesForVersion(String moduleName, Long deployedVersion);

    DroolsRuleModel findByCodeAndActive(String firedRuleCode, boolean active);
}
