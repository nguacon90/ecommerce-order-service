package com.vctek.orderservice.promotionengine.ruleengineservice.strategy;


import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;

import java.util.List;

public interface RuleParametersConverter {
    String toString(List<RuleParameterData> var1);

    List<RuleParameterData> fromString(String var1);
}
