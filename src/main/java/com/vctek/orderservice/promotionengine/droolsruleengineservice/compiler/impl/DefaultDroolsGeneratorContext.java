package com.vctek.orderservice.promotionengine.droolsruleengineservice.compiler.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.vctek.orderservice.promotionengine.droolsruleengineservice.compiler.DroolsRuleGeneratorContext;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleIr;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleIrVariable;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrVariablesContainer;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ClassUtils;

import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;

public class DefaultDroolsGeneratorContext implements DroolsRuleGeneratorContext {
    public static final String DEFAULT_INDENTATION_SIZE = "   ";
    public static final String DEFAULT_VARIABLE_PREFIX = "$";
    public static final String DEFAULT_ATTRIBUTE_DELIMITER = ".";
    private final RuleCompilerContext ruleCompilerContext;
    private final RuleIr ruleIr;
    private DroolsRuleModel droolsRule;
    private final Deque<Map<String, RuleIrVariable>> localVariables;
    private final Map<String, Class<?>> imports;
    private final Map<String, Class<?>> globals;
    private Map<String, RuleIrVariable> variables;

    public DefaultDroolsGeneratorContext(RuleCompilerContext ruleCompilerContext, RuleIr ruleIr,
                                         DroolsRuleModel droolsRule) {
        this.ruleCompilerContext = ruleCompilerContext;
        this.ruleIr = ruleIr;
        this.droolsRule = droolsRule;
        this.localVariables = new ConcurrentLinkedDeque();
        this.imports = Maps.newConcurrentMap();
        this.globals = Maps.newConcurrentMap();
    }

    public String getIndentationSize() {
        return DEFAULT_INDENTATION_SIZE;
    }

    public String getVariablePrefix() {
        return DEFAULT_VARIABLE_PREFIX;
    }

    public String getAttributeDelimiter() {
        return DEFAULT_ATTRIBUTE_DELIMITER;
    }

    public RuleCompilerContext getRuleCompilerContext() {
        return this.ruleCompilerContext;
    }

    public RuleIr getRuleIr() {
        return this.ruleIr;
    }

    public DroolsRuleModel getDroolsRule() {
        return this.droolsRule;
    }

    public Map<String, RuleIrVariable> getVariables() {
        if (this.variables == null) {
            this.variables = Maps.newConcurrentMap();
            this.populateVariables(this.ruleIr.getVariablesContainer());
        }

        return this.variables;
    }

    public Deque<Map<String, RuleIrVariable>> getLocalVariables() {
        return this.localVariables;
    }

    public void addLocalVariables(Map<String, RuleIrVariable> ruleIrVariables) {
        this.localVariables.offerFirst(ruleIrVariables);
    }

    public Set<Class<?>> getImports() {
        return ImmutableSet.copyOf(this.imports.values());
    }

    public Map<String, Class<?>> getGlobals() {
        return ImmutableMap.copyOf(this.globals);
    }

    public String generateClassName(Class<?> type) {
        String shortClassName = ClassUtils.getShortClassName(type);
        Class<?> existingType = this.imports.get(shortClassName);
        if (existingType == null) {
            this.imports.put(shortClassName, type);
            return shortClassName;
        } else {
            return existingType.equals(type) ? shortClassName : type.getName();
        }
    }

    public void addGlobal(String name, Class<?> type) {
        this.globals.put(name, type);
    }

    protected void populateVariables(RuleIrVariablesContainer variablesContainer) {
        Iterator var3;
        if (MapUtils.isNotEmpty(variablesContainer.getVariables())) {
            var3 = variablesContainer.getVariables().values().iterator();

            while(var3.hasNext()) {
                RuleIrVariable variable = (RuleIrVariable)var3.next();
                this.variables.put(variable.getName(), variable);
            }
        }

        if (MapUtils.isNotEmpty(variablesContainer.getChildren())) {
            var3 = variablesContainer.getChildren().values().iterator();

            while(var3.hasNext()) {
                RuleIrVariablesContainer childVariablesContainer = (RuleIrVariablesContainer)var3.next();
                this.populateVariables(childVariablesContainer);
            }
        }

    }
}
