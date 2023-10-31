package com.vctek.orderservice.promotionengine.ruleengineservice.calculation.impl;

import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.MinimumAmountValidationStrategy;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain.LineItem;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain.LineItemDiscount;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain.Order;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain.OrderDiscount;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DefaultMinimumAmountValidationStrategy implements MinimumAmountValidationStrategy {
    private static final BigDecimal DEFAULT_ORDER_LOWER_LIMIT_AMOUNT;
    private static final BigDecimal DEFAULT_LINE_ITEM_LOWER_LIMIT_AMOUNT;
    private BigDecimal orderLowerLimitAmount;
    private BigDecimal lineItemLowerLimitAmount;

    static {
        DEFAULT_ORDER_LOWER_LIMIT_AMOUNT = BigDecimal.ZERO;
        DEFAULT_LINE_ITEM_LOWER_LIMIT_AMOUNT = BigDecimal.ZERO;
    }

    public DefaultMinimumAmountValidationStrategy() {
        this.orderLowerLimitAmount = DEFAULT_ORDER_LOWER_LIMIT_AMOUNT;
        this.lineItemLowerLimitAmount = DEFAULT_LINE_ITEM_LOWER_LIMIT_AMOUNT;
    }

    public boolean isOrderLowerLimitValid(Order order, OrderDiscount discount) {
        if (order.getDiscounts().contains(discount)) {
            throw new IllegalArgumentException("The order already has the discount.");
        } else {
            boolean var4;
            try {
                order.addDiscount(discount);
                var4 = this.isOrderLowerLimitValid(order);
            } finally {
                order.removeDiscount(discount);
            }

            return var4;
        }
    }

    protected boolean isOrderLowerLimitValid(Order order) {
        return order.getSubTotal().subtract(order.getTotalDiscount()).getAmount()
                .compareTo(this.getOrderLowerLimitAmount()) >= 0;
    }

    public boolean isLineItemLowerLimitValid(LineItem lineItem, LineItemDiscount discount) {
        if (lineItem.getDiscounts().contains(discount)) {
            throw new IllegalArgumentException("The line item already has the discount.");
        } else {
            boolean var4;
            try {
                lineItem.addDiscount(discount);
                var4 = this.isLineItemLowerLimitValid(lineItem) && this.isOrderLowerLimitValid(lineItem.getOrder());
            } finally {
                lineItem.removeDiscount(discount);
            }

            return var4;
        }
    }

    protected boolean isLineItemLowerLimitValid(LineItem lineItem) {
        return lineItem.getSubTotal().subtract(lineItem.getTotalDiscount()).getAmount().compareTo(this.getLineItemLowerLimitAmount()) >= 0;
    }

    protected BigDecimal getOrderLowerLimitAmount() {
        return this.orderLowerLimitAmount;
    }

    public void setOrderLowerLimitAmount(BigDecimal orderLowerLimitAmount) {
        this.orderLowerLimitAmount = orderLowerLimitAmount;
    }

    protected BigDecimal getLineItemLowerLimitAmount() {
        return this.lineItemLowerLimitAmount;
    }

    public void setLineItemLowerLimitAmount(BigDecimal lineItemLowerLimitAmount) {
        this.lineItemLowerLimitAmount = lineItemLowerLimitAmount;
    }
}
