package com.vctek.orderservice.strategy;

import com.vctek.orderservice.dto.CommerceAbstractOrderParameter;

public interface CommerceUpdateCartStrategy {
    void updateCartDiscount(CommerceAbstractOrderParameter commerceAbtractOrderParameter);

    void updateVat(CommerceAbstractOrderParameter commerceAbtractOrderParameter);
}
