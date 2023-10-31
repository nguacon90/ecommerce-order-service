package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.impl;

import com.vctek.orderservice.promotionengine.ruleengine.exception.RuleCompilerException;
import com.vctek.orderservice.promotionengine.ruleengine.exception.RuleEngineServiceException;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.validation.RuleParameterValidator;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.AbstractRuleDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RuleDefinitionTranslator {
    protected ApplicationContext applicationContext;

    public RuleDefinitionTranslator(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    protected RuleParameterValidator getParameterValidator(String validatorId) {
        try {
            return this.applicationContext.getBean(validatorId, RuleParameterValidator.class);
        } catch (BeansException var3) {
            throw new RuleCompilerException(var3);
        }
    }

    protected void validateParameters(RuleCompilerContext context, AbstractRuleDefinitionData ruleDefinition,
                                      Map<String, RuleParameterData> parameters,
                                      Map<String, RuleParameterDefinitionData> parameterDefinitions) {
        Iterator var6 = parameters.entrySet().iterator();

        while(var6.hasNext()) {
            Map.Entry<String, RuleParameterData> entry = (Map.Entry)var6.next();
            String parameterId = entry.getKey();
            RuleParameterDefinitionData parameterDefinition = parameterDefinitions.get(parameterId);
            List<String> validatorIds = new ArrayList();
            validatorIds.add("ruleRequiredParameterValidator");
            validatorIds.addAll(parameterDefinition.getValidators());
            Iterator var11 = validatorIds.iterator();

            while(var11.hasNext()) {
                String validatorId = (String)var11.next();

                try {
                    this.getParameterValidator(validatorId).validate(context, ruleDefinition, entry.getValue(), parameterDefinition);
                } catch (RuleEngineServiceException var13) {
                    throw new RuleCompilerException(var13);
                }
            }
        }

    }
}
