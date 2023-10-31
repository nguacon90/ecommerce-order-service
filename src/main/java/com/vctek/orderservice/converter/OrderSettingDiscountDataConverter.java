package com.vctek.orderservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.OrderSettingDiscountData;
import com.vctek.orderservice.model.OrderSettingDiscountModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderSettingDiscountDataConverter extends AbstractPopulatingConverter<OrderSettingDiscountModel, OrderSettingDiscountData> {

    @Autowired
    private Populator<OrderSettingDiscountModel, OrderSettingDiscountData> orderSettingDiscountDataPopulator;

    @Override
    public void setTargetClass() {
        setTargetClass(OrderSettingDiscountData.class);
    }

    @Override
    public void setPopulators() {
        setPopulators(orderSettingDiscountDataPopulator);
    }

}
