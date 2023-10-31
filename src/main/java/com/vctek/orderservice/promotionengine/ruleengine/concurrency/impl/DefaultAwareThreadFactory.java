package com.vctek.orderservice.promotionengine.ruleengine.concurrency.impl;

import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadFactory;

@Component("defaultAwareThreadFactory")
public class DefaultAwareThreadFactory implements ThreadFactory {

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r);
    }
}
