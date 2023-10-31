package com.vctek.orderservice.promotionengine.promotionservice.model.mapper;

import java.math.BigInteger;

public interface ConsumeBudgetMapper {
    BigInteger getSourceRuleId();

    Double getTotalDiscountAmount();
}
