package com.vctek.orderservice.promotionengine.ruleengine.versioning.impl;

import com.google.common.base.Preconditions;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.versioning.ModuleVersionResolver;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DroolsKieModuleVersionResolver implements ModuleVersionResolver<DroolsKIEModuleModel> {
    public Optional<Long> getDeployedModuleVersion(DroolsKIEModuleModel rulesModule) {
        Preconditions.checkNotNull(rulesModule, "The instance of DroolsKIEModuleModel must not be null here");
        return Optional.ofNullable(rulesModule.getDeployedMvnVersion()).map((v) ->
                this.extractModuleVersion(rulesModule.getName(), v));
    }

    public Long extractModuleVersion(String moduleName, String deployedMvnVersion) {
        Long deployedModuleVersion = null;

        try {
            int idx = deployedMvnVersion.lastIndexOf(46);
            if (idx != -1 && deployedMvnVersion.length() > idx + 1) {
                deployedModuleVersion = Long.parseLong(deployedMvnVersion.substring(idx + 1).trim());
            }

            return deployedModuleVersion;
        } catch (RuntimeException var5) {
            throw new IllegalArgumentException("Error during the deployed version of module [" + moduleName + "] occurred: ", var5);
        }
    }
}