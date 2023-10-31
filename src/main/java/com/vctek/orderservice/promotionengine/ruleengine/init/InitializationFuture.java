package com.vctek.orderservice.promotionengine.ruleengine.init;

import com.google.common.collect.Lists;
import com.vctek.orderservice.promotionengine.ruleengine.RuleEngineActionResult;

import java.util.List;

public class InitializationFuture {
    private RuleEngineBootstrap ruleEngineBootstrap;
    private List<RuleEngineActionResult> results;

    public InitializationFuture(RuleEngineBootstrap ruleEngineBootstrap) {
        this.ruleEngineBootstrap = ruleEngineBootstrap;
        this.results = Lists.newCopyOnWriteArrayList();
    }

    public static InitializationFuture of(RuleEngineBootstrap ruleEngineBootstrap) {
        return new InitializationFuture(ruleEngineBootstrap);
    }

    public InitializationFuture waitForInitializationToFinish() {
        this.ruleEngineBootstrap.waitForSwappingToFinish();
        return this;
    }

    public List<RuleEngineActionResult> getResults() {
        return results;
    }

    public void setResults(List<RuleEngineActionResult> results) {
        this.results = results;
    }

    public RuleEngineBootstrap getRuleEngineBootstrap() {
        return ruleEngineBootstrap;
    }
}
