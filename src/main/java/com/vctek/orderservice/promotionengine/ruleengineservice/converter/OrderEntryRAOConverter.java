package com.vctek.orderservice.promotionengine.ruleengineservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.OrderEntryRAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderEntryRAOConverter extends AbstractPopulatingConverter<AbstractOrderEntryModel, OrderEntryRAO> {

    @Autowired
    private Populator<AbstractOrderEntryModel, OrderEntryRAO> orderEntryRaoPopulator;

    @Override
    public void setTargetClass() {
        super.setTargetClass(OrderEntryRAO.class);
    }

    @Override
    public void setPopulators() {
        super.setPopulators(orderEntryRaoPopulator);
    }
}
