package com.vctek.orderservice.promotionengine.promotionservice.strategy.impl;

import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.ItemModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionResultModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.RuleBasedOrderEntryAdjustActionModel;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionActionService;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.AbstractRuleActionRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.DiscountRAO;
import com.vctek.orderservice.service.CalculationService;
import com.vctek.orderservice.service.ModelService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Component("defaultOrderEntryAdjustActionStrategy")
public class DefaultOrderEntryAdjustActionStrategy extends AbstractRuleActionStrategy<RuleBasedOrderEntryAdjustActionModel> {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultOrderEntryAdjustActionStrategy.class);

    public DefaultOrderEntryAdjustActionStrategy(ModelService modelService,
                                                 PromotionActionService promotionActionService,
                                                 CalculationService calculationService) {
        super(modelService, promotionActionService, calculationService);
    }

    public List<PromotionResultModel> apply(AbstractRuleActionRAO action) {
        if (!(action instanceof DiscountRAO)) {
            LOG.warn("cannot apply {}, action is not of type DiscountRAO", this.getClass().getSimpleName());
            return Collections.emptyList();
        }

        AbstractOrderEntryModel entry = this.getPromotionActionService().getOrderEntry(action);
        if (entry == null) {
            LOG.warn("cannot apply {}, orderEntry could not be found.", this.getClass().getSimpleName());
            return Collections.emptyList();
        }

        PromotionResultModel promoResult = this.getPromotionActionService().createPromotionResult(action);
        if (promoResult == null) {
            LOG.warn("cannot apply {}, promotionResult could not be created.", this.getClass().getSimpleName());
            return Collections.emptyList();
        }
        AbstractOrderModel order = entry.getOrder();
        if (order == null) {
            LOG.warn("cannot apply {}, order does not exist for order entry", this.getClass().getSimpleName());

            return Collections.emptyList();
        }

        DiscountRAO discountRao = (DiscountRAO) action;
        BigDecimal discountAmount = discountRao.getValue();
        this.adjustDiscountRaoValue(entry, discountRao, discountAmount);
        RuleBasedOrderEntryAdjustActionModel actionModel = this.createOrderEntryAdjustAction(promoResult, action, entry, discountAmount);
        this.handleActionMetadata(action, actionModel);
        this.getPromotionActionService().createDiscountValue(discountRao, actionModel.getGuid(), entry);
        this.getModelService().saveAll(new Object[]{promoResult, actionModel, order, entry});
        return Collections.singletonList(promoResult);
    }

    protected void adjustDiscountRaoValue(AbstractOrderEntryModel entry, DiscountRAO discountRao, BigDecimal discountAmount) {
        BigDecimal amount = discountAmount;
        if (discountRao.isPerUnit()) {
            long appliedToQuantity = discountRao.getAppliedToQuantity();
            BigDecimal fraction = BigDecimal.valueOf((double) appliedToQuantity / (double) entry.getQuantity());
            amount = discountAmount.multiply(fraction);
        }

        discountRao.setValue(amount);
    }

    protected RuleBasedOrderEntryAdjustActionModel createOrderEntryAdjustAction(PromotionResultModel promoResult, AbstractRuleActionRAO action, AbstractOrderEntryModel entry, BigDecimal discountAmount) {
        RuleBasedOrderEntryAdjustActionModel actionModel = this.createPromotionAction(promoResult, action);
        actionModel.setAmount(discountAmount);
        actionModel.setOrderEntryNumber(entry.getEntryNumber());
        actionModel.setProductId(entry.getProductId());
        actionModel.setOrderEntryQuantity(this.getConsumedQuantity(promoResult, entry));
        return actionModel;
    }

    public void undo(ItemModel action) {
        if (action instanceof RuleBasedOrderEntryAdjustActionModel) {
            this.handleUndoActionMetadata((RuleBasedOrderEntryAdjustActionModel) action);
            this.undoInternal((RuleBasedOrderEntryAdjustActionModel) action);
        }

    }

    @Override
    Class<RuleBasedOrderEntryAdjustActionModel> forClass() {
        return RuleBasedOrderEntryAdjustActionModel.class;
    }
}