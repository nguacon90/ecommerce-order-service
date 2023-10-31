package com.vctek.orderservice.promotionengine.ruleengineservice.strategy.impl;

import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.strategy.RuleParameterUuidGenerator;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DefaultRuleParameterUuidGenerator implements RuleParameterUuidGenerator {

    public String generateUuid(RuleParameterData parameter, RuleParameterDefinitionData parameterDefinition) {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}