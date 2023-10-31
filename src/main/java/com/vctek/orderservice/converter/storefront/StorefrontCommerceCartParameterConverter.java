package com.vctek.orderservice.converter.storefront;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.CommerceAbstractOrderParameter;
import com.vctek.orderservice.dto.StorefrontOrderEntryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("storefrontCommerceCartParameterConverter")
public class StorefrontCommerceCartParameterConverter extends AbstractPopulatingConverter<StorefrontOrderEntryDTO, CommerceAbstractOrderParameter> {
    @Autowired
    private Populator<StorefrontOrderEntryDTO, CommerceAbstractOrderParameter> storefrontCommerceCartParameterPopulator;

    @Override
    public void setTargetClass() {
        setTargetClass(CommerceAbstractOrderParameter.class);
    }

    @Override
    public void setPopulators() {
        setPopulators(storefrontCommerceCartParameterPopulator);
    }
}
