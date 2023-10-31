package com.vctek.orderservice.promotionengine.ruleengine.concurrency;

public class RuleEngineTaskExecutionException extends RuntimeException {
    public RuleEngineTaskExecutionException() {
    }

    public RuleEngineTaskExecutionException(String message) {
        super(message);
    }

    public RuleEngineTaskExecutionException(Throwable cause) {
        super(cause);
    }

    public RuleEngineTaskExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
