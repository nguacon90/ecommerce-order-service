package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.impl;

import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerProblem;

public class DefaultRuleCompilerProblem implements RuleCompilerProblem {
    private final Severity severity;
    private final String message;

    public DefaultRuleCompilerProblem(Severity severity, String message) {
        this.severity = severity;
        this.message = message;
    }

    @Override
    public Severity getSeverity() {
        return severity;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
