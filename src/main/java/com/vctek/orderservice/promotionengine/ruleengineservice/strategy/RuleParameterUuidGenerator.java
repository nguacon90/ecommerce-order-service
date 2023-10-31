package com.vctek.orderservice.promotionengine.ruleengineservice.strategy;


import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;

public interface RuleParameterUuidGenerator {
    String generateUuid(RuleParameterData ruleParameterData, RuleParameterDefinitionData ruleParameterDefinitionData);
}
