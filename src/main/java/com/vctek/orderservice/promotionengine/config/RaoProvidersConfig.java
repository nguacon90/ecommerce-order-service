package com.vctek.orderservice.promotionengine.config;

import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.AbstractRuleEngineRuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.RAOFactsExtractor;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.RAOProvider;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.impl.CouponCartRaoExtractor;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.impl.FactContextType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;

@Configuration
public class RaoProvidersConfig {

    @Autowired
    @Qualifier("evaluationTimeRRDProvider")
    private RAOProvider evaluationTimeRRDProvider;

    @Autowired
    @Qualifier("cartRAOProvider")
    private RAOProvider cartRAOProvider;

    @Autowired
    @Qualifier("companyRAOProvider")
    private RAOProvider companyRAOProvider;

    @Autowired
    @Qualifier("ruleConfigurationRRDProvider")
    private RAOProvider ruleConfigurationRRDProvider;

    @Autowired
    @Qualifier("ruleGroupExecutionRRDProvider")
    private RAOProvider ruleGroupExecutionRRDProvider;

    @Bean("factExtractorList")
    public List<RAOFactsExtractor> factExtractorList(CouponCartRaoExtractor couponCartRaoExtractor) {
        List<RAOFactsExtractor> factsExtractorList = new ArrayList<>();
        factsExtractorList.add(couponCartRaoExtractor);
        return factsExtractorList;
    }

    @Bean(name = "promotionOrders")
    public Map<Class, List<RAOProvider>> promotionOrders() {
        Map<Class, List<RAOProvider>> promotionOrders = new HashMap<>();
        promotionOrders.put(Date.class, dateRAOProviders());
        promotionOrders.put(AbstractOrderModel.class, orderPromotionRaoProviders());
        return promotionOrders;
    }
    @Bean(name = "ruleConfigurationProviders")
    public Map<Class, List<RAOProvider>> ruleConfigurationProviders() {
        Map<Class, List<RAOProvider>> ruleConfigurationProviders = new HashMap<>();
        ruleConfigurationProviders.put(AbstractRuleEngineRuleModel.class, Arrays.asList(ruleConfigurationRRDProvider));
        return ruleConfigurationProviders;
    }

    @Bean(name = "ruleGroupRaoProviders")
    public Map<Class, List<RAOProvider>> ruleGroupRaoProviders() {
        Map<Class, List<RAOProvider>> ruleGroupRaoProviders = new HashMap<>();
        ruleGroupRaoProviders.put(AbstractRuleEngineRuleModel.class, Arrays.asList(ruleGroupExecutionRRDProvider));
        return ruleGroupRaoProviders;
    }

    @Bean(name = "orderPromotionRaoProviders")
    public List<RAOProvider> orderPromotionRaoProviders() {
        List<RAOProvider> raoProviders = new ArrayList<>();
        raoProviders.add(cartRAOProvider);
        raoProviders.add(companyRAOProvider);
        return raoProviders;
    }

    @Bean(name = "dateRAOProviders")
    public List<RAOProvider> dateRAOProviders() {
        List<RAOProvider> dateRAOProviders = new ArrayList<>();
        dateRAOProviders.add(evaluationTimeRRDProvider);
        return dateRAOProviders;
    }

    @Bean(name = "raoProviders")
    public Map<String, Map<Class, List<RAOProvider>>> raoProviders() {
        Map<String, Map<Class, List<RAOProvider>>> raoProviders = new HashMap<>();
        raoProviders.put(FactContextType.RULE_CONFIGURATION.toString(), ruleConfigurationProviders());
        raoProviders.put(FactContextType.RULE_GROUP.toString(), ruleGroupRaoProviders());
        raoProviders.put(FactContextType.PROMOTION_ORDER.toString(), promotionOrders());
        raoProviders.put(FactContextType.PROMOTION_PRODUCT.toString(), promotionProducts());
        return raoProviders;
    }

    //TODO implement promotion for product
    @Bean(name = "promotionProducts")
    public Map<Class, List<RAOProvider>> promotionProducts() {
        Map<Class, List<RAOProvider>> promotionOrders = new HashMap<>();
        return promotionOrders;
    }
}