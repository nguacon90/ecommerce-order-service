package com.vctek.orderservice.promotionengine.ruleengineservice.action.impl;

import com.vctek.orderservice.model.ItemModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.action.RuleActionService;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.AbstractRuleActionRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.DiscountRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.FixedPriceProductRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.RuleEngineResultRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.strategy.RuleActionStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class DefaultRuleActionService implements RuleActionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRuleActionService.class);
    private Map<String, RuleActionStrategy> actionStrategiesMapping;

    public DefaultRuleActionService(Map<String, RuleActionStrategy> actionStrategiesMapping) {
        this.actionStrategiesMapping = actionStrategiesMapping;
    }

    @Override
    public List<ItemModel> applyAllActions(RuleEngineResultRAO ruleEngineResultRAO) {
        List<ItemModel> actionResults = new ArrayList<>();
        if (ruleEngineResultRAO == null || ruleEngineResultRAO.getActions() == null) {
            LOGGER.info("applyAllActions called for undefined action set!");
            return actionResults;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("applyAllActions triggered for actions: [{}]",
                    ruleEngineResultRAO.getActions().stream().map(AbstractRuleActionRAO::getFiredRuleCode)
                            .collect(Collectors.joining(", ")));
        }

        Iterator var4 = ruleEngineResultRAO.getActions().iterator();

        while (var4.hasNext()) {
//            long startApply = System.currentTimeMillis();
            AbstractRuleActionRAO action = (AbstractRuleActionRAO) var4.next();
            RuleActionStrategy strategy = this.getRuleActionStrategy(action.getActionStrategyKey());
            if (Objects.isNull(strategy)) {
                LOGGER.error(String.format("Strategy bean for key '%s' not found!", action.getActionStrategyKey()));
            } else if ((action instanceof FixedPriceProductRAO) || !(action instanceof DiscountRAO) || ((DiscountRAO) action).getValue() == null
                    || ((DiscountRAO) action).getValue().compareTo(BigDecimal.ZERO) > 0) {
                actionResults.addAll(strategy.apply(action));
            }
//            long endApply = System.currentTimeMillis();
//            LOGGER.info("== APPLY: {}", (endApply - startApply));
        }

        return actionResults;
    }

    protected RuleActionStrategy getRuleActionStrategy(String strategyKey) {
        if (this.actionStrategiesMapping == null) {
            throw new IllegalStateException("cannot call getActionStrategiesMapping(\"" + strategyKey + "\")," +
                    " no strategy mapping defined! Please configure your DefaultRuleActionService" +
                    " bean to contain actionStrategiesMapping.");
        }

        RuleActionStrategy strategy = this.actionStrategiesMapping.get(strategyKey);
        if (strategy == null) {
            throw new IllegalArgumentException("cannot find RuleActionStrategy for given action: " + strategyKey);
        }

        return strategy;
    }
}
