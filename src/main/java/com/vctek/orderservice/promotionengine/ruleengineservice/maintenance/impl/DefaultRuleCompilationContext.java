package com.vctek.orderservice.promotionengine.ruleengineservice.maintenance.impl;

import com.vctek.orderservice.promotionengine.ruleengine.model.AbstractRuleModuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.repository.DroolsKIEModuleRepository;
import com.vctek.orderservice.promotionengine.ruleengineservice.maintenance.RuleCompilationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DefaultRuleCompilationContext implements RuleCompilationContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRuleCompilationContext.class);
    private DroolsKIEModuleRepository droolsKIEModuleRepository;
    private Map<String, AtomicLong> ruleVersionForModules;

    public AtomicLong resetRuleEngineRuleVersion(String moduleName) {
        AbstractRuleModuleModel moduleModel = droolsKIEModuleRepository.findByName(moduleName);
        LOGGER.debug("Resetting the module version to [{}]", moduleModel.getVersion());
        AtomicLong initVal = new AtomicLong(moduleModel.getVersion() + 1L);
        if (Objects.nonNull(this.ruleVersionForModules.putIfAbsent(moduleName, initVal))) {
            this.ruleVersionForModules.replace(moduleName, initVal);
        }

        synchronized(this) {
            AtomicLong moduleVersion = this.ruleVersionForModules.get(moduleName);
            moduleVersion.set(moduleModel.getVersion() + 1L);
            return moduleVersion;
        }
    }

    @PostConstruct
    public void setUp() {
        this.ruleVersionForModules = new ConcurrentHashMap(3, 0.75f, 2);
    }

    @Autowired
    public void setDroolsKIEModuleRepository(DroolsKIEModuleRepository droolsKIEModuleRepository) {
        this.droolsKIEModuleRepository = droolsKIEModuleRepository;
    }

    @Override
    public Long getNextRuleEngineRuleVersion(String moduleName) {
        AtomicLong moduleVersion = this.ruleVersionForModules.get(moduleName);
        if (Objects.isNull(moduleVersion)) {
            moduleVersion = this.resetRuleEngineRuleVersion(moduleName);
        }

        return moduleVersion.getAndAdd(1L);
    }
}
