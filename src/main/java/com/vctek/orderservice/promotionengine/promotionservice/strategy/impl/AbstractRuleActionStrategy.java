package com.vctek.orderservice.promotionengine.promotionservice.strategy.impl;

import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.AbstractRuleBasedPromotionActionModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionResultModel;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionActionService;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.CalculationException;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.AbstractRuleActionRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.strategy.RuleActionStrategy;
import com.vctek.orderservice.service.CalculationService;
import com.vctek.orderservice.service.ModelService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;

import java.util.UUID;

public abstract class AbstractRuleActionStrategy<A extends AbstractRuleBasedPromotionActionModel> implements RuleActionStrategy, BeanNameAware {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractRuleActionStrategy.class);
    private ModelService modelService;
    private PromotionActionService promotionActionService;
    private CalculationService calculationService;
    private Boolean forceImmediateRecalculation;
    private String beanName;

    public AbstractRuleActionStrategy(ModelService modelService, PromotionActionService promotionActionService, CalculationService calculationService) {
        this.modelService = modelService;
        this.promotionActionService = promotionActionService;
        this.calculationService = calculationService;
        this.forceImmediateRecalculation = Boolean.TRUE;
    }

    protected A createPromotionAction(PromotionResultModel promotionResult, AbstractRuleActionRAO action) {
        A result = this.getInstance();
        result.setPromotionResult(promotionResult);
        result.setGuid(createActionUUID());
        result.setRule(this.getPromotionActionService().getRule(action));
        result.setMarkedApplied(Boolean.TRUE);
        result.setStrategyId(this.getStrategyId());
        return result;
    }

    public PromotionActionService getPromotionActionService() {
        return promotionActionService;
    }

    public ModelService getModelService() {
        return modelService;
    }

    protected void handleActionMetadata(AbstractRuleActionRAO action, AbstractRuleBasedPromotionActionModel actionModel) {
        //NOSONAR
    }
    protected AbstractOrderModel undoInternal(A action) {
        PromotionResultModel promoResult = action.getPromotionResult();
        AbstractOrderModel order = promoResult.getOrder();
        this.getPromotionActionService().removeDiscountValue(action.getGuid(), order);
//            this.getModelService().saveAll(modifiedItems);
//        } catch (ObjectOptimisticLockingFailureException e) {
//            this.getModelService().saveAll(modifiedItems);//retry save again
//        }
        return order;
    }

    protected static String createActionUUID() {
        return "Action[" + UUID.randomUUID().toString() + "]";
    }

    protected boolean recalculateIfNeeded(AbstractOrderModel order) {
        if (BooleanUtils.isTrue(this.getForceImmediateRecalculation())) {
            try {
                this.calculationService.calculateTotals(order, true);
            } catch (CalculationException var3) {
                LOG.error(String.format("Recalculation of order with code '%s' failed.", order.getCode()), var3);
                order.setCalculated(Boolean.FALSE);
                this.getModelService().save(order);
                return false;
            }
        }

        return true;
    }

    protected void handleUndoActionMetadata(AbstractRuleBasedPromotionActionModel action) {
        //NOSONAR
    }

    protected A getInstance() {
        try {
            return this.forClass().newInstance();
        } catch (IllegalAccessException | InstantiationException var3) {
            throw new IllegalArgumentException("could not instantiate class " + this.forClass().getSimpleName(), var3);
        }
    }

    protected long getConsumedQuantity(PromotionResultModel promoResult, AbstractOrderEntryModel entry) {
        long consumedQuantity = 0L;
        if (CollectionUtils.isNotEmpty(promoResult.getConsumedEntries())) {
            consumedQuantity = promoResult.getConsumedEntries().stream()
                    .filter(ce -> ce.getOrderEntry() != null && entry.getId().equals(ce.getOrderEntry().getId()))
                    .mapToLong((consumedEntry) -> consumedEntry.getQuantity()).sum();
        }

        return consumedQuantity;
    }

    abstract Class<A> forClass();

    public Boolean getForceImmediateRecalculation() {
        return forceImmediateRecalculation;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    @Override
    public String getStrategyId() {
        return this.beanName;
    }
}
