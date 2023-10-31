package com.vctek.orderservice.promotionengine.ruleengine.init.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.vctek.orderservice.promotionengine.ruleengine.init.ContentMatchRulesFilter;
import com.vctek.orderservice.promotionengine.ruleengine.model.AbstractRuleEngineRuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.service.DroolsRuleService;
import com.vctek.orderservice.promotionengine.ruleengine.util.EngineRulePreconditions;
import com.vctek.orderservice.promotionengine.ruleengine.util.RuleMappings;
import com.vctek.orderservice.promotionengine.ruleengine.versioning.ModuleVersionResolver;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class DefaultContentMatchRulesFilter implements ContentMatchRulesFilter {
    private ModuleVersionResolver<DroolsKIEModuleModel> moduleVersionResolver;
    private DroolsRuleService droolsRuleService;

    @Override
    public Pair<Collection<DroolsRuleModel>, Collection<DroolsRuleModel>> apply(List<DroolsRuleModel> rules) {
        return this.apply(rules, null);
    }

    @Override
    public Pair<Collection<DroolsRuleModel>, Collection<DroolsRuleModel>> apply(List<DroolsRuleModel> rules, Long newModuleVersion) {
        if (CollectionUtils.isNotEmpty(rules)) {
            Optional<DroolsKIEModuleModel> kieModuleModelOptional = this.verifyTheRulesModuleIsSame(rules);
            if (kieModuleModelOptional.isPresent()) {
                DroolsKIEModuleModel module = kieModuleModelOptional.get();
                Optional<Long> moduleDeployedVersion = this.moduleVersionResolver.getDeployedModuleVersion(module);
                if (moduleDeployedVersion.isPresent()) {
                    List<DroolsRuleModel> deployedRuleset = this.droolsRuleService.getDeployedEngineRulesForModule(module.getName());
                    Collection<DroolsRuleModel> ruleSetToDeploy = this.getRuleSetWithMaxVersion(rules, newModuleVersion);
                    Set<DroolsRuleModel> ruleSetToAdd = Sets.newHashSet(ruleSetToDeploy);
                    ruleSetToAdd.removeAll(deployedRuleset);
                    Set<DroolsRuleModel> ruleSetToRemove = Sets.newHashSet(deployedRuleset);
                    ruleSetToRemove.removeAll(ruleSetToDeploy);
                    return ImmutablePair.of(ruleSetToAdd, ruleSetToRemove);
                }
            }
        }
        return ImmutablePair.of(rules, Lists.newArrayList());
    }

    protected Optional<DroolsKIEModuleModel> verifyTheRulesModuleIsSame(List<DroolsRuleModel> droolRules) {
        if (CollectionUtils.isEmpty(droolRules)) {
            return Optional.empty();
        }
        DroolsRuleModel firstDroolsRule = droolRules.iterator().next();
        EngineRulePreconditions.checkRuleHasKieModule(firstDroolsRule);
        DroolsKIEModuleModel kieModule = firstDroolsRule.getKieBase().getDroolsKIEModule();
        String kieModuleName = kieModule.getName();
        if (Objects.isNull(kieModuleName)) {
            throw new IllegalStateException("The KIE module cannot have the empty name");
        }

        if (droolRules.stream().anyMatch((r) -> !RuleMappings.module(r).getName().equals(kieModuleName))) {
            throw new IllegalStateException("All the rules in the collection should have the same DroolsKIEModuleModel [" + kieModuleName + "]");
        }
        return Optional.of(kieModule);
    }

    protected Collection<DroolsRuleModel> getRuleSetWithMaxVersion(Collection<DroolsRuleModel> rulesByUuids, Long version) {
        Long maxVersion = version;
        if (Objects.isNull(version)) {
            maxVersion = 9223372036854775807L;
        }

        Map<String, DroolsRuleModel> rulesByCodeMap = Maps.newHashMap();
        List<DroolsRuleModel> activeDroolRules = rulesByUuids.stream().filter(AbstractRuleEngineRuleModel::isActive).collect(Collectors.toList());
        Iterator var7 = activeDroolRules.iterator();

        while (var7.hasNext()) {
            DroolsRuleModel ruleByUuid = (DroolsRuleModel) var7.next();
            String code = ruleByUuid.getCode();
            if (rulesByCodeMap.containsKey(code)) {
                DroolsRuleModel ruleForCode = rulesByCodeMap.get(code);
                Long ruleVersion = ruleByUuid.getVersion();
                if (ruleVersion > ruleForCode.getVersion() && ruleVersion <= maxVersion) {
                    rulesByCodeMap.replace(code, ruleByUuid);
                }
            } else if (ruleByUuid.getVersion() <= maxVersion) {
                rulesByCodeMap.put(code, ruleByUuid);
            }
        }

        return rulesByCodeMap.values();
    }

    @Autowired
    public void setModuleVersionResolver(ModuleVersionResolver<DroolsKIEModuleModel> moduleVersionResolver) {
        this.moduleVersionResolver = moduleVersionResolver;
    }

    @Autowired
    public void setDroolsRuleService(DroolsRuleService droolsRuleService) {
        this.droolsRuleService = droolsRuleService;
    }
}
