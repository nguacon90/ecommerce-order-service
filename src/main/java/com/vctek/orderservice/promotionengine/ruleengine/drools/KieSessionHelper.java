package com.vctek.orderservice.promotionengine.ruleengine.drools;

import com.vctek.orderservice.promotionengine.ruleengine.RuleEvaluationContext;
import org.kie.api.runtime.KieContainer;

public interface KieSessionHelper<T> extends ModuleReleaseIdAware {
    T initializeSession(Class<T> var1, RuleEvaluationContext var2, KieContainer var3);
}
