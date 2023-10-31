package com.vctek.orderservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.OrderData;
import com.vctek.orderservice.model.OrderModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderConverter extends AbstractPopulatingConverter<OrderModel, OrderData> {

    @Autowired
    private Populator<OrderModel, OrderData> orderPopulator;

    @Override
    public void setTargetClass() {
        setTargetClass(OrderData.class);
    }

    @Override
    public void setPopulators() {
        setPopulators(orderPopulator);
    }
}
