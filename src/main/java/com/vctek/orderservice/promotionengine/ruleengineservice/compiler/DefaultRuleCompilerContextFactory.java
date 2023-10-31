package com.vctek.orderservice.promotionengine.ruleengineservice.compiler;

import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.enums.RuleType;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.maintenance.RuleCompilationContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleActionsRegistry;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleConditionsRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DefaultRuleCompilerContextFactory implements RuleCompilerContextFactory<DefaultRuleCompilerContext> {
    private RuleConditionsRegistry ruleConditionsRegistry;
    private RuleActionsRegistry ruleActionsRegistry;

    @Override
    public DefaultRuleCompilerContext createContext(RuleCompilationContext ruleCompilationContext, PromotionSourceRuleModel rule,
                                                    String moduleName, RuleIrVariablesGenerator variablesGenerator) {
        DefaultRuleCompilerContext context = new DefaultRuleCompilerContext(moduleName, rule, variablesGenerator, ruleCompilationContext);
        this.populateDefinitionsForRule(context);
        return context;
    }

    protected void populateDefinitionsForRule(DefaultRuleCompilerContext context) {
        Map<String, RuleConditionDefinitionData> conditionDefinitions = this.ruleConditionsRegistry.getConditionDefinitionsForRuleTypeAsMap(RuleType.PROMOTION);
        context.getConditionDefinitions().putAll(conditionDefinitions);
        Map<String, RuleActionDefinitionData> actionDefinitions = this.ruleActionsRegistry.getActionDefinitionsForRuleTypeAsMap(RuleType.PROMOTION);
        context.getActionDefinitions().putAll(actionDefinitions);
    }

    @Autowired
    public void setRuleConditionsRegistry(RuleConditionsRegistry ruleConditionsRegistry) {
        this.ruleConditionsRegistry = ruleConditionsRegistry;
    }

    @Autowired
    public void setRuleActionsRegistry(RuleActionsRegistry ruleActionsRegistry) {
        this.ruleActionsRegistry = ruleActionsRegistry;
    }
}
