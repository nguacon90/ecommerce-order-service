package com.vctek.orderservice.promotionengine.ruleengine.concurrency;

import org.apache.commons.collections.CollectionUtils;

import java.util.Objects;
import java.util.Set;

public interface TaskExecutionFuture <T extends TaskResult> {
    T getTaskResult();

    long getWorkerPreDestroyTimeout();

    void waitForTasksToFinish();

    default void waitForTasksToFinish(Set<Thread> workers) {
        if (CollectionUtils.isNotEmpty(workers)) {
            workers.forEach(this::waitWhileWorkerIsRunning);
        }

    }

    default void waitWhileWorkerIsRunning(Thread worker) {
        if (Objects.nonNull(worker) && worker.isAlive()) {
            try {
                worker.join(this.getWorkerPreDestroyTimeout());
            } catch (InterruptedException var3) {
                Thread.currentThread().interrupt();
                throw new RuleEngineTaskExecutionException("Interrupted exception is caught during rules compilation and publishing:", var3);
            }
        }

    }
}
