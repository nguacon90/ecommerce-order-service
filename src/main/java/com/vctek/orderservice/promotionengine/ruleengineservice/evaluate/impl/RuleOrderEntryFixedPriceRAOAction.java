package com.vctek.orderservice.promotionengine.ruleengineservice.evaluate.impl;

import com.vctek.orderservice.promotionengine.ruleengine.model.eveluation.RuleActionContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.AbstractRuleActionRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.FixedPriceProductRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.OrderEntryRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.RuleEngineResultRAO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component("ruleOrderEntryFixedPriceRAOAction")
public class RuleOrderEntryFixedPriceRAOAction extends AbstractRuleExecutableSupport {

    public boolean performActionInternal(RuleActionContext context) {
        Set<OrderEntryRAO> orderEntries = context.getValues(OrderEntryRAO.class);
        Map<String, BigDecimal> values = (Map)context.getParameter("value");
        boolean isPerformed = false;
        Iterator var6 = orderEntries.iterator();

        while(var6.hasNext()) {
            OrderEntryRAO orderEntry = (OrderEntryRAO)var6.next();
            BigDecimal valueForCurrency = values.get(orderEntry.getCurrencyIsoCode());
            if (Objects.nonNull(valueForCurrency)) {
                isPerformed |= this.processOrderEntry(context, orderEntry, valueForCurrency);
            }
        }

        return isPerformed;
    }

    protected boolean processOrderEntry(RuleActionContext context, OrderEntryRAO orderEntryRao, BigDecimal valueForCurrency) {
        boolean isPerformed = false;
        int consumableQuantity = this.getConsumableQuantity(orderEntryRao);
        if (consumableQuantity > 0) {
            isPerformed = true;
            FixedPriceProductRAO fixedPriceProductRAO = this.getRuleEngineCalculationService().addFixedPriceEntryDiscount(orderEntryRao, valueForCurrency, consumableQuantity);
            this.setRAOMetaData(context, new AbstractRuleActionRAO[]{fixedPriceProductRAO});
            RuleEngineResultRAO result = context.getRuleEngineResultRao();
            result.getActions().add(fixedPriceProductRAO);
            this.consumeOrderEntry(orderEntryRao, fixedPriceProductRAO);
            this.getRuleEngineCalculationService().calculateTotals(orderEntryRao.getOrder());
            context.scheduleForUpdate(new Object[]{orderEntryRao, orderEntryRao.getOrder(), result});
            context.insertFacts(new Object[]{fixedPriceProductRAO, orderEntryRao.getQuantity()});
        }

        return isPerformed;
    }
}
