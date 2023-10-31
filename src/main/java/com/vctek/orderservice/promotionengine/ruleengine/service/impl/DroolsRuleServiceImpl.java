package com.vctek.orderservice.promotionengine.ruleengine.service.impl;

import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.repository.DroolsKIEModuleRepository;
import com.vctek.orderservice.promotionengine.ruleengine.repository.DroolsRuleRepository;
import com.vctek.orderservice.promotionengine.ruleengine.service.DroolsRuleService;
import com.vctek.orderservice.promotionengine.ruleengine.versioning.ModuleVersionResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class DroolsRuleServiceImpl implements DroolsRuleService {
    private DroolsRuleRepository droolsRuleRepository;
    private DroolsKIEModuleRepository droolsKIEModuleRepository;
    private ModuleVersionResolver<DroolsKIEModuleModel> moduleVersionResolver;

    public DroolsRuleServiceImpl(DroolsRuleRepository droolsRuleRepository) {
        this.droolsRuleRepository = droolsRuleRepository;
    }

    @Override
    public DroolsRuleModel getRuleForCodeAndModule(String code, String moduleName) {
        return droolsRuleRepository.findByCodeAndModuleName(code, moduleName);
    }

    @Override
    public DroolsRuleModel save(DroolsRuleModel droolsRule) {
        DroolsKIEModuleModel droolsKIEModule = droolsRule.getKieBase().getDroolsKIEModule();
        Long moduleVersion = droolsKIEModule.getVersion() == null ? 0 : droolsKIEModule.getVersion();
        Long engineVersion = droolsRule.getVersion() == null ? 0 : droolsRule.getVersion();
        if(moduleVersion <= engineVersion) {
            droolsKIEModule.setVersion(engineVersion);
            droolsKIEModuleRepository.save(droolsKIEModule);
        }

        return droolsRuleRepository.save(droolsRule);
    }

    @Override
    public DroolsRuleModel findByCodeAndModuleNameAndActive(String code, String moduleName, boolean active) {
        return droolsRuleRepository.findByCodeAndModuleNameAndActive(code, moduleName, active);
    }

    @Override
    public List<DroolsRuleModel> getDeployedEngineRulesForModule(String moduleName) {
        DroolsKIEModuleModel kieModuleModel = droolsKIEModuleRepository.findByName(moduleName);
        Optional<Long> deployedModuleVersion = moduleVersionResolver.getDeployedModuleVersion(kieModuleModel);
        if(deployedModuleVersion.isPresent()) {
            List<DroolsRuleModel> activeRulesForVersion = droolsRuleRepository.getActiveRulesForVersion(
                    moduleName, deployedModuleVersion.get());
            return activeRulesForVersion == null ? Collections.emptyList() : activeRulesForVersion;
        }
        return Collections.emptyList();
    }

    @Override
    public long countDeployedEngineRulesForModule(String moduleName) {
        return (long)this.getDeployedEngineRulesForModule(moduleName).size();
    }

    @Override
    public List<DroolsRuleModel> getRulesByUuids(List<String> ruleUuids) {
        return droolsRuleRepository.findByUuidIn(ruleUuids);
    }

    @Override
    public List<DroolsRuleModel> getRulesForVersion(String moduleName, Long deployedVersion) {
        return droolsRuleRepository.getRulesForVersion(moduleName, deployedVersion);
    }

    @Override
    public DroolsRuleModel findByCodeAndActive(String firedRuleCode, boolean active) {
        return droolsRuleRepository.findByCodeAndActive(firedRuleCode, active);
    }

    @Autowired
    public void setDroolsKIEModuleRepository(DroolsKIEModuleRepository droolsKIEModuleRepository) {
        this.droolsKIEModuleRepository = droolsKIEModuleRepository;
    }

    @Autowired
    public void setModuleVersionResolver(ModuleVersionResolver<DroolsKIEModuleModel> moduleVersionResolver) {
        this.moduleVersionResolver = moduleVersionResolver;
    }
}
