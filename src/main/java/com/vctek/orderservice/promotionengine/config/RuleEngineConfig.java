package com.vctek.orderservice.promotionengine.config;

import com.vctek.orderservice.promotionengine.promotionservice.strategy.ConditionResolutionStrategy;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.strategy.EntriesSelectionStrategy;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.OrderEntrySelectionStrategy;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.RAOProvider;
import com.vctek.orderservice.promotionengine.ruleengineservice.rrd.RuleConfigurationRRD;
import com.vctek.orderservice.promotionengine.ruleengineservice.rrd.RuleGroupExecutionRRD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;

@Configuration
public class RuleEngineConfig {

    @Bean(name = "ruleParameterTypeFormatter")
    public Map<String, String> ruleParameterTypeFormatter() {
        Map<String, String> formats = new HashMap<>();
        formats.put("ItemType\\((.*)\\)", "java.lang.String");
        formats.put("Map\\((.+)\\,\\s*(.+)\\)", "Map(%s,%s)");
        formats.put("List\\((.+)\\)", "List(%s)");
        return formats;
    }

    @Bean(name = "supportedTypes")
    public Set<String> supportedTypes() {
        Set<String> supportedTypes = new HashSet<>();
        supportedTypes.add("java.lang.Boolean");
        supportedTypes.add("java.lang.Character");
        supportedTypes.add("java.lang.String");
        supportedTypes.add("java.lang.Byte");
        supportedTypes.add("java.lang.Short");
        supportedTypes.add("java.lang.Integer");
        supportedTypes.add("java.lang.Long");
        supportedTypes.add("java.lang.Float");
        supportedTypes.add("java.lang.Double");
        supportedTypes.add("java.math.BigInteger");
        supportedTypes.add("java.math.BigDecimal");
        supportedTypes.add("java.util.Date");
        supportedTypes.add("java.lang.Enum");
        supportedTypes.add("java.util.List");
        supportedTypes.add("java.util.Map");
        return supportedTypes;
    }


    @Bean(name = "conditionResolutionStrategies")
    public Map<String, ConditionResolutionStrategy> conditionResolutionStrategies(
            @Qualifier("productConditionResolutionStrategy") ConditionResolutionStrategy productConditionResolutionStrategy,
            @Qualifier("categoryConditionResolutionStrategy") ConditionResolutionStrategy categoryConditionResolutionStrategy) {
        Map<String, ConditionResolutionStrategy> conditionResolutionStrategies = new HashMap<>();
        conditionResolutionStrategies.put("vctek_qualifying_products", productConditionResolutionStrategy);
        conditionResolutionStrategies.put("vctek_qualifying_categories", categoryConditionResolutionStrategy);
        return conditionResolutionStrategies;
    }

    @Autowired
    @Qualifier("ruleConfigurationRRDTemplateProvider")
    private RAOProvider ruleConfigurationRRDTemplateProvider;

    @Autowired
    @Qualifier("ruleGroupExecutionRRDTemplateProvider")
    private RAOProvider ruleGroupExecutionRRDTemplateProvider;

    @Autowired
    @Qualifier("ruleConfigurationRRDProvider")
    private RAOProvider ruleConfigurationRRDProvider;

    @Autowired
    @Qualifier("ruleGroupExecutionRRDProvider")
    private RAOProvider ruleGroupExecutionRRDProvider;

    @Bean("commerceRuleEngineRaoCacheProviders")
    public Map<Class, RAOProvider> commerceRuleEngineRaoCacheProviders() {
        HashMap<Class, RAOProvider> providerHashMap = new HashMap<>();
        providerHashMap.put(RuleConfigurationRRD.class, ruleConfigurationRRDTemplateProvider);
        providerHashMap.put(RuleGroupExecutionRRD.class, ruleGroupExecutionRRDTemplateProvider);
        return providerHashMap;
    }

    @Bean("commerceRuleEngineRaoCacheCreators")
    public List<RAOProvider> commerceRuleEngineRaoCacheCreators() {
        List<RAOProvider> raoProviders = new ArrayList<>();
        raoProviders.add(ruleConfigurationRRDProvider);
        raoProviders.add(ruleGroupExecutionRRDProvider);
        return raoProviders;
    }

    @Bean("entriesSelectionStrategies")
    public Map<OrderEntrySelectionStrategy, EntriesSelectionStrategy> entriesSelectionStrategies(
            @Qualifier("defaultEntriesSelectionStrategy") EntriesSelectionStrategy defaultEntriesSelectionStrategy,
            @Qualifier("cheapestEntriesSelectionStrategy") EntriesSelectionStrategy cheapestEntriesSelectionStrategy) {
        Map<OrderEntrySelectionStrategy, EntriesSelectionStrategy> map = new HashMap<>();
        map.put(OrderEntrySelectionStrategy.DEFAULT, defaultEntriesSelectionStrategy);
        map.put(OrderEntrySelectionStrategy.CHEAPEST, cheapestEntriesSelectionStrategy);
        return map;
    }
}
