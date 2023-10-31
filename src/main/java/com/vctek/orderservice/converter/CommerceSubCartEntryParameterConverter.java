package com.vctek.orderservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.CommerceAbstractOrderParameter;
import com.vctek.orderservice.dto.request.AddSubOrderEntryRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("commerceSubCartEntryParameterConverter")
public class CommerceSubCartEntryParameterConverter extends AbstractPopulatingConverter<AddSubOrderEntryRequest, CommerceAbstractOrderParameter> {
    @Autowired
    @Qualifier("commerceSubCartEntryParameterPopulator")
    private Populator<AddSubOrderEntryRequest, CommerceAbstractOrderParameter> commerceSubCartEntryParameterPopulator;

    @Override
    public void setTargetClass() {
        setTargetClass(CommerceAbstractOrderParameter.class);
    }

    @Override
    public void setPopulators() {
        setPopulators(commerceSubCartEntryParameterPopulator);
    }
}
