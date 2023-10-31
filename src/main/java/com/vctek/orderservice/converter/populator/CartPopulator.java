package com.vctek.orderservice.converter.populator;

import com.vctek.orderservice.dto.CartData;
import com.vctek.orderservice.model.CartModel;
import org.springframework.stereotype.Component;

@Component
public class CartPopulator<T extends CartData> extends AbstractOrderPopulator<CartModel, T> {

    @Override
    public void populate(CartModel source, T target) {
        addCommon(source, target);
        addEntries(source, target);
        addPaymentTransactions(source, target);
        addPromotions(source, target);
        populateCouldFirePromotion(source, target);
        populateCouponCode(source, target);
    }

}
