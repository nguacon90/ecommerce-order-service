package com.vctek.orderservice.promotionengine.ruleengine.concurrency.impl;

import com.vctek.orderservice.promotionengine.ruleengine.concurrency.TaskContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadFactory;

@Component
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DefaultTaskContext implements TaskContext {

    private ThreadFactory threadFactory;
    private Long threadTimeout;

    @Override
    public ThreadFactory getThreadFactory() {
        return this.threadFactory;
    }

    @Override
    public int getNumberOfThreads() {
        return Runtime.getRuntime().availableProcessors() + 1;
    }

    @Override
    public Long getThreadTimeout() {
        return threadTimeout == null ? 3600000L : threadTimeout;
    }

    @Autowired
    @Qualifier("defaultAwareThreadFactory")
    public void setThreadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    @Value("${vctek.config.ruleengine.task.predestroytimeout:3600000}")
    public void setThreadTimeout(Long threadTimeout) {
        this.threadTimeout = threadTimeout;
    }
}
