package com.vctek.orderservice.promotionengine.config;

import com.vctek.orderservice.promotionengine.promotionservice.model.RuleBasedOrderAddProductActionModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.RuleBasedOrderAdjustTotalActionModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.RuleBasedOrderEntryAdjustActionModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.strategy.RuleActionStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class RuleActionStrategyConfig {

    @Autowired
    @Qualifier("orderTotalAdjustActionStrategy")
    private RuleActionStrategy<RuleBasedOrderAdjustTotalActionModel> orderTotalAdjustActionStrategy;

    @Autowired
    @Qualifier("defaultOrderEntryAdjustActionStrategy")
    private RuleActionStrategy<RuleBasedOrderEntryAdjustActionModel> orderEntryAdjustActionStrategy;


    @Autowired
    @Qualifier("defaultAddProductToCartActionStrategy")
    private RuleActionStrategy<RuleBasedOrderAddProductActionModel> defaultAddProductToCartActionStrategy;

    @Autowired
    @Qualifier("defaultFixedPriceProductActionStrategy")
    private RuleActionStrategy<RuleBasedOrderAddProductActionModel> defaultFixedPriceProductActionStrategy;


    @Bean(name = "actionStrategiesMapping")
    public Map<String, RuleActionStrategy> actionStrategiesMapping() {
        Map<String, RuleActionStrategy> actionStrategies = new HashMap<>();
        actionStrategies.put("ruleOrderFixedDiscountRAOAction", orderTotalAdjustActionStrategy);
        actionStrategies.put("ruleOrderPercentageDiscountRAOAction", orderTotalAdjustActionStrategy);
        actionStrategies.put("ruleEmployeeOrderPercentageDiscountRAOAction", orderTotalAdjustActionStrategy);
        actionStrategies.put("ruleOrderEntryFixedDiscountRAOAction", orderEntryAdjustActionStrategy);
        actionStrategies.put("ruleOrderEntryPercentageDiscountRAOAction", orderEntryAdjustActionStrategy);
        actionStrategies.put("ruleFreeGiftRAOAction", defaultAddProductToCartActionStrategy);
        actionStrategies.put("ruleOrderEntryFixedPriceRAOAction", defaultFixedPriceProductActionStrategy);
        actionStrategies.put("rulePartnerOrderEntryPercentageDiscountRAOAction", orderEntryAdjustActionStrategy);
        return actionStrategies;
    }

    @Bean(name = "actionStrategies")
    public List<RuleActionStrategy> actionStrategies() {
        List<RuleActionStrategy> actionStrategies = new ArrayList<>();
        actionStrategies.add(orderTotalAdjustActionStrategy);
        actionStrategies.add(orderEntryAdjustActionStrategy);
        actionStrategies.add(defaultAddProductToCartActionStrategy);
        actionStrategies.add(defaultFixedPriceProductActionStrategy);
        return actionStrategies;
    }
}
