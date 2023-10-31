package com.vctek.orderservice.promotionengine.droolsruleengineservice.compiler;

public interface DroolsRuleActionsGenerator {
    String generateActions(DroolsRuleGeneratorContext context, String indentation);
}
