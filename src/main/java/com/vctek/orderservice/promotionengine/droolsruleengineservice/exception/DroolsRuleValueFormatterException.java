package com.vctek.orderservice.promotionengine.droolsruleengineservice.exception;

public class DroolsRuleValueFormatterException extends RuntimeException {
    public DroolsRuleValueFormatterException() {
    }

    public DroolsRuleValueFormatterException(String message) {
        super(message);
    }

    public DroolsRuleValueFormatterException(Throwable cause) {
        super(cause);
    }

    public DroolsRuleValueFormatterException(String message, Throwable cause) {
        super(message, cause);
    }
}
