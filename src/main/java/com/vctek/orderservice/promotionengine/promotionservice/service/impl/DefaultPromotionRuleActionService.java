package com.vctek.orderservice.promotionengine.promotionservice.service.impl;

import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.ItemModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionResultModel;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionActionService;
import com.vctek.orderservice.promotionengine.ruleengineservice.action.impl.DefaultRuleActionService;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.RuleEngineResultRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.strategy.RuleActionStrategy;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("promotionRuleActionService")
public class DefaultPromotionRuleActionService extends DefaultRuleActionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPromotionRuleActionService.class);
    private PromotionActionService promotionActionService;

    public DefaultPromotionRuleActionService(@Qualifier("actionStrategiesMapping") Map<String, RuleActionStrategy> actionStrategiesMapping,
                                             PromotionActionService promotionActionService) {
        super(actionStrategiesMapping);
        this.promotionActionService = promotionActionService;
    }

    public List<ItemModel> applyAllActions(RuleEngineResultRAO ruleEngineResultRAO) {
//        long startAppliedAction = System.currentTimeMillis();
        List<ItemModel> actionResults = super.applyAllActions(ruleEngineResultRAO);
//        long endAppliedAction = System.currentTimeMillis();
//        LOGGER.info(" ==== Applied action: {}", (endAppliedAction - startAppliedAction));

        this.recalculateTotals(actionResults);

        return actionResults;
    }

    protected void recalculateTotals(List<ItemModel> actionResults) {
        if (CollectionUtils.isNotEmpty(actionResults)) {
            ItemModel item = actionResults.get(0);
            if (!(item instanceof PromotionResultModel)) {
                LOGGER.error("Can not recalculate totals. Action result is not PromotionResultModel " + item.toString());
                return;
            }

            PromotionResultModel promotionResult = (PromotionResultModel)item;
            AbstractOrderModel order = promotionResult.getOrder();
            if (order == null) {
                LOGGER.error("Can not recalculate totals. No order found for PromotionResult: " + promotionResult.toString());
                return;
            }

            this.promotionActionService.recalculateTotals(order);
        }

    }
}
