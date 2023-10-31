package com.vctek.orderservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.elasticsearch.model.OrderSearchModel;
import com.vctek.orderservice.model.OrderModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("orderSearchEntryModelConverter")
public class OrderSearchEntryModelConverter extends AbstractPopulatingConverter<OrderModel, OrderSearchModel> {

    @Autowired
    @Qualifier("orderSearchEntryModelPopulator")
    private Populator<OrderModel, OrderSearchModel> orderSearchEntryModelPopulator;

    @Override
    public void setTargetClass() {
        super.setTargetClass(OrderSearchModel.class);
    }

    @Override
    public void setPopulators() {
        super.setPopulators(orderSearchEntryModelPopulator);
    }
}
