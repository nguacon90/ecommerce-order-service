package com.vctek.orderservice.promotionengine.ruleengine.init.impl;

import com.vctek.orderservice.promotionengine.ruleengine.RuleEngineActionResult;
import com.vctek.orderservice.promotionengine.ruleengine.concurrency.TaskResult;
import com.vctek.orderservice.promotionengine.ruleengine.concurrency.TaskResultState;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

public class RuleDeploymentTaskResult implements TaskResult {
    private final List<RuleEngineActionResult> rulePublishingResults;

    public RuleDeploymentTaskResult(List<RuleEngineActionResult> rulePublishingResults) {
        this.rulePublishingResults = rulePublishingResults;
    }

    public List<RuleEngineActionResult> getRulePublishingResults() {
        return this.rulePublishingResults;
    }

    public TaskResultState getState() {
        TaskResultState state = TaskResultState.SUCCESS;
        List<RuleEngineActionResult> results = this.getRulePublishingResults();
        if (CollectionUtils.isNotEmpty(results) && results.stream().anyMatch(RuleEngineActionResult::isActionFailed)) {
            state = TaskResultState.FAILURE;
        }

        return state;
    }
}