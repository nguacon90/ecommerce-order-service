package com.vctek.orderservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.OrderSourceData;
import com.vctek.orderservice.model.OrderSourceModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderSourceConverter extends AbstractPopulatingConverter<OrderSourceModel, OrderSourceData> {

    @Autowired
    private Populator<OrderSourceModel, OrderSourceData> orderSourceDataPopulator;
    @Override
    public void setTargetClass() {
        super.setTargetClass(OrderSourceData.class);
    }

    @Override
    public void setPopulators() {
        super.setPopulators(orderSourceDataPopulator);
    }
}
