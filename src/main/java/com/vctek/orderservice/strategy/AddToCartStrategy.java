package com.vctek.orderservice.strategy;

import com.vctek.orderservice.dto.CommerceAbstractOrderParameter;
import com.vctek.orderservice.dto.CommerceCartModification;

public interface AddToCartStrategy {
    CommerceCartModification addToCart(CommerceAbstractOrderParameter parameter);

    CommerceCartModification addEntryToOrder(CommerceAbstractOrderParameter parameter);

    void changeOrderEntryToComboEntry(CommerceAbstractOrderParameter cartParameter);
}
