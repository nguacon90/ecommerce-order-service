package com.vctek.orderservice.promotionengine.ruleengine.exception;

import org.kie.api.definition.rule.Rule;

import java.util.*;
import java.util.stream.Collectors;

public class DroolsRuleLoopException extends RuleEngineRuntimeException {
    private final long limit;
    private final transient Map<Rule, Long> ruleMap;

    public DroolsRuleLoopException(long limit, Map<Rule, Long> ruleMap) {
        this.limit = limit;
        this.ruleMap = ruleMap;
    }

    public long getLimit() {
        return this.limit;
    }

    public List<String> getAllRuleFirings() {
        return this.getRuleFirings(9223372036854775807L);
    }

    public List<String> getRuleFirings(long size) {
        return this.ruleMap == null ? Collections.emptyList() :
                this.ruleMap.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .limit(size).map((e) -> e.getValue().toString() + ":" + e.getKey().getId()).collect(Collectors.toList());
    }

    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Possible rule-loop detected. Maximum allowed rule matches has been exceeded.").append(System.lineSeparator());
        sb.append("Current Limit:").append(this.limit).append(System.lineSeparator());
        Iterator var3 = this.getRuleFirings(10L).iterator();

        while(var3.hasNext()) {
            String ruleFiring = (String)var3.next();
            sb.append(ruleFiring).append(System.lineSeparator());
        }

        sb.append("You can adjust or disable the limit for rule matches by changing the ruleFiringLimit field in the 'Drools Engine Context' object (see the 'Rule Firing Limit' attribute).\n");
        return sb.toString();
    }
}
