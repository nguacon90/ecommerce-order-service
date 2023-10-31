package com.vctek.orderservice.strategy;

import com.vctek.orderservice.dto.CommerceAbstractOrderParameter;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.model.OrderModel;

import java.util.Map;

public interface CommerceCartCalculationStrategy {
    /**
     * Using recalculate cart, only recalculate entry that haven't been calculated
     * @param parameter
     * @return
     */
    boolean calculateCart(final CommerceAbstractOrderParameter parameter);

    boolean recalculateCart(final CommerceAbstractOrderParameter parameter);

    void splitOrderPromotionToEntries(AbstractOrderModel orderModel);

    void calculateLoyaltyRewardOrder(OrderModel orderModel);

    Double calculateTotalRewardAmount(OrderModel orderModel);

    Map<Long, Double> doAppliedCartTemp(CartModel cartModel);

}
