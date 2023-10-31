package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.impl;

import com.vctek.converter.Converter;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.promotionengine.ruleengine.exception.RuleCompilerException;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.actions.RuleIrAction;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.RuleActionTranslator;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.RuleActionsTranslator;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleActionDefinitionModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleActionDefinitionService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
public class DefaultRuleActionsTranslator extends RuleDefinitionTranslator implements RuleActionsTranslator {
    private RuleActionDefinitionService ruleActionDefinitionService;
    private Converter<RuleActionDefinitionModel, RuleActionDefinitionData> actionDefinitionConverter;

    public DefaultRuleActionsTranslator(ApplicationContext applicationContext,
                                        RuleActionDefinitionService ruleActionDefinitionService,
                                        Converter<RuleActionDefinitionModel, RuleActionDefinitionData> actionDefinitionConverter) {
        super(applicationContext);
        this.ruleActionDefinitionService = ruleActionDefinitionService;
        this.actionDefinitionConverter = actionDefinitionConverter;
    }

    @Override
    public List<RuleIrAction> translate(RuleCompilerContext context, List<RuleActionData> actions) {
        List<RuleIrAction> ruleIrActions = new ArrayList();
        Iterator var6 = actions.iterator();

        while (var6.hasNext()) {
            RuleActionData action = (RuleActionData) var6.next();
            RuleActionDefinitionModel actionDefinitionModel = ruleActionDefinitionService.findByCode(action.getDefinitionId());
            if (actionDefinitionModel != null) {
                RuleActionDefinitionData actionDefinition = actionDefinitionConverter.convert(actionDefinitionModel);
                RuleActionTranslator actionTranslator = this.getActionTranslator(actionDefinition.getTranslatorId());
                RuleIrAction ruleIrAction = actionTranslator.translate(context, action, actionDefinition);
                ruleIrActions.add(ruleIrAction);
            }
        }

        return ruleIrActions;
    }

    @Override
    public void validate(RuleCompilerContext context, List<RuleActionData> actions) {
        Map<String, RuleActionDefinitionData> actionDefinitions = context.getActionDefinitions();
        Iterator var5 = actions.iterator();

        while (var5.hasNext()) {
            RuleActionData action = (RuleActionData) var5.next();
            RuleActionDefinitionData actionDefinition = actionDefinitions.get(action.getDefinitionId());
            if (actionDefinition == null) {
                ErrorCodes err = ErrorCodes.EMPTY_PROMOTION_ACTIONS;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }

            this.validateParameters(context, actionDefinition, action.getParameters(), actionDefinition.getParameters());
        }

    }

    protected RuleActionTranslator getActionTranslator(String translatorId) {
        try {
            return this.applicationContext.getBean(translatorId, RuleActionTranslator.class);
        } catch (BeansException var3) {
            throw new RuleCompilerException(var3);
        }
    }
}
