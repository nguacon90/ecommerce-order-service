package com.vctek.orderservice.promotionengine.ruleengine.init.impl;

import com.vctek.orderservice.promotionengine.ruleengine.RuleEngineActionResult;
import com.vctek.orderservice.promotionengine.ruleengine.concurrency.TaskResult;
import com.vctek.orderservice.promotionengine.ruleengine.concurrency.impl.DefaultTaskExecutionFuture;
import org.kie.api.builder.KieBuilder;

import java.util.List;
import java.util.Set;

public class RulePublishingFuture  extends DefaultTaskExecutionFuture {
    private final List<RuleEngineActionResult> rulePublishingResults;
    private final List<KieBuilder> partialKieBuilders;
    private final long workerPreDestroyTimeout;

    public RulePublishingFuture(Set<Thread> workers, List<RuleEngineActionResult> rulePublishingResults,
                                List<KieBuilder> partialKieBuilders, long workerPreDestroyTimeout) {
        super(workers);
        this.rulePublishingResults = rulePublishingResults;
        this.workerPreDestroyTimeout = workerPreDestroyTimeout;
        this.partialKieBuilders = partialKieBuilders;
    }

    public List<KieBuilder> getPartialKieBuilders() {
        return this.partialKieBuilders;
    }

    @Override
    public TaskResult getTaskResult() {
        this.waitForTasksToFinish();
        return new RuleDeploymentTaskResult(this.rulePublishingResults);
    }

    @Override
    public long getWorkerPreDestroyTimeout() {
        return this.workerPreDestroyTimeout;
    }
}
