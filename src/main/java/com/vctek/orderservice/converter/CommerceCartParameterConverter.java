package com.vctek.orderservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.CommerceAbstractOrderParameter;
import com.vctek.orderservice.dto.OrderEntryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("commerceCartParameterConverter")
public class CommerceCartParameterConverter extends AbstractPopulatingConverter<OrderEntryDTO, CommerceAbstractOrderParameter> {

    @Autowired
    @Qualifier("commerceCartParameterPopulator")
    private Populator<OrderEntryDTO, CommerceAbstractOrderParameter> commerceCartParameterPopulator;

    @Override
    public void setTargetClass() {
        setTargetClass(CommerceAbstractOrderParameter.class);
    }

    @Override
    public void setPopulators() {
        setPopulators(commerceCartParameterPopulator);
    }
}
