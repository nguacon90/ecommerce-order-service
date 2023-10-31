package com.vctek.orderservice.promotionengine.ruleengineservice.compiler;

import org.springframework.stereotype.Component;

@Component
public class DefaultRuleIrVariablesGeneratorFactory implements RuleIrVariablesGeneratorFactory {

    @Override
    public RuleIrVariablesGenerator createVariablesGenerator() {
        return new DefaultRuleIrVariablesGenerator();
    }
}
