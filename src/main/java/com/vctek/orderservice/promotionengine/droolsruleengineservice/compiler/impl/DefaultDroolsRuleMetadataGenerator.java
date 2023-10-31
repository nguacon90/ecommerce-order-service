package com.vctek.orderservice.promotionengine.droolsruleengineservice.compiler.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vctek.orderservice.promotionengine.droolsruleengineservice.compiler.DroolsRuleGeneratorContext;
import com.vctek.orderservice.promotionengine.droolsruleengineservice.compiler.DroolsRuleMetadataGenerator;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrCondition;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class DefaultDroolsRuleMetadataGenerator implements DroolsRuleMetadataGenerator {

    @Override
    public String generateMetadata(DroolsRuleGeneratorContext context, String indentation) {
        List<RuleIrCondition> conditions = context.getRuleIr().getConditions();
        Map<String, List<Object>> conditionMetadata = Maps.newHashMap();
        List<Map<String, Object>> metadataList = conditions.stream()
                .filter(c -> Objects.nonNull(c.getMetadata()))
                .map(RuleIrCondition::getMetadata).collect(Collectors.toList());
        Iterator var7 = metadataList.iterator();

        while (var7.hasNext()) {
            Map<String, Object> metadata = (Map) var7.next();
            Iterator var9 = metadata.entrySet().iterator();

            while (var9.hasNext()) {
                Map.Entry<String, Object> entry = (Map.Entry) var9.next();
                if (!conditionMetadata.containsKey(entry.getKey())) {
                    conditionMetadata.put(entry.getKey(), Lists.newArrayList());
                }

                if (entry.getValue() instanceof Collection) {
                    conditionMetadata.get(entry.getKey()).addAll((Collection) entry.getValue());
                } else {
                    conditionMetadata.get(entry.getKey()).add(entry.getValue());
                }
            }
        }

        StringJoiner conditionsJoiner = new StringJoiner(StringUtils.EMPTY);
        Iterator var12 = conditionMetadata.entrySet().iterator();

        while (var12.hasNext()) {
            Map.Entry<String, List<Object>> entry = (Map.Entry) var12.next();
            String metadataValue = (String) ((List) entry.getValue()).stream().map((o) -> {
                return String.format("\"%s\"", o.toString());
            }).collect(Collectors.joining(","));
            conditionsJoiner.add("@").add(entry.getKey()).add(" ( ").add(metadataValue).add(" )\n");
        }

        return conditionsJoiner.toString();
    }
}
