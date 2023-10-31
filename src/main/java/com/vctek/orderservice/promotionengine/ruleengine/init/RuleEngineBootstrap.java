package com.vctek.orderservice.promotionengine.ruleengine.init;

import com.vctek.orderservice.promotionengine.ruleengine.KieContainerListener;
import com.vctek.orderservice.promotionengine.ruleengine.RuleEngineActionResult;
import com.vctek.orderservice.promotionengine.ruleengine.cache.KIEModuleCacheBuilder;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public interface RuleEngineBootstrap {
    KieServices getEngineServices();

    RuleEngineActionResult startup(String moduleName);

    RuleEngineActionResult startup(DroolsKIEModuleModel module);

    Optional<ReleaseId> getDeployedReleaseId(DroolsKIEModuleModel module, String deployedMvnVersion);

    void warmUpRuleEngineContainer(DroolsKIEModuleModel var1, KieContainer var2);

    void activateNewRuleEngineContainer(KieContainer container, KIEModuleCacheBuilder cacheBuilder,
                                        RuleEngineActionResult result, DroolsKIEModuleModel module, String deployedReleaseVersion);

    void waitForSwappingToFinish();

    void switchKieModuleAsync(String moduleName, KieContainerListener listener, List<Object> resultAccumulator,
                              Supplier resetFlagSupplier, LinkedList<Supplier<Object>> postTaskList,
                              boolean enableIncrementalUpdate, RuleEngineActionResult result);
}
