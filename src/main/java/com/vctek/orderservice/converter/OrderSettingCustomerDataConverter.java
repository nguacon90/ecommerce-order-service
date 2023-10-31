package com.vctek.orderservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.OrderSettingCustomerData;
import com.vctek.orderservice.model.OrderSettingCustomerModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderSettingCustomerDataConverter extends AbstractPopulatingConverter<OrderSettingCustomerModel, OrderSettingCustomerData> {

    @Autowired
    private Populator<OrderSettingCustomerModel, OrderSettingCustomerData> populator;

    @Override
    public void setTargetClass() {
        setTargetClass(OrderSettingCustomerData.class);
    }

    @Override
    public void setPopulators() {
        setPopulators(populator);
    }

}
