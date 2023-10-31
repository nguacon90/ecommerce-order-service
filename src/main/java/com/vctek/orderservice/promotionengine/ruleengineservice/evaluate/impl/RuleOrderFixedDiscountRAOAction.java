package com.vctek.orderservice.promotionengine.ruleengineservice.evaluate.impl;

import com.vctek.orderservice.promotionengine.ruleengine.model.eveluation.RuleActionContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.AbstractRuleActionRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.CartRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.DiscountRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.RuleEngineResultRAO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

@Component("ruleOrderFixedDiscountRAOAction")
public class RuleOrderFixedDiscountRAOAction extends AbstractRuleExecutableSupport {

    public boolean performActionInternal(RuleActionContext context) {
        Map<String, BigDecimal> values = (Map)context.getParameter("value");
        CartRAO cartRAO = context.getCartRao();
        BigDecimal discountValueForCartCurrency = values.get(cartRAO.getCurrencyIsoCode());
        return Objects.nonNull(discountValueForCartCurrency) && this.performAction(context, discountValueForCartCurrency);
    }

    protected boolean performAction(RuleActionContext context, BigDecimal amount) {
        CartRAO cartRAO = context.getCartRao();
        if (CollectionUtils.isNotEmpty(cartRAO.getEntries())) {
            RuleEngineResultRAO result = context.getRuleEngineResultRao();
            DiscountRAO discount = this.getRuleEngineCalculationService().addOrderLevelDiscount(cartRAO, isAbsolute(), amount);
            result.getActions().add(discount);
            this.setRAOMetaData(context, new AbstractRuleActionRAO[]{discount});
            context.scheduleForUpdate(new Object[]{cartRAO, result});
            context.insertFacts(new Object[]{discount});
            return true;
        }

        return false;
    }

    protected boolean isAbsolute() {
        return true;
    }
}
