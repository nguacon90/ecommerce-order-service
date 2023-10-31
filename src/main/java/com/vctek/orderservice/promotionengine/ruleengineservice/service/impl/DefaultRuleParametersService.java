package com.vctek.orderservice.promotionengine.ruleengineservice.service.impl;

import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleParametersService;
import com.vctek.orderservice.promotionengine.ruleengineservice.strategy.RuleParameterUuidGenerator;
import com.vctek.orderservice.promotionengine.ruleengineservice.strategy.RuleParametersConverter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultRuleParametersService implements RuleParametersService {
    private RuleParametersConverter ruleParametersConverter;
    private RuleParameterUuidGenerator ruleParameterUuidGenerator;

    public DefaultRuleParametersService(RuleParametersConverter ruleParametersConverter,
                                        RuleParameterUuidGenerator ruleParameterUuidGenerator) {
        this.ruleParametersConverter = ruleParametersConverter;
        this.ruleParameterUuidGenerator = ruleParameterUuidGenerator;
    }

    @Override
    public RuleParameterData createParameterFromDefinition(RuleParameterDefinitionData definition) {
        RuleParameterData parameter = new RuleParameterData();
        parameter.setUuid(this.ruleParameterUuidGenerator.generateUuid(parameter, definition));
        parameter.setType(definition.getType());
        parameter.setValue(definition.getDefaultValue());
        return parameter;
    }

    @Override
    public String convertParametersToString(List<RuleParameterData> parameters) {
        return this.ruleParametersConverter.toString(parameters);
    }

    @Override
    public List<RuleParameterData> convertParametersFromString(String parameters) {
        return this.ruleParametersConverter.fromString(parameters);
    }

}
