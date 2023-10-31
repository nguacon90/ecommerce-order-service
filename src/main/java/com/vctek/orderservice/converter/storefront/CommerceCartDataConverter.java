package com.vctek.orderservice.converter.storefront;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.CommerceCartData;
import com.vctek.orderservice.model.AbstractOrderModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommerceCartDataConverter extends AbstractPopulatingConverter<AbstractOrderModel, CommerceCartData> {
    @Autowired
    private Populator<AbstractOrderModel, CommerceCartData> commerceCartDataPopulator;

    @Override
    public void setTargetClass() {
        setTargetClass(CommerceCartData.class);
    }

    @Override
    public void setPopulators() {
        setPopulators(commerceCartDataPopulator);
    }
}
