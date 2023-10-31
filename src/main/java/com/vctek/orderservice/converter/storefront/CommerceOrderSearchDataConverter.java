package com.vctek.orderservice.converter.storefront;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.request.storefront.CommerceOrderData;
import com.vctek.orderservice.elasticsearch.model.OrderSearchModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommerceOrderSearchDataConverter extends AbstractPopulatingConverter<OrderSearchModel, CommerceOrderData> {
    @Autowired
    private Populator<OrderSearchModel, CommerceOrderData> commerceOrderSearchDataPopulator;

    @Override
    public void setTargetClass() {
        setTargetClass(CommerceOrderData.class);
    }

    @Override
    public void setPopulators() {
        setPopulators(commerceOrderSearchDataPopulator);
    }
}
