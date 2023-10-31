package com.vctek.orderservice.promotionengine.ruleengineservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.ProductRAO;
import com.vctek.redis.ProductData;
import org.springframework.stereotype.Component;

@Component
public class ProductRAOPopulator implements Populator<ProductData, ProductRAO> {

    @Override
    public void populate(ProductData source, ProductRAO target) {
        target.setId(source.getId());
        target.setDtype(source.getdType());
        //TODO Implement populate category for other promotion, currently ignore for free gift promotion
    }
}
