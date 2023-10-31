package com.vctek.orderservice.promotionengine.ruleengineservice.maintenance;

import com.vctek.orderservice.promotionengine.ruleengine.RuleEngineActionResult;

import java.util.Collections;
import java.util.List;

public class RulePublisherResult {
    private final List<RuleEngineActionResult> publisherResults;
    public static final RulePublisherResult SUCCESS = new RulePublisherResult(PublishResult.SUCCESS,
            Collections.emptyList());

    private PublishResult result;

    public RulePublisherResult(PublishResult result, List<RuleEngineActionResult> publisherResults) {
        this.publisherResults = publisherResults;
        this.result = result;
    }

    public PublishResult getResult() {
        return result;
    }

    public void setResult(PublishResult result) {
        this.result = result;
    }

    public List<RuleEngineActionResult> getPublisherResults() {
        return publisherResults;
    }
}
