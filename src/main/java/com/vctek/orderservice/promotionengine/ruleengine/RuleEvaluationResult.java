package com.vctek.orderservice.promotionengine.ruleengine;


import com.vctek.orderservice.promotionengine.ruleengineservice.rao.RuleEngineResultRAO;

import java.util.Set;

public class RuleEvaluationResult {
    private boolean evaluationFailed;
    private Set<Object> facts;
    private String errorMessage;
    private Object executionResult;
    private RuleEngineResultRAO result;

    public boolean isEvaluationFailed() {
        return evaluationFailed;
    }

    public void setEvaluationFailed(boolean evaluationFailed) {
        this.evaluationFailed = evaluationFailed;
    }

    public Set<Object> getFacts() {
        return facts;
    }

    public void setFacts(Set<Object> facts) {
        this.facts = facts;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Object getExecutionResult() {
        return executionResult;
    }

    public void setExecutionResult(Object executionResult) {
        this.executionResult = executionResult;
    }

    public RuleEngineResultRAO getResult() {
        return result;
    }

    public void setResult(RuleEngineResultRAO result) {
        this.result = result;
    }
}
