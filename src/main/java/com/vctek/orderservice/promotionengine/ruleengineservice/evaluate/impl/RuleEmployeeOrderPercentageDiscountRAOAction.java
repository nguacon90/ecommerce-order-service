package com.vctek.orderservice.promotionengine.ruleengineservice.evaluate.impl;

import com.vctek.orderservice.dto.ConsumeBudgetParam;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionBudgetConsumeService;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionSourceRuleService;
import com.vctek.orderservice.promotionengine.ruleengine.model.eveluation.RuleActionContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.*;
import com.vctek.orderservice.promotionengine.util.CommonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

@Component("ruleEmployeeOrderPercentageDiscountRAOAction")
public class RuleEmployeeOrderPercentageDiscountRAOAction extends AbstractRuleExecutableSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(RuleEmployeeOrderPercentageDiscountRAOAction.class);
    private PromotionBudgetConsumeService promotionBudgetConsumeService;
    private PromotionSourceRuleService promotionSourceRuleService;
    public boolean performActionInternal(RuleActionContext context) {
        BigDecimal value = (BigDecimal) context.getParameter("value");
        return Objects.nonNull(value) && this.performAction(context, value);
    }

    protected boolean performAction(RuleActionContext context, BigDecimal amount) {
        CartRAO cartRAO = context.getCartRao();
        if (CollectionUtils.isNotEmpty(cartRAO.getEntries())) {
            RuleEngineResultRAO result = context.getRuleEngineResultRao();
            String ruleCode = getRuleCode(context);
            PromotionSourceRuleModel sourceRuleModel = promotionSourceRuleService.findByCode(ruleCode);
            if(sourceRuleModel == null) {
                LOGGER.error("Cannot found source rule : {}", ruleCode);
                return false;
            }

            BigDecimal limitedAmount = getLimitedAmount(context, cartRAO, amount, sourceRuleModel);
            DiscountRAO discount = this.getRuleEngineCalculationService().addOrderLevelDiscount(cartRAO, true, limitedAmount);
            LOGGER.debug("DiscountRAO: {}", discount.getValue().doubleValue());
            this.consumeBudget(discount, cartRAO, limitedAmount, sourceRuleModel);
            result.getActions().add(discount);
            this.setRAOMetaData(context, new AbstractRuleActionRAO[]{discount});
            context.scheduleForUpdate(new Object[]{cartRAO, result});
            context.insertFacts(new Object[]{discount});
            return true;
        }

        return false;
    }

    private void consumeBudget(DiscountRAO discount, CartRAO cartRAO, BigDecimal limitedAmount, PromotionSourceRuleModel sourceRuleModel) {
        UserRAO user = cartRAO.getUser();
        if(user == null || user.getId() == null) {
            return;
        }
        BudgetConsumedRAO budgetConsumedRAO = new BudgetConsumedRAO();
        budgetConsumedRAO.setFiredRuleCode(sourceRuleModel.getCode());
        budgetConsumedRAO.setCustomerId(user.getId());
        budgetConsumedRAO.setDiscountAmount(limitedAmount);
        budgetConsumedRAO.setMonth(CommonUtils.getMonth(cartRAO.getCreatedDate()));
        budgetConsumedRAO.setYear(CommonUtils.getYear(cartRAO.getCreatedDate()));
        budgetConsumedRAO.setOrderCode(cartRAO.getCode());

        if(sourceRuleModel != null) {
            budgetConsumedRAO.setPromotionSourceRuleId(sourceRuleModel.getId());
        }
        discount.setConsumedBudget(budgetConsumedRAO);
    }

    protected BigDecimal getLimitedAmount(RuleActionContext context, CartRAO cartRAO, BigDecimal amount, PromotionSourceRuleModel sourceRuleModel) {
        BigDecimal maxDiscountAmount = (BigDecimal) context.getParameter(MAX_DISCOUNT_AMOUNT);
        if(maxDiscountAmount == null) {
            return BigDecimal.ZERO;
        }
        String ruleCode = this.getRuleCode(context);
        UserRAO user = cartRAO.getUser();
        if(user == null) {
            return BigDecimal.ZERO;
        }

        ConsumeBudgetParam param = new ConsumeBudgetParam();
        param.setCustomerId(user.getId());
        param.setRuleCode(ruleCode);
        param.setCreatedOrderDate(cartRAO.getCreatedDate());
        BigDecimal consumedAmount = promotionBudgetConsumeService.calculateConsumeBudgetAmount(param);
        BigDecimal remainAmount = maxDiscountAmount.subtract(consumedAmount);
        if(remainAmount.doubleValue() <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal totalPrice = sourceRuleModel.isAppliedOnlyOne() ? cartRAO.getOriginalTotal() : cartRAO.getTotal();
        BigDecimal discount = amount.multiply(totalPrice).divide(BigDecimal.valueOf(100l));
        if(discount.doubleValue() >= remainAmount.doubleValue()) {
            return remainAmount;
        }
        return discount;
    }

    @Autowired
    public void setPromotionBudgetConsumeService(PromotionBudgetConsumeService promotionBudgetConsumeService) {
        this.promotionBudgetConsumeService = promotionBudgetConsumeService;
    }

    @Autowired
    public void setPromotionSourceRuleService(PromotionSourceRuleService promotionSourceRuleService) {
        this.promotionSourceRuleService = promotionSourceRuleService;
    }
}
