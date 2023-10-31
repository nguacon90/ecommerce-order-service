package com.vctek.orderservice.promotionengine.droolsruleengineservice.compiler;

public interface DroolsRuleConditionsGenerator {
    String generateConditions(DroolsRuleGeneratorContext context, String indentation);

    String generateRequiredFactsCheckPattern(DroolsRuleGeneratorContext context);
}
