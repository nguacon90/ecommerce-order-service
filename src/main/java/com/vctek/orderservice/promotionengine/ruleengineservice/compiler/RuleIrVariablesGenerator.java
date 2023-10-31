package com.vctek.orderservice.promotionengine.ruleengineservice.compiler;


import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrLocalVariablesContainer;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrVariablesContainer;

public interface RuleIrVariablesGenerator {
    String generateVariable(Class<?> var1);

    RuleIrLocalVariablesContainer createLocalContainer();

    RuleIrVariablesContainer getRootContainer();

    String generateLocalVariable(RuleIrLocalVariablesContainer var1, Class<?> var2);

    RuleIrVariablesContainer createContainer(String id);

    void closeContainer();

}
