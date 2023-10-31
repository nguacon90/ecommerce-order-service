package com.vctek.orderservice.promotionengine.ruleengineservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.ProductRAO;
import com.vctek.redis.ProductData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductRaoConverter extends AbstractPopulatingConverter<ProductData, ProductRAO> {

    @Autowired
    private Populator<ProductData, ProductRAO> productRAOPopulator;

    @Override
    public void setTargetClass() {
        setTargetClass(ProductRAO.class);
    }

    @Override
    public void setPopulators() {
        setPopulators(productRAOPopulator);
    }
}
