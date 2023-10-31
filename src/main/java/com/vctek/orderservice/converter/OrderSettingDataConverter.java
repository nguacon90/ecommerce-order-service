package com.vctek.orderservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.OrderSettingData;
import com.vctek.orderservice.model.OrderSettingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderSettingDataConverter extends AbstractPopulatingConverter<OrderSettingModel, OrderSettingData> {

    @Autowired
    private Populator<OrderSettingModel, OrderSettingData> orderSettingDataPopulator;

    @Override
    public void setTargetClass() {
        setTargetClass(OrderSettingData.class);
    }

    @Override
    public void setPopulators() {
        setPopulators(orderSettingDataPopulator);
    }

}
