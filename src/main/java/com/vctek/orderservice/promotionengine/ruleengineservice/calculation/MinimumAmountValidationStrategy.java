package com.vctek.orderservice.promotionengine.ruleengineservice.calculation;


import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain.LineItem;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain.LineItemDiscount;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain.Order;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain.OrderDiscount;

public interface MinimumAmountValidationStrategy {
    boolean isOrderLowerLimitValid(Order order, OrderDiscount discount);

    boolean isLineItemLowerLimitValid(LineItem lineItem, LineItemDiscount lineItemDiscount);
}
