package com.vctek.orderservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.request.OrderFileParameter;
import com.vctek.orderservice.dto.request.OrderSearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderFilePrameterConverter extends AbstractPopulatingConverter<OrderSearchRequest, OrderFileParameter> {
    @Autowired
    private Populator<OrderSearchRequest, OrderFileParameter> fileParameterPopulator;

    @Override
    public void setTargetClass() {
        setTargetClass(OrderFileParameter.class);
    }

    @Override
    public void setPopulators() {
        setPopulators(fileParameterPopulator);
    }
}
