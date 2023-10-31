package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.impl;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.promotionengine.ruleengine.exception.RuleCompilerException;
import com.vctek.orderservice.promotionengine.ruleengine.exception.RuleEngineServiceException;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleIr;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.actions.RuleIrAction;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.RuleActionsTranslator;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.RuleConditionsTranslator;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.RuleSourceCodeTranslator;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.SourceRuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleActionsService;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleConditionsService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

@Component
public class DefaultRuleSourceCodeTranslator implements RuleSourceCodeTranslator {
    private RuleConditionsService ruleConditionsService;
    private RuleActionsService ruleActionsService;
    private RuleConditionsTranslator ruleConditionsTranslator;
    private RuleActionsTranslator ruleActionsTranslator;

    public DefaultRuleSourceCodeTranslator(RuleConditionsService ruleConditionsService,
                                           RuleActionsService ruleActionsService,
                                           RuleConditionsTranslator ruleConditionsTranslator,
                                           RuleActionsTranslator ruleActionsTranslator) {
        this.ruleConditionsService = ruleConditionsService;
        this.ruleActionsService = ruleActionsService;
        this.ruleConditionsTranslator = ruleConditionsTranslator;
        this.ruleActionsTranslator = ruleActionsTranslator;
    }

    @Override
    public RuleIr translate(RuleCompilerContext context) {
        try {
            if (!(context.getRule() instanceof SourceRuleModel)) {
                throw new RuleCompilerException("Rule is not of type SourceRule");
            }

            SourceRuleModel rule = context.getRule();
            RuleIr ruleIr = new RuleIr();
            ruleIr.setVariablesContainer(context.getVariablesGenerator().getRootContainer());
            List<RuleConditionData> ruleConditions = this.ruleConditionsService.convertConditionsFromString(rule.getConditions(),
                    context.getConditionDefinitions());
            this.populateRuleParametersFromConditions(context, ruleConditions);
            this.addRuleConditionsToContext(context, ruleConditions);
            List<RuleActionData> ruleActions = this.ruleActionsService.convertActionsFromString(rule.getActions(),
                    context.getActionDefinitions());

            if (CollectionUtils.isEmpty(ruleActions)) {
                throw new RuleEngineServiceException("empty rule actions");
            }

            this.populateRuleParametersFromActions(context, ruleActions);
            List<RuleIrCondition> ruleIrConditions = this.ruleConditionsTranslator.translate(context, ruleConditions);
            ruleIr.setConditions(ruleIrConditions);
            List<RuleIrAction> ruleIrActions = this.ruleActionsTranslator.translate(context, ruleActions);
            ruleIr.setActions(ruleIrActions);
            this.validate(context, ruleConditions, ruleActions);
            return ruleIr;
        } catch (RuleEngineServiceException var8) {
            throw new RuleCompilerException(var8);
        }
    }

    protected void addRuleConditionsToContext(RuleCompilerContext context, List<RuleConditionData> ruleConditions) {
        if (!CollectionUtils.isEmpty(ruleConditions)) {
            Iterator var4 = ruleConditions.iterator();

            while (var4.hasNext()) {
                RuleConditionData condition = (RuleConditionData) var4.next();
                context.getRuleConditions().add(condition);
                this.addRuleConditionsToContext(context, condition.getChildren());
            }

        }
    }

    protected void populateRuleParametersFromConditions(RuleCompilerContext context, List<RuleConditionData> conditions) {
        if (!CollectionUtils.isEmpty(conditions)) {
            RuleConditionData condition;
            for (Iterator var4 = conditions.iterator(); var4.hasNext();
                 this.populateRuleParametersFromConditions(context, condition.getChildren())) {
                condition = (RuleConditionData) var4.next();
                if (MapUtils.isNotEmpty(condition.getParameters())) {
                    Iterator var6 = condition.getParameters().values().iterator();

                    while (var6.hasNext()) {
                        RuleParameterData parameter = (RuleParameterData) var6.next();
                        context.getRuleParameters().add(parameter);
                    }
                }
            }

        }
    }

    protected void populateRuleParametersFromActions(RuleCompilerContext context, List<RuleActionData> actions) {
        if (!CollectionUtils.isEmpty(actions)) {
            Iterator var4 = actions.iterator();

            while (true) {
                RuleActionData action;
                do {
                    if (!var4.hasNext()) {
                        return;
                    }

                    action = (RuleActionData) var4.next();
                } while (!MapUtils.isNotEmpty(action.getParameters()));

                Iterator var6 = action.getParameters().values().iterator();

                while (var6.hasNext()) {
                    RuleParameterData parameter = (RuleParameterData) var6.next();
                    context.getRuleParameters().add(parameter);
                }
            }
        }
    }

    protected void validate(RuleCompilerContext context, List<RuleConditionData> conditions, List<RuleActionData> actions) {
        if (CollectionUtils.isEmpty(actions)) {
            ErrorCodes err = ErrorCodes.EMPTY_PROMOTION_ACTIONS;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        this.ruleConditionsTranslator.validate(context, conditions);
        this.ruleActionsTranslator.validate(context, actions);
    }

}