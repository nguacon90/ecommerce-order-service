package com.vctek.orderservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.UpdateOrderParameter;
import com.vctek.orderservice.dto.request.OrderRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("updateOrderParameterConverter")
public class UpdateOrderParameterConverter extends AbstractPopulatingConverter<OrderRequest, UpdateOrderParameter> {

    @Autowired
    @Qualifier("updateOrderParameterPopulator")
    private Populator<OrderRequest, UpdateOrderParameter> updateOrderParameterPopulator;

    @Override
    public void setTargetClass() {
        setTargetClass(UpdateOrderParameter.class);
    }

    @Override
    public void setPopulators() {
        setPopulators(updateOrderParameterPopulator);
    }
}
