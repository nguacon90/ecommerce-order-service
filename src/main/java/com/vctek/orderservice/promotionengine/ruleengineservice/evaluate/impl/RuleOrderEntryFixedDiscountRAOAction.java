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
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component("ruleOrderEntryFixedDiscountRAOAction")
public class RuleOrderEntryFixedDiscountRAOAction extends AbstractRuleExecutableSupport {

    @Override
    public boolean performActionInternal(RuleActionContext context) {
        Set<OrderEntryRAO> orderEntries = context.getValues(OrderEntryRAO.class);
        Map<String, BigDecimal> values = (Map) context.getParameter("value");
        boolean isPerformed = false;
        Iterator var6 = orderEntries.iterator();

        if (CollectionUtils.isEmpty(orderEntries)) {
            return isPerformed;
        }

        Long maximumQuantity = getMaximumQuantity(context);

        BigDecimal valueForCurrency = values.get(context.getCartRao().getCurrencyIsoCode());
        if (maximumQuantity != null) {
            return isPerformed || performLimitedAction(context, orderEntries, maximumQuantity, valueForCurrency, true);
        }

        while (var6.hasNext()) {
            OrderEntryRAO orderEntry = (OrderEntryRAO) var6.next();
            if (Objects.nonNull(valueForCurrency)) {
                isPerformed |= this.performAction(context, orderEntry, valueForCurrency);
            }
        }
        return isPerformed;
    }

    protected boolean performAction(RuleActionContext context, OrderEntryRAO orderEntryRao, BigDecimal valueForCurrency) {
        boolean isPerformed = false;
        int consumableQuantity = this.getConsumableQuantity(orderEntryRao);
        if (consumableQuantity > 0) {
            isPerformed = true;
            DiscountRAO discount = this.getRuleEngineCalculationService().addOrderEntryLevelDiscount(orderEntryRao,
                    true, valueForCurrency);
            this.setRAOMetaData(context, new AbstractRuleActionRAO[]{discount});
            this.consumeOrderEntry(orderEntryRao, consumableQuantity, this.adjustUnitPrice(orderEntryRao, consumableQuantity), discount);
            RuleEngineResultRAO result = context.getRuleEngineResultRao();
            result.getActions().add(discount);
            context.scheduleForUpdate(new Object[]{orderEntryRao, orderEntryRao.getOrder(), result});
            context.insertFacts(new Object[]{discount});
            context.insertFacts(discount.getConsumedEntries());
        }

        return isPerformed;
    }
}