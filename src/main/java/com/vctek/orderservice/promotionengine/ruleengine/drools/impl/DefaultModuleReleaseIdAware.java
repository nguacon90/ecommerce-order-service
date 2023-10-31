package com.vctek.orderservice.promotionengine.ruleengine.drools.impl;

import com.google.common.base.Preconditions;
import com.vctek.orderservice.promotionengine.ruleengine.RuleEvaluationContext;
import com.vctek.orderservice.promotionengine.ruleengine.drools.ModuleReleaseIdAware;
import com.vctek.orderservice.promotionengine.ruleengine.init.RuleEngineBootstrap;
import com.vctek.orderservice.promotionengine.ruleengine.model.*;
import org.drools.compiler.kproject.ReleaseIdImpl;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.rule.AgendaFilter;

import java.util.Objects;

public abstract class DefaultModuleReleaseIdAware implements ModuleReleaseIdAware {
    public static final String DUMMY_GROUP = "DUMMY_GROUP";
    public static final String DUMMY_ARTIFACT = "DUMMY_ARTIFACT";
    public static final String DUMMY_VERSION = "DUMMY_VERSION";
    protected RuleEngineBootstrap ruleEngineBootstrap;

    public DefaultModuleReleaseIdAware(RuleEngineBootstrap ruleEngineBootstrap) {
        this.ruleEngineBootstrap = ruleEngineBootstrap;
    }

    @Override
    public ReleaseId getDeployedKieModuleReleaseId(RuleEvaluationContext context) {
        DroolsRuleEngineContextModel ruleEngineContext = this.validateRuleEvaluationContext(context);
        DroolsKIESessionModel kieSession = ruleEngineContext.getKieSession();
        DroolsKIEBaseModel kieBase = kieSession.getDroolsKIEBase();
        DroolsKIEModuleModel kieModule = kieBase.getDroolsKIEModule();
        return this.ruleEngineBootstrap.getDeployedReleaseId(kieModule, null)
                .orElse(this.getDummyReleaseId(kieModule));
    }

    private ReleaseId getDummyReleaseId(DroolsKIEModuleModel module) {
        String groupId = module.getMvnGroupId();
        String artifactId = module.getMvnArtifactId();
        return new ReleaseIdImpl(Objects.nonNull(groupId) ? groupId : DUMMY_GROUP,
                Objects.nonNull(artifactId) ? artifactId : DUMMY_ARTIFACT, DUMMY_VERSION);
    }

    protected DroolsRuleEngineContextModel validateRuleEvaluationContext(RuleEvaluationContext context) {
        Preconditions.checkArgument(context != null, "rule evaluation context must not be null");
        AbstractRuleEngineContextModel abstractREContext = context.getRuleEngineContext();
        Preconditions.checkArgument(abstractREContext != null, "rule engine context must not be null");
        if (!(abstractREContext instanceof DroolsRuleEngineContextModel)) {
            throw new IllegalArgumentException("rule engine context " + abstractREContext.getName() + " must be of type DroolsRuleEngineContext. " +
                    abstractREContext.getName() + " is not supported.");
        } else {
            DroolsRuleEngineContextModel ruleEngineContext = (DroolsRuleEngineContextModel)abstractREContext;
            if (Objects.nonNull(context.getFilter()) && !(context.getFilter() instanceof AgendaFilter)) {
                throw new IllegalArgumentException("context.filter attribute must be of type org.kie.api.runtime.rule.AgendaFilter");
            } else {
                return ruleEngineContext;
            }
        }
    }
}
