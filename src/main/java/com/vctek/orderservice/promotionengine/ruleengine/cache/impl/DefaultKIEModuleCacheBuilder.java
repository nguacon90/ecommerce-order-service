package com.vctek.orderservice.promotionengine.ruleengine.cache.impl;

import com.google.common.base.Preconditions;
import com.vctek.orderservice.promotionengine.droolsruleengineservice.util.Map2StringUtils;
import com.vctek.orderservice.promotionengine.ruleengine.cache.KIEModuleCacheBuilder;
import com.vctek.orderservice.promotionengine.ruleengine.cache.RuleGlobalsBeanProvider;
import com.vctek.orderservice.promotionengine.ruleengine.model.AbstractRuleEngineRuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEBaseModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.util.EngineRulePreconditions;
import com.vctek.orderservice.promotionengine.ruleengine.util.RuleMappings;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultKIEModuleCacheBuilder implements KIEModuleCacheBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultKIEModuleCacheBuilder.class);
    private final Map<Object, Map<String, Object>> globalsCache = new ConcurrentHashMap();
    private final Map<Object, DroolsRuleModel> duplicateGlobalsCheckMap = new ConcurrentHashMap();
    private DroolsKIEModuleModel kieModule;
    private boolean failOnBeanMismatches;
    private RuleGlobalsBeanProvider ruleGlobalsBeanProvider;

    public DefaultKIEModuleCacheBuilder(RuleGlobalsBeanProvider ruleGlobalsBeanProvider, DroolsKIEModuleModel kieModule,
                                        boolean failOnBeanMismatches) {
        Preconditions.checkArgument(kieModule != null, "kieModule must not be null");
        this.ruleGlobalsBeanProvider = ruleGlobalsBeanProvider;
        this.kieModule = kieModule;
        this.failOnBeanMismatches = failOnBeanMismatches;
    }

    @Override
    public <T extends AbstractRuleEngineRuleModel> void processRule(T rule) {
        EngineRulePreconditions.checkRuleHasKieModule(rule);
        DroolsRuleModel droolsRule = (DroolsRuleModel) rule;
        Preconditions.checkArgument(this.kieModule.getName().equals(RuleMappings.moduleName(droolsRule)),
                        "rule must have the same kie module as cache builder");
        Map<String, Object> kieBaseGlobals = this.getCachedGlobalsForKieBase(droolsRule.getKieBase());
        Object bean;
        String globalMap = droolsRule.getGlobals();
        Map<String, String> globals = Map2StringUtils.stringToMap(globalMap);
        if (MapUtils.isNotEmpty(globals)) {
            for (Iterator var5 = globals.entrySet().iterator(); var5.hasNext(); this.duplicateGlobalsCheckMap.put(bean, droolsRule)) {
                Map.Entry<String, String> entry = (Map.Entry) var5.next();
                bean = this.getRuleGlobalsBeanProvider().getRuleGlobals(entry.getValue());
                Object oldBean = kieBaseGlobals.put(entry.getKey(), bean);
                if (oldBean != null && !bean.equals(oldBean) && !bean.getClass().isAssignableFrom(oldBean.getClass())) {
                    DroolsRuleModel oldRule = this.duplicateGlobalsCheckMap.get(oldBean);
                    String errorMessage = MessageFormat.format("Error when registering global of type {4} for rule {0}. " +
                            "Bean for global {1} was already defined by rule {2} which added bean of type {3}." +
                            "\n Check your rules! Rule {2} might encounter runtime errors as it expects a global of type {3}",
                            rule.getCode(), entry.getKey(), oldRule == null ? "" : oldRule.getCode(),
                            oldBean.getClass().getName(), bean.getClass().getName());
                    LOGGER.error(errorMessage);
                    this.escalateOnBeanMismatchesIfNecessary(errorMessage);
                }
            }
        }

    }

    protected void escalateOnBeanMismatchesIfNecessary(String message) {
        if (this.failOnBeanMismatches) {
            throw new IllegalArgumentException(message);
        }
    }

    protected Map<String, Object> getCachedGlobalsForKieBase(DroolsKIEBaseModel kieBase) {
        return this.globalsCache.computeIfAbsent(kieBase.getId(), (k) -> new ConcurrentHashMap());
    }

    public Map<Object, Map<String, Object>> getGlobalsCache() {
        return this.globalsCache;
    }

    public DroolsKIEModuleModel getKieModule() {
        return this.kieModule;
    }

    protected RuleGlobalsBeanProvider getRuleGlobalsBeanProvider() {
        return this.ruleGlobalsBeanProvider;
    }
}
