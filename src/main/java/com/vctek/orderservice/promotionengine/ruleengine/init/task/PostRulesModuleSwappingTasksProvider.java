package com.vctek.orderservice.promotionengine.ruleengine.init.task;

import com.vctek.orderservice.promotionengine.ruleengine.RuleEngineActionResult;

import java.util.List;
import java.util.function.Supplier;

public interface PostRulesModuleSwappingTasksProvider {
    List<Supplier<Object>> getTasks(RuleEngineActionResult result);
}
