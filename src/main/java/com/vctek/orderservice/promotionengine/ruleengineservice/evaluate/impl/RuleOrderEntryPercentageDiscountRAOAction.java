package com.vctek.orderservice.promotionengine.ruleengineservice.evaluate.impl;

import com.vctek.orderservice.promotionengine.ruleengine.model.eveluation.RuleActionContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.AbstractRuleActionRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.DiscountRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.OrderEntryRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.RuleEngineResultRAO;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

@Component("ruleOrderEntryPercentageDiscountRAOAction")
public class RuleOrderEntryPercentageDiscountRAOAction extends AbstractRuleExecutableSupport {

    public boolean performActionInternal(RuleActionContext context) {
        boolean isPerformed = false;
        Optional<BigDecimal> amount = this.extractAmountForCurrency(context, context.getParameter("value"));
        if (!amount.isPresent()) {
            return isPerformed;
        }

        Set<OrderEntryRAO> orderEntries = context.getValues(OrderEntryRAO.class);
        if (CollectionUtils.isEmpty(orderEntries)) {
            return isPerformed;
        }

        Long maximumQuantity = getMaximumQuantity(context);
        if(maximumQuantity != null) {
            return isPerformed || performLimitedAction(context, orderEntries, maximumQuantity, amount.get(), false);
        }

        OrderEntryRAO orderEntryRAO;
        if (CollectionUtils.isNotEmpty(orderEntries)) {
            for (Iterator var6 = orderEntries.iterator(); var6.hasNext();
                 isPerformed |= this.processOrderEntry(context, orderEntryRAO, amount.get())) {
                orderEntryRAO = (OrderEntryRAO) var6.next();
            }
        }

        return isPerformed;
    }

    protected boolean processOrderEntry(RuleActionContext context, OrderEntryRAO orderEntryRao, BigDecimal value) {
        boolean isPerformed = false;
        int consumableQuantity = this.getConsumableQuantity(orderEntryRao);
        if (consumableQuantity > 0) {
            DiscountRAO discount = this.getRuleEngineCalculationService().addOrderEntryLevelDiscount(orderEntryRao, false, value);
            this.setRAOMetaData(context, new AbstractRuleActionRAO[]{discount});
            this.consumeOrderEntry(orderEntryRao, consumableQuantity, this.adjustUnitPrice(orderEntryRao, consumableQuantity), discount);
            RuleEngineResultRAO result = context.getValue(RuleEngineResultRAO.class);
            result.getActions().add(discount);
            context.scheduleForUpdate(new Object[]{orderEntryRao, orderEntryRao.getOrder(), result});
            context.insertFacts(new Object[]{discount});
            context.insertFacts(discount.getConsumedEntries());
            isPerformed = true;
        }

        return isPerformed;
    }
}
