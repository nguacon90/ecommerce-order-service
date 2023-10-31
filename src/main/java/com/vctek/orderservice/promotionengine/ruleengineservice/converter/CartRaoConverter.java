package com.vctek.orderservice.promotionengine.ruleengineservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.CartRAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class CartRaoConverter extends AbstractPopulatingConverter<AbstractOrderModel, CartRAO> {

    @Autowired
    @Qualifier("cartRaoPopulator")
    private Populator<AbstractOrderModel, CartRAO> cartRaoPopulator;

    @Autowired
    @Qualifier("couponRaoPopulator")
    private Populator<AbstractOrderModel, CartRAO> couponRaoPopulator;

    @Override
    public void setTargetClass() {
        super.setTargetClass(CartRAO.class);
    }

    @Override
    public void setPopulators() {
        super.setPopulators(cartRaoPopulator, couponRaoPopulator);
    }
}
