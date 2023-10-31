package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.impl;

import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.DefaultRuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.DefaultRuleIrVariablesGenerator;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrFalseCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.RuleConditionTranslator;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.maintenance.RuleCompilationContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.maintenance.impl.DefaultRuleCompilationContext;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

public abstract class AbstractRuleConditionTranslatorTest {
    protected RuleConditionTranslator translator;
    protected RuleCompilerContext context;
    protected RuleConditionData condition;
    protected RuleConditionDefinitionData conditionDefinition;
    protected PromotionSourceRuleModel promotionSourceRule;
    protected RuleParameterData operatorParameter = new RuleParameterData();
    protected RuleParameterData valueParameter = new RuleParameterData();
    protected Map<String, RuleParameterData> parameters = new HashMap<>();
    private RuleCompilationContext ruleCompilationContext;

    @Before
    public void init() {
        ruleCompilationContext = new DefaultRuleCompilationContext();
        promotionSourceRule = new PromotionSourceRuleModel();
        conditionDefinition = new RuleConditionDefinitionData();
        condition = new RuleConditionData();
        parameters.put(RuleCartTotalConditionTranslator.OPERATOR_PARAM, operatorParameter);
        parameters.put(RuleCartTotalConditionTranslator.VALUE_PARAM, valueParameter);
        condition.setParameters(parameters);
        context = new DefaultRuleCompilerContext("promotion", promotionSourceRule, new DefaultRuleIrVariablesGenerator(), ruleCompilationContext);
    }

    @Test
    public void translate_OperatorParamNull() {
        RuleIrCondition ruleIrCondition = translator.translate(context, condition, conditionDefinition);
        assertThat(ruleIrCondition, instanceOf(RuleIrFalseCondition.class));
    }

    protected void setTranslator(RuleConditionTranslator translator) {
        this.translator = translator;
    }
}
