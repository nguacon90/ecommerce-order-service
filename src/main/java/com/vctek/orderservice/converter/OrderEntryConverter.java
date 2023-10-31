package com.vctek.orderservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.OrderEntryData;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderEntryConverter extends AbstractPopulatingConverter<AbstractOrderEntryModel, OrderEntryData> {

    @Autowired
    private Populator<AbstractOrderEntryModel, OrderEntryData> orderEntryPopulator;

    @Override
    public void setTargetClass() {
        setTargetClass(OrderEntryData.class);
    }

    @Override
    public void setPopulators() {
        setPopulators(orderEntryPopulator);
    }
}
