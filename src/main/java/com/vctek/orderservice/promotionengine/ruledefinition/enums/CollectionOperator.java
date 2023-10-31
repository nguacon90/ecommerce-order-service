package com.vctek.orderservice.promotionengine.ruledefinition.enums;


import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleParameterEnum;

public enum CollectionOperator implements RuleParameterEnum {
    CONTAINS_ANY,
    CONTAINS_ALL,
    NOT_CONTAINS;
}
