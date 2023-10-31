package com.vctek.orderservice.promotionengine.ruleengine.versioning;

import com.vctek.orderservice.promotionengine.ruleengine.model.AbstractRuleModuleModel;

import java.util.Optional;

public interface ModuleVersionResolver<T extends AbstractRuleModuleModel> {
    Optional<Long> getDeployedModuleVersion(T rulesModule);

    Long extractModuleVersion(String moduleName, String deployedMvnVersion);
}
