package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.impl;

import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.actions.RuleIrAction;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.actions.RuleIrExecutableAction;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.actions.RuleIrNoOpAction;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.RuleActionTranslator;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component("ruleExecutableActionTranslator")
public class RuleExecutableActionTranslator implements RuleActionTranslator {
    public static final String ACTION_ID_PARAM = "actionId";

    @Override
    public RuleIrAction translate(RuleCompilerContext context, RuleActionData action, RuleActionDefinitionData actionDefinition) {
        String actionId = actionDefinition.getTranslatorParameters().get(ACTION_ID_PARAM);
        if (StringUtils.isBlank(actionId)) {
            return new RuleIrNoOpAction();
        }

        Map<String, Object> actionParameters = new HashMap<>();
        if (MapUtils.isNotEmpty(action.getParameters())) {
            Iterator var7 = action.getParameters().entrySet().iterator();

            while (var7.hasNext()) {
                Map.Entry<String, RuleParameterData> entry = (Map.Entry) var7.next();
                String parameterId = entry.getKey();
                if (entry.getValue() != null) {
                    actionParameters.put(parameterId, entry.getValue().getValue());
                    actionParameters.put(parameterId + "_uuid", entry.getValue().getUuid());
                }
            }
        }

        RuleIrExecutableAction irExecutableAction = new RuleIrExecutableAction();
        irExecutableAction.setActionId(actionId);
        irExecutableAction.setActionParameters(actionParameters);
        return irExecutableAction;
    }
}
