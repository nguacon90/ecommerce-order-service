package com.vctek.orderservice.promotionengine.droolsruleengineservice.compiler;

public interface DroolsRuleMetadataGenerator {
    String generateMetadata(DroolsRuleGeneratorContext context, String indentation);
}
