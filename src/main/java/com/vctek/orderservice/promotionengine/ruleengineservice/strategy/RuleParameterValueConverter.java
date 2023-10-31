package com.vctek.orderservice.promotionengine.ruleengineservice.strategy;

import com.fasterxml.jackson.databind.JavaType;

public interface RuleParameterValueConverter {
    String toString(Object value);

    Object fromString(String value, String type);

    JavaType fromString(String type);

}
