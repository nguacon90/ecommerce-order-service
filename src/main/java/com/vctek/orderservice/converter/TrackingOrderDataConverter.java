package com.vctek.orderservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.kafka.data.OrderData;
import com.vctek.orderservice.dto.TrackingOrderData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TrackingOrderDataConverter extends AbstractPopulatingConverter<OrderData, TrackingOrderData> {

    @Autowired
    private Populator<OrderData, TrackingOrderData> trackingOrderDataPopulator;
    @Override
    public void setTargetClass() {
        super.setTargetClass(TrackingOrderData.class);
    }

    @Override
    public void setPopulators() {
        super.setPopulators(trackingOrderDataPopulator);
    }
}
