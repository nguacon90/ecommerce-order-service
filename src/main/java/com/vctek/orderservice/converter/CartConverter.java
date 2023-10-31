package com.vctek.orderservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.CartData;
import com.vctek.orderservice.model.CartModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CartConverter extends AbstractPopulatingConverter<CartModel, CartData> {

    @Autowired
    private Populator<CartModel, CartData> cartPopulator;

    @Override
    public void setTargetClass() {
        setTargetClass(CartData.class);
    }

    @Override
    public void setPopulators() {
        setPopulators(cartPopulator);
    }
}
