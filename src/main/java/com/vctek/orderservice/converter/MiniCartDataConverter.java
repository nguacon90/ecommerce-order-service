package com.vctek.orderservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.MiniCartData;
import com.vctek.orderservice.model.CartModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("miniCartDataConverter")
public class MiniCartDataConverter extends AbstractPopulatingConverter<CartModel, MiniCartData> {

    @Autowired
    @Qualifier("miniCartPopulator")
    private Populator<CartModel, MiniCartData> miniCartPopulator;

    @Override
    public void setTargetClass() {
        setTargetClass(MiniCartData.class);
    }

    @Override
    public void setPopulators() {
        setPopulators(miniCartPopulator);
    }
}
