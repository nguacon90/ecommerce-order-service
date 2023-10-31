package com.vctek.orderservice.promotionengine.ruleengine.exception;

public class RuleConverterException extends RuleEngineServiceException {
    public RuleConverterException() {
    }

    public RuleConverterException(String message) {
        super(message);
    }

    public RuleConverterException(Throwable cause) {
        super(cause);
    }

    public RuleConverterException(String message, Throwable cause) {
        super(message, cause);
    }
}

