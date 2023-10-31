package com.vctek.orderservice.promotionengine.droolsruleengineservice.compiler;


import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleIr;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleIrVariable;

import java.util.Deque;
import java.util.Map;
import java.util.Set;

public interface DroolsRuleGeneratorContext {
    RuleCompilerContext getRuleCompilerContext();

    String getIndentationSize();

    String getVariablePrefix();

    String getAttributeDelimiter();

    RuleIr getRuleIr();

    Map<String, RuleIrVariable> getVariables();

    Deque<Map<String, RuleIrVariable>> getLocalVariables();

    void addLocalVariables(Map<String, RuleIrVariable> var1);

    Set<Class<?>> getImports();

    Map<String, Class<?>> getGlobals();

    String generateClassName(Class<?> var1);

    void addGlobal(String var1, Class<?> var2);

    DroolsRuleModel getDroolsRule();
}
