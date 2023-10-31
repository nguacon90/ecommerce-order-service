package com.vctek.orderservice.promotionengine.ruleengine.service;


import com.vctek.orderservice.promotionengine.ruleengine.ExecutionContext;
import com.vctek.orderservice.promotionengine.ruleengine.RuleEvaluationContext;
import com.vctek.orderservice.promotionengine.ruleengine.RuleEvaluationResult;
import com.vctek.orderservice.promotionengine.ruleengine.init.InitializationFuture;
import com.vctek.orderservice.promotionengine.ruleengine.model.AbstractRuleModuleModel;

import java.util.List;

public interface RuleEngineService {
    RuleEvaluationResult evaluate(RuleEvaluationContext context);

    InitializationFuture initialize(List<AbstractRuleModuleModel> modules, boolean enableIncrementalUpdate,
                                    ExecutionContext executionContext);
}
