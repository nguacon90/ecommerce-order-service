package com.vctek.orderservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.ReturnOrderCommerceParameter;
import com.vctek.orderservice.dto.request.ReturnOrderRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ReturnOrderCommerceParameterConverter
        extends AbstractPopulatingConverter<ReturnOrderRequest, ReturnOrderCommerceParameter> {

    @Autowired
    @Qualifier("returnOrderCommerceParameterPopulator")
    private Populator<ReturnOrderRequest, ReturnOrderCommerceParameter> returnOrderCommerceParameterPopulator;

    @Override
    public void setTargetClass() {
        setTargetClass(ReturnOrderCommerceParameter.class);
    }

    @Override
    public void setPopulators() {
        setPopulators(returnOrderCommerceParameterPopulator);
    }
}
