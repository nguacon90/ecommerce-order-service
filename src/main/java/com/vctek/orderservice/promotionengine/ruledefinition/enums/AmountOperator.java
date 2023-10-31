package com.vctek.orderservice.promotionengine.ruledefinition.enums;


import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleParameterEnum;

public enum AmountOperator implements RuleParameterEnum {
    EQUAL,
    GREATER_THAN,
    GREATER_THAN_OR_EQUAL,
    LESS_THAN,
    LESS_THAN_OR_EQUAL;
}
