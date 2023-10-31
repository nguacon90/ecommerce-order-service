package com.vctek.orderservice.promotionengine.ruleengine.cache.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.vctek.orderservice.promotionengine.ruleengine.RuleEvaluationContext;
import com.vctek.orderservice.promotionengine.ruleengine.cache.KIEModuleCacheBuilder;
import com.vctek.orderservice.promotionengine.ruleengine.cache.RuleEngineCache;
import com.vctek.orderservice.promotionengine.ruleengine.cache.RuleEngineCacheService;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEBaseModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleEngineContextModel;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service("defaultRuleEngineCacheService")
public class DefaultRuleEngineCacheService implements RuleEngineCacheService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRuleEngineCacheService.class);
    protected RuleEngineCache ruleEngineCache;

    public DefaultRuleEngineCacheService(@Qualifier("defaultRuleEngineCache") RuleEngineCache ruleEngineCache) {
        this.ruleEngineCache = ruleEngineCache;
    }

    @Override
    public KIEModuleCacheBuilder createKIEModuleCacheBuilder(DroolsKIEModuleModel module) {
        return this.ruleEngineCache.createKIEModuleCacheBuilder(module);
    }

    @Override
    public void addToCache(KIEModuleCacheBuilder builder) {
        this.ruleEngineCache.addKIEModuleCache(builder);
    }

    @Override
    public void provideCachedEntities(RuleEvaluationContext context) {
        Preconditions.checkArgument(context != null, "context must not be null");
        Preconditions.checkArgument(context.getRuleEngineContext() instanceof DroolsRuleEngineContextModel,
                "rule engine context must be of type DroolsRuleEngineContext");
        DroolsRuleEngineContextModel engineContext = (DroolsRuleEngineContextModel) context.getRuleEngineContext();
        DroolsKIEBaseModel droolsKIEBase = engineContext.getKieSession().getDroolsKIEBase();
        Preconditions.checkArgument(droolsKIEBase != null,
                "rule engine context must have a kie session and kie base set");
        Map<String, Object> globalsForKIEBase = this.ruleEngineCache.getGlobalsForKIEBase(droolsKIEBase);
        Map<String, Object> globals = Maps.newHashMap();
        if (MapUtils.isNotEmpty(globalsForKIEBase)) {
            globals = Maps.newHashMap(globalsForKIEBase);
        } else {
            LOGGER.warn("Globals map for evaluation context [{}] is empty. Rules won't be evaluated!", context.getRuleEngineContext().getName());
        }

        context.setGlobals(globals);
    }
}
