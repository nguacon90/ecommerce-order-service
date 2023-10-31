package com.vctek.orderservice.promotionengine.ruleengineservice.converter;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectReader;
import com.vctek.orderservice.promotionengine.ruleengine.exception.RuleConverterException;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;
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
public class DefaultRuleConditionsConverter extends AbstractRuleConverter implements RuleConditionsConverter {

    public DefaultRuleConditionsConverter(RuleParameterValueConverter ruleParameterValueConverter, RuleParameterUuidGenerator ruleParameterUuidGenerator) {
        super(ruleParameterValueConverter, ruleParameterUuidGenerator);
    }

    @Override
    public String toString(List<RuleConditionData> conditions, Map<String, RuleConditionDefinitionData> conditionDefinitions) {
        try {
            return this.getObjectWriter().writeValueAsString(conditions);
        } catch (IOException var4) {
            throw new RuleConverterException(var4);
        }
    }

    @Override
    public List<RuleConditionData> fromString(String conditions, Map<String, RuleConditionDefinitionData> conditionDefinitions) {
        if (StringUtils.isBlank(conditions)) {
            return Collections.emptyList();
        }
        if(MapUtils.isEmpty(conditionDefinitions)) {
            return Collections.emptyList();
        }
        try {
            ObjectReader objectReader = this.getObjectReader();
            JavaType javaType = objectReader.getTypeFactory().constructCollectionType(List.class, RuleConditionData.class);
            List<RuleConditionData> parsedConditions = objectReader.forType(javaType).readValue(conditions);
            this.convertParameterValues(conditionDefinitions, parsedConditions);
            return parsedConditions;
        } catch (IOException var6) {
            throw new RuleConverterException(var6);
        }
    }

    private void convertParameterValues(Map<String, RuleConditionDefinitionData> conditionDefinitions, List<RuleConditionData> conditions) {
        if (!CollectionUtils.isEmpty(conditions)) {
            RuleConditionData condition;
            for(Iterator var4 = conditions.iterator(); var4.hasNext(); this.convertParameterValues(conditionDefinitions, condition.getChildren())) {
                condition = (RuleConditionData)var4.next();
                String definitionId = condition.getDefinitionId();
                RuleConditionDefinitionData conditionDefinition = conditionDefinitions.get(definitionId);
                if (conditionDefinition == null) {
                    throw new RuleConverterException("No definition found for condition: [definitionId=" + definitionId + "]");
                }

                if (condition.getParameters() == null) {
                    condition.setParameters(new HashMap());
                }

                if (MapUtils.isEmpty(conditionDefinition.getParameters())) {
                    condition.getParameters().clear();
                } else {
                    condition.getParameters().keySet().retainAll(conditionDefinition.getParameters().keySet());
                    this.convertParameters(condition, conditionDefinition);
                }
            }

        }
    }

}
