package com.vctek.orderservice.promotionengine.ruleengine.concurrency.impl;

import com.vctek.orderservice.promotionengine.ruleengine.concurrency.TaskExecutionFuture;
import com.vctek.orderservice.promotionengine.ruleengine.concurrency.TaskResult;
import com.vctek.orderservice.promotionengine.ruleengine.concurrency.TaskResultState;

import java.util.Set;

public class DefaultTaskExecutionFuture implements TaskExecutionFuture<TaskResult> {

    private static final long DEFAULT_PRE_DESTROY_TOUT = 1000L;
    private Set<Thread> workers;
    private long predestroyTimeout;

    public DefaultTaskExecutionFuture(Set<Thread> workers) {
        this(workers, -1L);
    }

    public DefaultTaskExecutionFuture(Set<Thread> workers, long predestroyTimeout) {
        this.workers = workers;
        this.predestroyTimeout = predestroyTimeout;
    }

    @Override
    public TaskResult getTaskResult() {
        return this.workers.stream().anyMatch(Thread::isAlive) ? () -> TaskResultState.IN_PROGRESS :
                () -> TaskResultState.SUCCESS;
    }

    @Override
    public long getWorkerPreDestroyTimeout() {
        return this.predestroyTimeout == -1L ? DEFAULT_PRE_DESTROY_TOUT : this.predestroyTimeout;
    }

    @Override
    public void waitForTasksToFinish() {
        this.waitForTasksToFinish(this.workers);
    }
}
