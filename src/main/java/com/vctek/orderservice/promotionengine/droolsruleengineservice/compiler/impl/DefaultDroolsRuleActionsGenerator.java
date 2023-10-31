package com.vctek.orderservice.promotionengine.droolsruleengineservice.compiler.impl;

import com.vctek.orderservice.promotionengine.droolsruleengineservice.compiler.DroolsRuleActionsGenerator;
import com.vctek.orderservice.promotionengine.droolsruleengineservice.compiler.DroolsRuleGeneratorContext;
import com.vctek.orderservice.promotionengine.droolsruleengineservice.exception.DroolsRuleValueFormatterException;
import com.vctek.orderservice.promotionengine.ruleengine.exception.RuleCompilerException;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleIrVariable;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.actions.*;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.formatter.DroolsRuleValueFormatter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
public class DefaultDroolsRuleActionsGenerator implements DroolsRuleActionsGenerator {
    public static final int BUFFER_SIZE = 4096;
    private static final String EXECUTABLE_ACTION_METHOD = "executeAction";
    private static final String VARIABLES_PARAM = "variables";
    private static final String DROOLS_CONTEXT_PARAM = "kcontext";
    private DroolsRuleValueFormatter droolsRuleValueFormatter;

    public DefaultDroolsRuleActionsGenerator(DroolsRuleValueFormatter droolsRuleValueFormatter) {
        this.droolsRuleValueFormatter = droolsRuleValueFormatter;
    }

    public String generateActions(DroolsRuleGeneratorContext context, String indentation) {
        StringBuilder actionsBuffer = new StringBuilder(BUFFER_SIZE);
        this.generateVariables(context, indentation, actionsBuffer);
        List<RuleIrAction> actions = context.getRuleIr().getActions();
        if (CollectionUtils.isEmpty(actions)) {
            actions = Collections.emptyList();
        }

        Iterator var6 = actions.iterator();

        while(var6.hasNext()) {
            RuleIrAction action = (RuleIrAction)var6.next();
            if (action instanceof RuleIrExecutableAction) {
                try {
                    this.generateExecutableAction(context, (RuleIrExecutableAction)action, indentation, actionsBuffer);
                } catch (DroolsRuleValueFormatterException var8) {
                    throw new RuleCompilerException(var8);
                }
            } else if (!(action instanceof RuleIrNoOpAction)) {
                throw new RuleCompilerException("Not supported RuleIrAction");
            }
        }

        return actionsBuffer.toString();
    }

    protected void generateVariables(DroolsRuleGeneratorContext context, String indentation, StringBuilder actionsBuffer) {
        String mapClassName = context.generateClassName(Map.class);
        actionsBuffer.append(indentation).append(mapClassName).append(' ').append(VARIABLES_PARAM).append(" = [\n");
        String variableIndentation = indentation + context.getIndentationSize();
        Map<String, RuleIrVariable> variables = context.getVariables();
        int remainingVariables = variables.size();

        for(Iterator var9 = variables.values().iterator(); var9.hasNext(); --remainingVariables) {
            RuleIrVariable variable = (RuleIrVariable)var9.next();
            String variableClassName = variable.getType().getName();
            actionsBuffer.append(variableIndentation);
            actionsBuffer.append('"');
            String[] path = variable.getPath();
            if (path != null && path.length > 0) {
                String[] var15 = variable.getPath();
                int var14 = var15.length;

                for(int var13 = 0; var13 < var14; ++var13) {
                    String groupId = var15[var13];
                    actionsBuffer.append(groupId);
                    actionsBuffer.append("/");
                }
            }

            actionsBuffer.append(variableClassName);
            actionsBuffer.append("\" : ");
            actionsBuffer.append(context.getVariablePrefix());
            actionsBuffer.append(variable.getName());
            actionsBuffer.append("_set");
            if (remainingVariables > 1) {
                actionsBuffer.append(',');
            }

            actionsBuffer.append('\n');
        }

        actionsBuffer.append(indentation).append("];\n");
    }

    protected void generateExecutableAction(DroolsRuleGeneratorContext context, RuleIrExecutableAction ruleIrAction, String indentation, StringBuilder actionsBuffer) {
        context.addGlobal(ruleIrAction.getActionId(), RuleExecutableAction.class);
        String actionContextClassName = context.generateClassName(DefaultDroolsRuleActionContext.class);
        actionsBuffer.append(indentation);
        actionsBuffer.append(ruleIrAction.getActionId()).append('.').append(EXECUTABLE_ACTION_METHOD);
        actionsBuffer.append('(');
        actionsBuffer.append("new ").append(actionContextClassName).append('(').append(VARIABLES_PARAM)
                .append(", ").append(DROOLS_CONTEXT_PARAM).append(')');
        actionsBuffer.append(", ");
        Map<String, Object> actionParameters = ruleIrAction.getActionParameters();
        actionsBuffer.append(this.droolsRuleValueFormatter.formatValue(context, actionParameters));
        actionsBuffer.append(");\n");
    }

}
