package com.vctek.orderservice.promotionengine.ruleengineservice.enums;

public enum RuleIrAttributeOperator {
    EQUAL("==")  ,
    NOT_EQUAL("!=")  ,
    GREATER_THAN(">")  ,
    GREATER_THAN_OR_EQUAL(">=")  ,
    LESS_THAN("<")  ,
    LESS_THAN_OR_EQUAL("<=")  ,
    IN("in")  ,
    NOT_IN("not in")  ,
    CONTAINS("contains")  ,
    NOT_CONTAINS("not contains")  ,
    MEMBER_OF("memberOf");

    private String value;
    private final String originalCode;

    RuleIrAttributeOperator(String value) {
        this.value = value.toUpperCase();
        this.originalCode = value;
    }

    public String value() {
        return this.value;
    }

    public String getOriginalCode() {
        return originalCode;
    }

    public static RuleIrAttributeOperator fromCode(String code)
    {
        switch (code)
        {
            case "==":
                return EQUAL;
            case "!=":
                return NOT_EQUAL;
            case ">":
                return GREATER_THAN;
            case ">=":
                return GREATER_THAN_OR_EQUAL;
            case "<":
                return LESS_THAN;
            case "<=":
                return LESS_THAN_OR_EQUAL;
            case "in":
                return IN;
            case "not in":
                return NOT_IN;
            case "contains":
                return CONTAINS;
            case "not contains":
                return NOT_CONTAINS;
            case "memberOf":
                return MEMBER_OF;
            default:
                throw new IllegalArgumentException("Unknown code \"" + code + "\"");

        }
    }
}
