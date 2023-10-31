package com.vctek.orderservice.promotionengine.promotionservice.strategy.impl;

import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.ItemModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionResultModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.RuleBasedOrderAdjustTotalActionModel;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionActionService;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.AbstractRuleActionRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.DiscountRAO;
import com.vctek.orderservice.service.CalculationService;
import com.vctek.orderservice.service.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component("orderTotalAdjustActionStrategy")
public class DefaultOrderAdjustTotalActionStrategy extends AbstractRuleActionStrategy<RuleBasedOrderAdjustTotalActionModel> {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultOrderAdjustTotalActionStrategy.class);

    public DefaultOrderAdjustTotalActionStrategy(ModelService modelService,
                                                 PromotionActionService promotionActionService,
                                                 CalculationService calculationService) {
        super(modelService, promotionActionService, calculationService);
    }

    @Override
    public Class<RuleBasedOrderAdjustTotalActionModel> forClass() {
        return RuleBasedOrderAdjustTotalActionModel.class;
    }

    @Override
    public List<Object> apply(AbstractRuleActionRAO action) {
        if (!(action instanceof DiscountRAO)) {
            LOG.warn("cannot apply {}, action is not of type DiscountRAO", this.getClass().getSimpleName());
            return Collections.emptyList();
        }

        PromotionResultModel promoResult = this.getPromotionActionService().createPromotionResult(action);
        if (promoResult == null) {
            LOG.warn("cannot apply {}, promotionResult could not be created.", this.getClass().getSimpleName());
            return Collections.emptyList();
        }

        AbstractOrderModel order = promoResult.getOrder();
        if (order == null) {
            LOG.warn("cannot apply {}, order not found", this.getClass().getSimpleName());
            return Collections.emptyList();
        }

        DiscountRAO discountRao = (DiscountRAO) action;
        RuleBasedOrderAdjustTotalActionModel actionModel = this.createOrderAdjustTotalAction(promoResult, discountRao);
        this.handleActionMetadata(action, actionModel);
        this.getPromotionActionService().createDiscountValue(discountRao, actionModel.getGuid(), order);
        this.getModelService().saveAll(new Object[]{promoResult, actionModel, order});
        return Collections.singletonList(promoResult);

    }

    protected RuleBasedOrderAdjustTotalActionModel createOrderAdjustTotalAction(PromotionResultModel promoResult, DiscountRAO discountRao) {
        RuleBasedOrderAdjustTotalActionModel actionModel = this.createPromotionAction(promoResult, discountRao);
        actionModel.setAmount(discountRao.getValue());
        return actionModel;
    }

    @Override
    public void undo(ItemModel action) {
        if (action instanceof RuleBasedOrderAdjustTotalActionModel) {
            this.undoInternal((RuleBasedOrderAdjustTotalActionModel)action);
        }
    }

}
