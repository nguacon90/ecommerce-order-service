package com.vctek.orderservice.converter.storefront;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.MiniCartData;
import com.vctek.orderservice.model.CartModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("storefrontMiniCartConverter")
public class StorefrontMiniCartConverter extends AbstractPopulatingConverter<CartModel, MiniCartData> {
    @Autowired
    @Qualifier("storefrontMiniCartPopulator")
    private Populator<CartModel, MiniCartData> storefrontMiniCartPopulator;

    @Override
    public void setTargetClass() {
        setTargetClass(MiniCartData.class);
    }

    @Override
    public void setPopulators() {
        setPopulators(storefrontMiniCartPopulator);
    }
}
