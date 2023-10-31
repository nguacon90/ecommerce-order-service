package com.vctek.orderservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.CommerceAbstractOrderParameter;
import com.vctek.orderservice.dto.request.AddSubOrderEntryRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("commerceSubOrderEntryParameterConverter")
public class CommerceSubOrderEntryParameterConverter extends AbstractPopulatingConverter<AddSubOrderEntryRequest, CommerceAbstractOrderParameter> {
    @Autowired
    @Qualifier("commerceSubOrderEntryParameterPopulator")
    private Populator<AddSubOrderEntryRequest, CommerceAbstractOrderParameter> commerceSubOrderEntryParameterPopulator;

    @Override
    public void setTargetClass() {
        setTargetClass(CommerceAbstractOrderParameter.class);
    }

    @Override
    public void setPopulators() {
        setPopulators(commerceSubOrderEntryParameterPopulator);
    }
}
