package com.vctek.orderservice.promotionengine.ruleengineservice.compiler;

import java.io.Serializable;

public interface RuleCompilerProblem extends Serializable {
    RuleCompilerProblem.Severity getSeverity();

    String getMessage();

    enum Severity {
        WARNING,
        ERROR;

        Severity() {
        }
    }
}
