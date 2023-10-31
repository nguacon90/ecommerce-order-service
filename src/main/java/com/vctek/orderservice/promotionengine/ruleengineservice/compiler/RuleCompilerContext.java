package com.vctek.orderservice.promotionengine.ruleengineservice.compiler;


import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrLocalVariablesContainer;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.maintenance.RuleCompilationContext;

import java.util.List;
import java.util.Map;

public interface RuleCompilerContext {
    String getModuleName();

    PromotionSourceRuleModel getRule();

    List<RuleParameterData> getRuleParameters();

    String generateVariable(Class<?> var1);

    RuleIrVariablesGenerator getVariablesGenerator();

    List<RuleConditionData> getRuleConditions();

    Map<String, RuleConditionDefinitionData> getConditionDefinitions();

    Map<String, RuleActionDefinitionData> getActionDefinitions();

    RuleCompilationContext getRuleCompilationContext();

    RuleIrLocalVariablesContainer createLocalContainer();

    String generateLocalVariable(RuleIrLocalVariablesContainer container, Class<?> type);

    void addProblem(RuleCompilerProblem problem);
}
