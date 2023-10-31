package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.impl;

import com.google.common.collect.Lists;
import com.vctek.converter.Converter;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.promotionengine.ruleengine.exception.RuleCompilerException;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.RuleConditionTranslator;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.RuleConditionsTranslator;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleConditionDefinitionModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleConditionDefinitionService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
public class DefaultRuleConditionsTranslator extends RuleDefinitionTranslator implements RuleConditionsTranslator {
    private RuleConditionDefinitionService conditionDefinitionService;
    private Converter<RuleConditionDefinitionModel, RuleConditionDefinitionData> conditionDefinitionConverter;

    public DefaultRuleConditionsTranslator(ApplicationContext applicationContext,
                                           RuleConditionDefinitionService conditionDefinitionService,
                                           Converter<RuleConditionDefinitionModel, RuleConditionDefinitionData> conditionDefinitionConverter) {
        super(applicationContext);
        this.conditionDefinitionService = conditionDefinitionService;
        this.conditionDefinitionConverter = conditionDefinitionConverter;
    }

    @Override
    public synchronized List<RuleIrCondition> translate(RuleCompilerContext context, List<RuleConditionData> conditions) {
        List<RuleIrCondition> ruleIrConditions = Lists.newArrayList();
        Iterator var6 = conditions.iterator();

        while(var6.hasNext()) {
            RuleConditionData condition = (RuleConditionData)var6.next();
            RuleConditionDefinitionModel ruleConditionDefinitionModel = conditionDefinitionService.findByCode(condition.getDefinitionId());
            if (ruleConditionDefinitionModel != null) {
                RuleConditionDefinitionData conditionDefinition = conditionDefinitionConverter.convert(ruleConditionDefinitionModel);
                RuleConditionTranslator conditionTranslator = this.getConditionTranslator(conditionDefinition.getTranslatorId());
                RuleIrCondition ruleIrCondition = conditionTranslator.translate(context, condition, conditionDefinition);
                ruleIrConditions.add(ruleIrCondition);
            }
        }

        return ruleIrConditions;
    }

    @Override
    public void validate(RuleCompilerContext context, List<RuleConditionData> conditions) {
        Map<String, RuleConditionDefinitionData> conditionDefinitions = context.getConditionDefinitions();
        Iterator var5 = conditions.iterator();

        while(var5.hasNext()) {
            RuleConditionData condition = (RuleConditionData)var5.next();
            RuleConditionDefinitionData conditionDefinition = conditionDefinitions.get(condition.getDefinitionId());
            if (conditionDefinition == null) {
                ErrorCodes err = ErrorCodes.EMPTY_PROMOTION_CONDITIONS;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }

            this.validateParameters(context, conditionDefinition, condition.getParameters(), conditionDefinition.getParameters());
        }
    }


    protected RuleConditionTranslator getConditionTranslator(String translatorId) {
        try {
            return this.applicationContext.getBean(translatorId, RuleConditionTranslator.class);
        } catch (BeansException var3) {
            throw new RuleCompilerException(var3);
        }
    }
}
