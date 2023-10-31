package com.vctek.orderservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.ReturnOrderData;
import com.vctek.orderservice.model.ReturnOrderModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class BasicReturnOrderConverter extends AbstractPopulatingConverter<ReturnOrderModel, ReturnOrderData> {

    @Autowired
    @Qualifier("basicReturnOrderPopulator")
    private Populator<ReturnOrderModel, ReturnOrderData> basicReturnOrderPopulator;

    @Override
    public void setTargetClass() {
        setTargetClass(ReturnOrderData.class);
    }

    @Override
    public void setPopulators() {
        setPopulators(basicReturnOrderPopulator);
    }
}
