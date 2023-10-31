package com.vctek.orderservice.promotionengine.ruleengineservice.converter;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectReader;
import com.vctek.orderservice.promotionengine.ruleengine.exception.RuleConverterException;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.strategy.RuleParameterUuidGenerator;
import com.vctek.orderservice.promotionengine.ruleengineservice.strategy.RuleParameterValueConverter;
import com.vctek.orderservice.promotionengine.ruleengineservice.strategy.impl.AbstractRuleConverter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
public class DefaultRuleActionsConverter extends AbstractRuleConverter implements RuleActionsConverter {
    public DefaultRuleActionsConverter(RuleParameterValueConverter ruleParameterValueConverter,
                                       RuleParameterUuidGenerator ruleParameterUuidGenerator) {
        super(ruleParameterValueConverter, ruleParameterUuidGenerator);
    }

    @Override
    public String toString(List<RuleActionData> actions, Map<String, RuleActionDefinitionData> actionDefinitions) {
        try {
            return this.getObjectWriter().writeValueAsString(actions);
        } catch (IOException var4) {
            throw new RuleConverterException(var4);
        }
    }

    @Override
    public List<RuleActionData> fromString(String actions, Map<String, RuleActionDefinitionData> actionDefinitions) {
        if (StringUtils.isBlank(actions)) {
            return Collections.emptyList();
        }
        try {
            ObjectReader objectReader = this.getObjectReader();
            JavaType javaType = objectReader.getTypeFactory().constructCollectionType(List.class, RuleActionData.class);
            List<RuleActionData> parsedActions = objectReader.forType(javaType).readValue(actions);
            this.convertParameterValues(parsedActions, actionDefinitions);
            return parsedActions;
        } catch (IOException var6) {
            throw new RuleConverterException(var6);
        }
    }

    private void convertParameterValues(List<RuleActionData> actions, Map<String, RuleActionDefinitionData> actionDefinitions) {
        if (!CollectionUtils.isEmpty(actions)) {
            Iterator var4 = actions.iterator();

            while (var4.hasNext()) {
                RuleActionData action = (RuleActionData) var4.next();
                RuleActionDefinitionData actionDefinition = actionDefinitions.get(action.getDefinitionId());

                if (actionDefinition == null) {
                    throw new RuleConverterException("No definition found for action: [definitionId=" + action.getDefinitionId() + "]");
                }

                if (action.getParameters() == null) {
                    action.setParameters(new HashMap());
                }

                if (MapUtils.isEmpty(actionDefinition.getParameters())) {
                    action.getParameters().clear();
                } else {
                    action.getParameters().keySet().retainAll(actionDefinition.getParameters().keySet());
                    this.convertParameters(action, actionDefinition);
                }
            }

        }
    }
}
