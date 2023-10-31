package com.vctek.orderservice.promotionengine.ruleengineservice.compiler;


import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrLocalVariablesContainer;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.maintenance.RuleCompilationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultRuleCompilerContext implements RuleCompilerContext {
    private final String moduleName;
    private final PromotionSourceRuleModel rule;
    private final List<RuleParameterData> ruleParameters;
    private final RuleIrVariablesGenerator variablesGenerator;
    private final List<RuleConditionData> ruleConditions;
    private final Map<String, RuleConditionDefinitionData> conditionDefinitions;
    private final Map<String, RuleActionDefinitionData> actionDefinitions;
    private RuleCompilationContext ruleCompilationContext;
    private final List<RuleCompilerProblem> problems;

    public DefaultRuleCompilerContext(String moduleName, PromotionSourceRuleModel rule,
                                      RuleIrVariablesGenerator variablesGenerator,
                                      RuleCompilationContext ruleCompilationContext) {
        this.moduleName = moduleName;
        this.rule = rule;
        this.variablesGenerator = variablesGenerator;
        this.ruleCompilationContext = ruleCompilationContext;
        this.actionDefinitions = new HashMap<>();
        this.conditionDefinitions = new HashMap<>();
        ruleParameters = new ArrayList<>();
        ruleConditions = new ArrayList<>();
        problems = new ArrayList<>();
    }

    @Override
    public String getModuleName() {
        return this.moduleName;
    }

    @Override
    public PromotionSourceRuleModel getRule() {
        return rule;
    }

    @Override
    public List<RuleParameterData> getRuleParameters() {
        return ruleParameters;
    }

    @Override
    public String generateVariable(Class<?> type) {
        return this.variablesGenerator.generateVariable(type);
    }

    @Override
    public RuleIrVariablesGenerator getVariablesGenerator() {
        return this.variablesGenerator;
    }

    @Override
    public List<RuleConditionData> getRuleConditions() {
        return ruleConditions;
    }

    @Override
    public Map<String, RuleConditionDefinitionData> getConditionDefinitions() {
        return this.conditionDefinitions;
    }

    @Override
    public Map<String, RuleActionDefinitionData> getActionDefinitions() {
        return this.actionDefinitions;
    }

    @Override
    public RuleCompilationContext getRuleCompilationContext() {
        return this.ruleCompilationContext;
    }

    @Override
    public RuleIrLocalVariablesContainer createLocalContainer() {
        return this.variablesGenerator.createLocalContainer();
    }

    @Override
    public String generateLocalVariable(RuleIrLocalVariablesContainer container, Class<?> type) {
        return this.variablesGenerator.generateLocalVariable(container, type);
    }

    @Override
    public void addProblem(RuleCompilerProblem problem) {
        this.problems.add(problem);
    }
}
