package com.vctek.orderservice.promotionengine.ruleengineservice.strategy;


import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;

import java.util.Locale;
import java.util.Map;

public interface RuleMessageFormatStrategy {
    String format(String var1, Map<String, RuleParameterData> var2, Locale var3);

    String format(String var1, Map<String, RuleParameterData> var2, Locale var3, RuleMessageParameterDecorator var4);
}
