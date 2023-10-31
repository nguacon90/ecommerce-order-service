package com.vctek.orderservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.CommerceCheckoutParameter;
import com.vctek.orderservice.dto.request.OrderRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("commerceCheckoutParameterConverter")
public class CommerceCheckoutParameterConverter extends AbstractPopulatingConverter<OrderRequest, CommerceCheckoutParameter> {

    @Autowired
    @Qualifier("commerceCheckoutPopulator")
    private Populator<OrderRequest, CommerceCheckoutParameter> commerceCheckoutPopulator;

    @Override
    public void setTargetClass() {
        setTargetClass(CommerceCheckoutParameter.class);
    }

    @Override
    public void setPopulators() {
        setPopulators(commerceCheckoutPopulator);
    }
}
