package com.vctek.orderservice.promotionengine.ruleengineservice.evaluate.impl;

import com.vctek.orderservice.promotionengine.ruleengine.model.eveluation.RuleActionContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

@Component("ruleOrderPercentageDiscountRAOAction")
public class RuleOrderPercentageDiscountRAOAction extends RuleOrderFixedDiscountRAOAction {

    @Override
    public boolean performActionInternal(RuleActionContext context) {
        BigDecimal value = (BigDecimal) context.getParameter("value");
        return Objects.nonNull(value) && this.performAction(context, value);
    }

    @Override
    protected boolean isAbsolute() {
        return false;
    }
}
