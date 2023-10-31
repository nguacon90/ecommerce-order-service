package com.vctek.orderservice.promotionengine.ruleengine.drools;

import com.vctek.orderservice.promotionengine.ruleengine.RuleEvaluationContext;
import org.kie.api.builder.ReleaseId;

public interface ModuleReleaseIdAware {
    ReleaseId getDeployedKieModuleReleaseId(RuleEvaluationContext context);
}
