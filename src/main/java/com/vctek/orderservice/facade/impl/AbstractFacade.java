package com.vctek.orderservice.facade.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class AbstractFacade {

    protected void shutdownExecutorService(ExecutorService executorService) {
        executorService.shutdown();
        try {
            if(!executorService.awaitTermination(4, TimeUnit.HOURS)) {
                executorService.shutdown();
            }
        } catch (InterruptedException e) {
            executorService.shutdown();
            Thread.currentThread().interrupt();
        }
    }
}
