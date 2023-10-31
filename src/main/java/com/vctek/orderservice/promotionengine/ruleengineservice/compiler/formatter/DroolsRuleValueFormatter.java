package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.formatter;


import com.vctek.orderservice.promotionengine.droolsruleengineservice.compiler.DroolsRuleGeneratorContext;

public interface DroolsRuleValueFormatter {
    String formatValue(DroolsRuleGeneratorContext context, Object value);
}
