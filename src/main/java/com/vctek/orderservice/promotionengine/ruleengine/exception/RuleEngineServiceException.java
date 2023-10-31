package com.vctek.orderservice.promotionengine.ruleengine.exception;

public class RuleEngineServiceException extends RuntimeException {
    public RuleEngineServiceException() {
    }

    public RuleEngineServiceException(String message) {
        super(message);
    }

    public RuleEngineServiceException(Throwable cause) {
        super(cause);
    }

    public RuleEngineServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
