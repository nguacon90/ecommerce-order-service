package com.vctek.orderservice.promotionengine.ruleengine.concurrency;

import java.util.concurrent.ThreadFactory;

public interface TaskContext {
    ThreadFactory getThreadFactory();

    int getNumberOfThreads();

    Long getThreadTimeout();
}
